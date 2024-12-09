import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DBManager {
    private Database activeDB;
    private HashMap<String, Database> dataBases;
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

    public Database getActiveDB() {
        return activeDB;
    }
    public void setActiveDB(Database activeDB) {
        this.activeDB = activeDB;
    }

    public HashMap<String, Database> getDataBases() {
        return dataBases;
    }
    public void setDataBases(HashMap<String, Database> dataBases) {
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
            this.dataBases.put(dbName, new Database (dbName));
            System.out.println("Création de la base de données.....");
            System.out.println("La base de données : {" + dbName + "} a bien été créée.");
        }
    }

    public void SetCurrentDatabase (String dbName) {
        if (! this.dataBases.containsKey(dbName)) {
            System.err.println("Erreur : La base de données {" + dbName + "} n'existe pas...");
        } else    {
            this.activeDB = this.dataBases.get(dbName);
            System.out.println("Activation de la base de données.....");
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
        System.out.println("La table a bien été créer !");
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
        System.out.println("Suppression de la table.....");
        System.out.println("La table (" + tableName + ") a été supprimée avec succès de la base de données {" + this.activeDB.getNom() + "}");
    }

    public void RemoveDatabase(String dbName){
        if(!this.dataBases.containsKey(dbName)){
            System.err.println("Erreur : Aucune base de données enregistrée sous le nom {" + dbName + "}...");
        }   else    {
            Database db = this.dataBases.get(dbName);
            this.setActiveDB(db);
            for (String tableName : new ArrayList<>(this.activeDB.getRelations().keySet()))   {
                RemoveTableFromCurrentDatabase(tableName);
            }
            this.dataBases.remove(dbName);
            this.setActiveDB(null);
            System.out.println("Suppression de la base de données.....");
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
        System.out.println("Suppression de toute les bases de données.....");
        for (String tableName : new ArrayList<>(this.activeDB.getRelations().keySet()))    {
            RemoveTableFromCurrentDatabase(tableName);
        }

    }

    public void ListDatabasesNames() {
        System.out.println("Bases de données :");
        for (Database dataBase : this.dataBases.values()){
            System.out.println(dataBase.getNom());
        }
    }

    public void ListDatabases() {
        System.out.println("Voici les bases de données actuellement présente dans votre SGBD :");
        for (Database dataBase : this.dataBases.values())  {
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

    /*public void SaveState() {
        String path = this.dbconfig.getDbPath() + "/bindata/databases.json";
        try     {
            File file = new File(path);
            JSONObject root = new JSONObject();
            JSONObject dataBasesJson = new JSONObject();
            root.put("dataBases", dataBasesJson);
            // Cuisiner l'objet JSON contenant toutes les informations sur les bdds
            for (Database db : this.dataBases.values())     {
                String dbName = db.getNom();
                JSONObject dbInfos = new JSONObject();
                dbInfos.put("numberOfTables", db.getTables().size());
                JSONObject relationsJSON = new JSONObject();
                for (Relation table : db.getTables())   {
                    String tableName = table.getNom();
                    JSONObject relationJSON = new JSONObject();
                    relationJSON.put("numberOfColumns", table.getNbColonnes());
                    JSONObject headerPageIdJSON = new JSONObject();
                    headerPageIdJSON.put("fileIdx",table.getHeaderPageId().getFileIdx());
                    headerPageIdJSON.put("pageIdx",table.getHeaderPageId().getPageIdx());
                    relationJSON.put("headerPageId",  headerPageIdJSON);
                    JSONObject columnsJSON = new JSONObject();
                    for (ColInfo colonne : table.getColonnes()) {
                        JSONObject columnJSON = new JSONObject();
                        columnJSON.put("type", colonne.getType());
                        columnJSON.put("size", colonne.getTaille());
                        columnsJSON.put(colonne.getNom(), columnJSON);
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
    }*/

    public void SaveState() {
        String path = this.dbconfig.getDbPath() + "/bindata/databases.json";
        try {
            File file = new File(path);
            JSONObject root = new JSONObject();
            JSONObject dataBasesJson = new JSONObject();
            root.put("dataBases", dataBasesJson);

            // Construire l'objet JSON contenant toutes les informations sur les bases de données
            for (Database db : this.dataBases.values()) {
                String dbName = db.getNom();
                JSONObject dbInfos = new JSONObject();
                dbInfos.put("numberOfTables", db.getTables().size());
                JSONObject relationsJSON = new JSONObject();

                for (Relation table : db.getTables()) {
                    String tableName = table.getNom();
                    JSONObject relationJSON = new JSONObject();
                    relationJSON.put("numberOfColumns", table.getNbColonnes());

                    JSONObject headerPageIdJSON = new JSONObject();
                    headerPageIdJSON.put("fileIdx", table.getHeaderPageId().getFileIdx());
                    headerPageIdJSON.put("pageIdx", table.getHeaderPageId().getPageIdx());
                    relationJSON.put("headerPageId", headerPageIdJSON);

                    // Utiliser un JSONArray pour garantir l'ordre des colonnes
                    JSONArray columnsArray = new JSONArray();
                    for (ColInfo colonne : table.getColonnes()) {
                        JSONObject columnJSON = new JSONObject();
                        columnJSON.put("name", colonne.getNom());
                        columnJSON.put("type", colonne.getType());
                        columnJSON.put("size", colonne.getTaille());
                        columnsArray.put(columnJSON); // Ajouter chaque colonne au tableau
                    }
                    relationJSON.put("columns", columnsArray); // Ajouter les colonnes sous forme de tableau
                    relationsJSON.put(tableName, relationJSON);
                }

                dbInfos.put("tables", relationsJSON);
                dataBasesJson.put(dbName, dbInfos);
            }

            // Écrire l'objet JSON dans le fichier
            FileWriter fileWr = new FileWriter(file);
            BufferedWriter bufferWr = new BufferedWriter(fileWr);
            bufferWr.write(root.toString(4)); // Formater avec une indentation de 4 espaces
            bufferWr.flush();
            bufferWr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void LoadState() {
        String path = this.dbconfig.getDbPath() + "/bindata/databases.json";
        try {
            File file = new File(path);
            if (!file.exists()) {
                return;
            }
            FileReader fileRd = new FileReader(file);
            BufferedReader bufferRd = new BufferedReader(fileRd);
            StringBuilder content = new StringBuilder();
            String line = null;
            while ((line = bufferRd.readLine()) != null) {
                content.append(line);
            }
            bufferRd.close();

            // Construire un objet JSON à partir du contenu lu depuis le fichier
            JSONObject contentJSON = new JSONObject(content.toString());
            JSONObject rootJSON = contentJSON.getJSONObject("dataBases");

            // Parcourir les bases de données une par une
            for (String dbName : rootJSON.keySet()) {
                JSONObject dataBaseJSON = rootJSON.getJSONObject(dbName);
                this.CreateDatabase(dbName);
                int nbTables = dataBaseJSON.getInt("numberOfTables");
                JSONObject relationsJSON = dataBaseJSON.getJSONObject("tables");

                for (String relationName : relationsJSON.keySet()) {
                    this.SetCurrentDatabase(dbName);
                    JSONObject relationJSON = relationsJSON.getJSONObject(relationName);
                    int nbColonnes = relationJSON.getInt("numberOfColumns");
                    JSONObject headerPageIdJSON = relationJSON.getJSONObject("headerPageId");
                    int fileIdx = headerPageIdJSON.getInt("fileIdx");
                    int pageIdx = headerPageIdJSON.getInt("pageIdx");
                    PageId headerPageId = new PageId(fileIdx, pageIdx);

                    // Récupérer les colonnes depuis le JSONArray
                    JSONArray columnsArray = relationJSON.getJSONArray("columns");
                    ColInfo[] colonnes = new ColInfo[nbColonnes];
                    for (int i = 0; i < columnsArray.length(); i++) {
                        JSONObject columnJSON = columnsArray.getJSONObject(i);
                        String columnName = columnJSON.getString("name");
                        String columnType = columnJSON.getString("type");
                        int columnSize = columnJSON.getInt("size");
                        colonnes[i] = new ColInfo(columnName, columnType, columnSize);
                    }

                    Relation table = new Relation(relationName, nbColonnes, colonnes, headerPageId, this.diskManager, this.bufferManager);
                    this.AddTableToCurrentDatabase(table);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}