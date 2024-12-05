import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class DBManager {
    private String basededonnee;
    private DBConfig dbconfig;
    private HashMap<String, Database> databases;
    DiskManager diskManager;
    BufferManager bufferManager;

    public DBManager(DBConfig dbconfig) {
        this.basededonnee = null;
        this.dbconfig = dbconfig;
        this.databases = new HashMap<>();
        this.diskManager = new DiskManager(dbconfig);
        this.bufferManager = new BufferManager(dbconfig, diskManager);
    }

    public String getBasededonnee() {
        return basededonnee;
    }

    public void setBasededonnee(String basededonnee) {
        this.basededonnee = basededonnee;
    }

    public DBConfig getDbconfig() {
        return dbconfig;
    }

    public void setDbconfig(DBConfig dbconfig) {
        this.dbconfig = dbconfig;
    }

    public HashMap<String, Database> getDatabases() {
        return databases;
    }

    public void setDatabases(HashMap<String, Database> databases) {
        this.databases = databases;
    }

    public void CreateDatabase(String nomBdd) {
        if (databases.containsKey(nomBdd)) {
            System.out.println("La base de données existes déjà, on ne peut pas créer de doublon");
        }

        System.out.println("Création de la base de données");
        Database db = new Database(nomBdd);
        databases.put(nomBdd, db);
        System.out.println("La base de données : " + nomBdd + "a bien été créer");
    }

    public void setCurrentDatabase(String nomBdd) {
        if (!databases.containsKey(nomBdd)) {
            System.err.println("La base de données n'existe pas");
            //CreateDatabase(nomBdd);
        }else{
            setBasededonnee(nomBdd);
            System.out.println("Base de données : " + nomBdd + " activé");
        }
    }

    public void addTableToCurrentDatabase(Relation tab) {
        // Vérifier que la base de données courante est définie
        if (basededonnee == null) {
            System.err.println("Il n'y a aucune base de données activée.");
            return;
        }

        // Récupérer la base de données courante
        Database db = databases.get(basededonnee);

        if (db == null) {
            System.err.println("La base de données courante " + basededonnee + " n'existe pas.");
            return;
        }

        // Ajouter la table à la base de données
        db.ajouterTable(tab);

        System.out.println("Table " + tab.getNom() + " ajoutée à la base de données " + basededonnee + ".");
    }


    public Relation getTableFromCurrentDatabase(String nomTable) {
        if (basededonnee == null) {
            System.err.println("Aucune base de données activée. Impossible de récupérer la table.");
            return null;
        }

        Database db = databases.get(basededonnee);
        if (db == null) {
            System.err.println("La base de données " + basededonnee + " n'existe pas.");
            return null;
        }

        Relation table = (Relation) db.getTable(nomTable);
        if (table == null) {
            System.err.println("La table " + nomTable + " n'existe pas dans la base de données " + basededonnee + ".");
            return null;
        }

        System.out.println("Table " + nomTable + " récupérée avec succès de la base de données " + basededonnee + ".");
        return table;
    }


    public void RemoveTableFromCurrentDatabase(String nomTable) {
        if (basededonnee == null) {
            System.err.println("Aucune base de données activée. Impossible de supprimer une table.");
            return;
        }

        Database db = databases.get(basededonnee);
        if (db == null) {
            System.err.println("La base de données " + basededonnee + " n'existe pas.");
            return;
        }

        // Vérification si la table existe dans la base de données active
        if (!db.containsTable(nomTable)) {  // Vous devez ajouter cette méthode dans la classe Database
            System.err.println("La table " + nomTable + " n'existe pas dans la base de données " + basededonnee + ".");
            return;
        }

        db.supprimerTable(nomTable);
        System.out.println("La table " + nomTable + " a été supprimée avec succès de la base de données " + basededonnee + ".");
    }


    public void RemoveDatabase(String nomBdd) {
        if (databases.containsKey(nomBdd)) {
            databases.remove(nomBdd);
            System.out.println("La base de données : " + nomBdd + "a été supprimer");
        }else{
            System.out.println("Cette base de données n'existe pas");
        }
    }

    public Collection<Relation> getTables() {
        // Vérifier si la base de données courante est activée
        if (basededonnee == null) {
            System.err.println("Aucune base de données activée. Impossible de récupérer les tables.");
            return null;
        }

        Database db = databases.get(basededonnee);
        if (db == null) {
            System.err.println("La base de données " + basededonnee + " n'existe pas.");
            return null;
        }

        // Récupérer et retourner les tables de la base de données courante
        return db.getTables();  // Appel à getTables() de la classe Database
    }

    public void RemoveDatabase(){
        Scanner s = new Scanner(System.in);
        System.out.println("Tapez le nom de la bdd à supprimer");
        String nom = s.nextLine();
        if(!databases.containsKey(nom)){
            System.out.println("Ta base de données : " + nom + " n'existe pas");
        }else{
            databases.remove(nom);
            System.out.println("Base de données : " + nom + " correctement supprimer");

        }
    }

    public void RemoveDatabases(){
        if (databases != null && !databases.isEmpty()) {
            System.out.println("******************Suppression de toutes les bases de données*******************");
            System.out.println("Bases de données à supprimer : " + databases.keySet());
            databases.clear();
            System.out.println("Toutes les bases de données ont été supprimées.");
        } else {
            System.out.println("Aucune base de données à supprimer.");
        }
    }


    public void RemoveTablesFromCurrentDatabase(){
        Database db = databases.get(basededonnee);
        db.supprimerBDD();
        System.out.println("Les tables de la base de données on été correctement supprimer");
    }



    public String toString(){
        StringBuilder sb = new StringBuilder();
        System.out.println("Affichage de(s) base de données");
        for(Database db : databases.values()){
            sb.append(db).append("\n");
        }
        System.out.println("******************Fin d'affichage**************");
        return sb.toString();
    }

    public void ListDatabases(){
        System.out.println("Ensemble des bases de données : ");
        for(Database db : databases.values()){
            System.out.println(db.toString() + "\n");
        }
    }

    public void ListTableInCurrentDatabase() {
        if (basededonnee == null) {
            System.err.println("Aucune base de données activée. Impossible de récupérer les tables.");
            return;
        }

        Database db = databases.get(basededonnee);
        if (db == null) {
            System.err.println("La base de données " + basededonnee + " n'existe pas.");
            return;
        }

        Collection<Relation> tables = db.getTables();

        if (tables.isEmpty()) {
            System.out.println("Aucune table n'existe dans la base de données " + basededonnee + ".");
            return;
        }

        System.out.println("Voici les tables présentes dans la base de données : " + basededonnee);
        for (Relation table : tables) {
            System.out.println("Table : " + table.getNom() + "\n");
        }
    }


    public void SaveState() {
        String chemin = dbconfig.getDbPath() + "/../databases.save.json";

        try {
            JSONObject existingData = new JSONObject();
            File fichier = new File(chemin);

            if (fichier.exists()) {
                FileReader fr = new FileReader(chemin);
                BufferedReader bfr = new BufferedReader(fr);
                StringBuilder sb = new StringBuilder();
                String ligne;
                while ((ligne = bfr.readLine()) != null) {
                    sb.append(ligne);
                }
                fr.close();
                existingData = new JSONObject(sb.toString());
            }

            if (!existingData.has("Bases de données")) {
                existingData.put("Bases de données", new JSONObject());
            }

            JSONObject basesDeDonnees = existingData.getJSONObject("Bases de données");

            // Sauvegarder chaque base de données
            for (String dbName : this.databases.keySet()) {
                JSONObject dbInfo = new JSONObject();
                dbInfo.put("Nom BD", dbName);

                // Informations sur la base de données
                JSONObject infoBD = new JSONObject();
                JSONArray relationsArray = new JSONArray();

                // Sauvegarder les relations de la base de données
                for (Relation relation : this.databases.get(dbName).getTables()) {
                    JSONObject relationJson = new JSONObject();
                    relationJson.put("Nom relation", relation.getNom());
                    relationJson.put("Header page", relation.getHeaderPageId().toString());
                    relationJson.put("Nombre colonnes", relation.getNbColonnes());

                    // Sauvegarder les colonnes de chaque relation
                    JSONArray colonnesArray = new JSONArray();
                    for (ColInfo colonne : relation.getColonnes()) {
                        if (colonne != null) {
                            JSONObject colonneJson = new JSONObject();
                            colonneJson.put("Nom Colonne", colonne.getNom());
                            colonneJson.put("Type Colonne", colonne.getType());
                            colonneJson.put("Taille Colonne", colonne.getTaille());
                            colonnesArray.put(colonneJson);
                        }
                    }

                    relationJson.put("Colonnes", colonnesArray);
                    relationsArray.put(relationJson);
                }

                infoBD.put("Relations", relationsArray);
                infoBD.put("Nbr Relations", relationsArray.length());
                dbInfo.put("info BD", infoBD);

                // Ajouter ou mettre à jour la base de données dans le fichier
                basesDeDonnees.put(dbName, dbInfo);
            }

            // Écrire les données dans le fichier JSON
            FileWriter fw = new FileWriter(fichier);
            BufferedWriter bfw = new BufferedWriter(fw);

            // Sauvegarde avec une indentation pour rendre le fichier lisible
            bfw.write(existingData.toString(4));
            bfw.flush();
            bfw.close();

            System.out.println("État des bases de données sauvegardé avec succès dans " + chemin);

        } catch (IOException e) {
            System.out.println("DBManager : SAVE STATE : Le fichier n'a pas pu être sauvegardé.");
            e.printStackTrace();
        }
    }


    public void LoadState() {
        String chemin = dbconfig.getDbPath() + "/../databases.save.json";

        try {
            File fichier = new File(chemin);
            if (!fichier.exists()) {
                return; // Si le fichier n'existe pas, on sort de la méthode
            }

            // Lecture du contenu du fichier JSON
            FileReader fr = new FileReader(fichier);
            BufferedReader bfr = new BufferedReader(fr);
            StringBuilder sb = new StringBuilder();
            String ligne;
            while ((ligne = bfr.readLine()) != null) {
                sb.append(ligne);
            }
            bfr.close();

            JSONObject js = new JSONObject(sb.toString());
            JSONObject basesDeDonnees = js.getJSONObject("Bases de données");

            // Parcours des bases de données
            for (String bdName : basesDeDonnees.keySet()) {
                JSONObject dbJson = basesDeDonnees.getJSONObject(bdName);

                // Création et définition de la base de données courante
                this.CreateDatabase(bdName);
                this.setCurrentDatabase(bdName);

                JSONObject infoBD = dbJson.getJSONObject("info BD");
                JSONArray relationsArray = infoBD.getJSONArray("Relations");

                // Parcours des relations de la base de données
                for (int i = 0; i < relationsArray.length(); i++) {
                    JSONObject relationJson = relationsArray.getJSONObject(i);

                    String nomRelation = relationJson.getString("Nom relation");
                    String headerPageString = relationJson.getString("Header page");
                    int nbrColonnes = relationJson.getInt("Nombre colonnes");

                    // Conversion du Header Page (ex: "PageId{fileIdx=1, pageIdx=0}") en objet PageId
                    int fileIdx = Integer.parseInt(headerPageString.split("=")[1].split(",")[0].trim());
                    int pageIdx = Integer.parseInt(headerPageString.split("=")[2].replace("}", "").trim());
                    PageId headerPage = new PageId(pageIdx, fileIdx);

                    // Création de la liste des colonnes de la relation
                    List<ColInfo> listeColonnesInfo = new ArrayList<>();
                    JSONArray colonnesArray = relationJson.getJSONArray("Colonnes");

                    for (int j = 0; j < colonnesArray.length(); j++) {
                        JSONObject colonneJson = colonnesArray.getJSONObject(j);
                        String nomColonne = colonneJson.getString("Nom Colonne");
                        String typeColonne = colonneJson.getString("Type Colonne");
                        int tailleColonne = colonneJson.getInt("Taille Colonne");

                        ColInfo colonne = new ColInfo(nomColonne, typeColonne, tailleColonne);
                        listeColonnesInfo.add(colonne);
                    }

                    // Création de la relation et ajout à la base de données courante
                    Relation relation = new Relation(nomRelation, nbrColonnes, headerPage, diskManager, bufferManager);
                    relation.setColonnes(listeColonnesInfo.toArray(new ColInfo[0]));
                    this.addTableToCurrentDatabase(relation);
                }
            }

            System.out.println("État des bases de données chargé avec succès.");

        } catch (IOException | JSONException e) {
            System.out.println("Erreur lors du chargement de l'état des bases de données : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /*Relation relation = new Relation(nomRelation, nbrColonnes, headerPage, diskManager, bufferManager);*/


    /*public void SaveState() throws IOException{
        Path path = Paths.get(dbconfig.getDbPath(), "database.save");
        Files.createDirectories(path.getParent());
        try(ObjectOutputStream obj = new ObjectOutputStream(new FileOutputStream(path.toString()))){
            obj.writeObject(databases);
        }
        System.out.println("On a sauvegarder dans le : " + path);
    }

    public void LoadState() throws IOException {
        Path path = Paths.get(dbconfig.getDbPath(), "database.save");
        if (Files.exists(path)) {
            try (ObjectInputStream obj = new ObjectInputStream(new FileInputStream(path.toString()))) {
                databases = (HashMap<String, Database>) obj.readObject();
                System.out.println("Chargement réussi depuis le fichier : " + path);
            } catch (ClassNotFoundException e) {
                System.err.println("Classe introuvable lors de la désérialisation : " + e.getMessage());
            }
        } else {
            System.err.println("Aucun fichier trouvé pour la sauvegarde : " + path);
        }
    }*/
}










