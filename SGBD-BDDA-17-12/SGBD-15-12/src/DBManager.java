import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DBManager {
    private DataBase activeDB;
    private HashMap<String, DataBase> dataBases;
    private DBConfig dbconfig;
    private DiskManager diskManager;
    private BufferManager bufferManager;

    public DBManager(DBConfig dbconfig, DiskManager diskManager, BufferManager bufferManager)   {
        this.activeDB = null;
        this.dataBases = new HashMap<>();
        this.dbconfig = dbconfig;
        this.diskManager = diskManager;
        this.bufferManager = bufferManager;
    }

    public DBManager (DBConfig dbconfig)  {
        this.activeDB = null;
        this.dataBases = new HashMap<>();
        this.dbconfig = dbconfig;
        this.diskManager = new DiskManager(dbconfig);
        this.bufferManager = new BufferManager(this.dbconfig, this.diskManager);
    }

    public DataBase getActiveDB() {
        return activeDB;
    }
    public void setActiveDB(DataBase activeDB) {
        this.activeDB = activeDB;
    }

    public HashMap<String, DataBase> getDataBases() {
        return dataBases;
    }
    public void setDataBases(HashMap<String, DataBase> dataBases) {
        this.dataBases = dataBases;
    }

    public DBConfig getDbconfig() {
        return dbconfig;
    }
    public void setDbconfig(DBConfig dbconfig) {
        this.dbconfig = dbconfig;
    }

    public DiskManager getDiskManager() {
        return diskManager;
    }
    public void setDiskManager(DiskManager diskManager) {
        this.diskManager = diskManager;
    }

    public BufferManager getBufferManager() {
        return bufferManager;
    }
    public void setBufferManager(BufferManager bufferManager) {
        this.bufferManager = bufferManager;
    }

    public void CreateDatabase (String dbName) {
        if (this.dataBases.containsKey(dbName)) {
            System.err.println("Erreur : La base de données {" + dbName + "} existe déjà, on ne peut pas en créer un doublon...");
        }   else    {
            this.dataBases.put(dbName, new DataBase (dbName));
            System.out.println("La base de données : {" + dbName + "} a bien été créée.");
        }
    }

    public void SetCurrentDatabase (String dbName) {
        if (! this.dataBases.containsKey(dbName)) {
            System.err.println("Erreur : La base de données {" + dbName + "} n'existe pas...");
        } else    {
            this.activeDB = this.dataBases.get(dbName);
            System.out.println("Base de données {" + dbName + "} désormais active.");
        }
    }
    
    public void AddTableToCurrentDatabase(Relation table) {
        // Vérifier que la base de données courante est définie
        if (this.activeDB == null) {
            System.err.println("Erreur : Il n'y a aucune base de données active pour le moment. Commencez par en activer une d'abord avant d'y ajouter une table...");
            return ;
        }
        // Ajouter la table à la base de données
        this.activeDB.addTable(table);
    }

    public Relation GetTableFromCurrentDatabase(String tableName) {
        if (this.activeDB == null) {
            System.err.println("Erreur : Il n'y a aucune base de données active pour le moment. Commencez par en activer une d'abord avant d'y tirer des tables...");
            return null;
        }
        Relation table = this.activeDB.getTable(tableName);
        if (table == null) {
            System.err.println("Erreur : La table (" + tableName + ") n'existe pas dans la base de données {" + this.activeDB.getNom() + "}");
            return null;
        }
        return table;
    }

    public void RemoveTableFromCurrentDatabase (String tableName) {
        if (this.activeDB == null) {
            System.err.println("Erreur : Il n'y a aucune base de données active pour le moment. Commencez par en activer une d'abord avant d'y supprimer une table...");
            return;
        }
        if (this.activeDB.containsTable(tableName))     {
            Relation table = this.activeDB.getTable(tableName);
            List<PageId> tableDataPages = table.getDataPages();
            for (PageId datapage : tableDataPages)  {
                this.diskManager.DeallocPage(datapage);
            }
            this.diskManager.DeallocPage(table.getHeaderPageId());
        }
        this.activeDB.removeTable(tableName);
        System.out.println("La table (" + tableName + ") a été supprimée avec succès de la base de données {" + this.activeDB.getNom() + "}");
    }

    public void RemoveDatabase(String dbName){
        if(!this.dataBases.containsKey(dbName)){
            System.err.println("Erreur : Aucune base de données enregistrée sous le nom {" + dbName + "}...");
        }   else    {
            DataBase db = this.dataBases.get(dbName);
            this.setActiveDB(db);
            for (String tableName : new ArrayList<>(this.activeDB.getRelations().keySet()))   {
                RemoveTableFromCurrentDatabase(tableName);
            }
            this.dataBases.remove(dbName);
            this.setActiveDB(null);
            System.out.println("La base de données {" + dbName + "} est correctement supprimée");
        }
    }

    public void RemoveDatabases ()  {
        for (String dbName : new ArrayList<>(this.dataBases.keySet()))  {
            this.RemoveDatabase(dbName);
        }
    }

    public void RemoveTablesFromCurrentDatabase()   {
        if (this.activeDB == null)  {
            System.err.println("Erreur : Il n'y a aucune base de données active pour le moment. Commencez par en activer une d'abord avant d'y supprimer ses table...");
            return;
        }
        for (String tableName : new ArrayList<>(this.activeDB.getRelations().keySet()))    {
            RemoveTableFromCurrentDatabase(tableName);
        }
        
    }

    public void ListDatabasesNames() {
        System.out.println("Bases de données :");
        for (DataBase dataBase : this.dataBases.values()){
            System.out.println(dataBase.getNom());
        }
    }

    public void ListDatabases() {
        System.out.println("Bases de données :");
        for (DataBase dataBase : this.dataBases.values())  {
            System.out.print(dataBase.toString()+"\n");
        }
    }

    public void ListTablesInCurrentDatabase() {
        if (this.activeDB == null) {
            System.err.println("Erreur : Il n'y a aucune base de données active pour le moment. Commencez par en activer une d'abord avant d'afficher ses tables...");
            return;
        }
        Collection<Relation> tables = this.activeDB.getTables();
        if (tables.isEmpty()) {
            System.out.println("Aucune table n'existe dans la base de données {" + this.activeDB.getNom() + "}");
            return;
        }
        System.out.println("Tables présentes dans la base de données {" + this.activeDB.getNom() + "} : ");
        for (Relation table : tables) {
            System.out.print(table.toString());
        }
    }

    public void SaveState() {
        String path = this.dbconfig.getDbPath() + "/bindata/databases.json";
        try     {
            File file = new File(path);
            JSONObject root = new JSONObject();
            JSONObject dataBasesJson = new JSONObject();
            root.put("dataBases", dataBasesJson);
            // Cuisiner l'objet JSON contenant toutes les informations sur les bdds
            for (DataBase db : this.dataBases.values())     {
                String dbName = db.getNom();
                JSONObject dbInfos = new JSONObject();
                dbInfos.put("numberOfTables", db.getTables().size());
                JSONObject relationsJSON = new JSONObject();
                for (Relation table : db.getTables())   {
                    String tableName = table.getNom();
                    JSONObject relationJSON = new JSONObject(new LinkedHashMap<>());
                    relationJSON.put("numberOfColumns", table.getNbColonnes());
                    JSONObject headerPageIdJSON = new JSONObject();
                    headerPageIdJSON.put("fileIdx",table.getHeaderPageId().getFileIdx());
                    headerPageIdJSON.put("pageIdx",table.getHeaderPageId().getPageIdx());
                    relationJSON.put("headerPageId",  headerPageIdJSON);
                    JSONArray columnsJSON = new JSONArray();
                    for (ColInfo colonne : table.getColonnes()) {
                        JSONObject columnJSON = new JSONObject();
                        columnJSON.put("name", colonne.getNom());
                        columnJSON.put("type", colonne.getType());
                        columnJSON.put("size", colonne.getTaille());
                        columnsJSON.put(columnJSON);
                    }
                    relationJSON.put("columns", columnsJSON);
                    relationsJSON.put(tableName, relationJSON);
                }
                dbInfos.put("tables", relationsJSON);
                dataBasesJson.put(dbName, dbInfos);
            }
            // ecrire le fichier JSON déjà prêt dans notre fichier réservé pour...
            FileWriter fileWr = new FileWriter(file);
            BufferedWriter bufferWr = new BufferedWriter(fileWr);                
            // Ecriture
            bufferWr.write(root.toString(4));
            bufferWr.flush();
            bufferWr.close();       
        }   catch (IOException e)   {
            e.printStackTrace();
        }
    }

    public void LoadState() {
        String path = this.dbconfig.getDbPath() + "/bindata/databases.json";
        try     {
            File file = new File(path);
            if (! file.exists())    {
                return ;
            }
            FileReader fileRd = new FileReader(file);
            BufferedReader bufferRd = new BufferedReader(fileRd);
            StringBuilder content = new StringBuilder();
            String line = null;
            while ((line = bufferRd.readLine()) != null)    {
                content.append(line);
            }
            bufferRd.close();
            // Construire un objet json à partir du contenu lu depuis le fichier json
            JSONObject contentJSON = new JSONObject(content.toString());
            JSONObject rootJSON = contentJSON.getJSONObject("dataBases");
            // Parcourrir les bases de données une par une
            for (String dbName : rootJSON.keySet())     {
                JSONObject dataBaseJSON = rootJSON.getJSONObject(dbName);   
                this.CreateDatabase(dbName);
                int nbTables = dataBaseJSON.getInt("numberOfTables");
                JSONObject relationsJSON = dataBaseJSON.getJSONObject("tables");
                for (String relationName : relationsJSON.keySet())  {
                    this.SetCurrentDatabase(dbName);
                    JSONObject relationJSON = relationsJSON.getJSONObject(relationName);
                    int nbColonnes = relationJSON.getInt("numberOfColumns");
                    JSONObject headerPageIdJSON = relationJSON.getJSONObject("headerPageId");
                    int fileIdx = headerPageIdJSON.getInt("fileIdx");
                    int pageIdx = headerPageIdJSON.getInt("pageIdx");
                    PageId headerPageId = new PageId(fileIdx, pageIdx);
                    JSONArray columnsJSON = relationJSON.getJSONArray("columns");
                    ColInfo[] colonnes = new ColInfo[nbColonnes];  
                    for (int i=0; i<columnsJSON.length(); i+=1)     {
                        JSONObject columnJSON = columnsJSON.getJSONObject(i);
                        String columnName = columnJSON.getString("name"); 
                        String columnType = columnJSON.getString("type");
                        int columnSize = columnJSON.getInt("size");
                        colonnes[i] = new ColInfo(columnName, columnType, columnSize);
                    }
                    Relation table = new Relation(relationName, nbColonnes, colonnes, headerPageId, this.diskManager,  this.bufferManager);
                    this.AddTableToCurrentDatabase(table);
                }
            }
        }   catch (IOException e)   {
            e.printStackTrace();
        }
    }

}