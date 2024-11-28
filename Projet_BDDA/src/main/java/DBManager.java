import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class DBManager {
    private String basededonnee;
    private DBConfig dbconfig;
    private HashMap<String, Database> databases;

    public DBManager(DBConfig dbconfig) {
        this.basededonnee = null;
        this.dbconfig = dbconfig;
        this.databases = new HashMap<>();
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
        }

        setBasededonnee(nomBdd);
        System.out.println("Base de données : " + nomBdd + " activé");
    }

    public void addTableToCurrentDatabase(RelationBis tab) {
        Database db = databases.get(tab.getNom());
        db.ajouterTable(tab);
        if (basededonnee == null) {
            System.err.println("Il n'y a aucune base de données qui est activé à ce nom (méthode addTableToCurrentDatabase)");
        } else {
            System.out.println("Ajout de la table sur la base de données courante correctement réaliser");
        }
    }

    public RelationBis getTableFromCurrentDatabase(String nomTable) {
        Database db = databases.get(basededonnee);
        return (RelationBis) db.getTable(nomTable);
    }

    public void RemoveTableFromCurrentDatabase(String nomTable) {
        Database db = databases.get(basededonnee);
        db.supprimerTable(nomTable);
    }

    public void RemoveDatabase(String nomBdd) {
        if (databases.containsKey(nomBdd)) {
            databases.remove(nomBdd);
            System.out.println("La base de données : " + nomBdd + "a été supprimer");
        }else{
            System.out.println("Cette base de données n'existe pas");
        }
    }

    public void RemoveTableFromCurrentDatabase(){
        Database db = databases.get(basededonnee);
        db.supprimerBDD();
        System.out.println("Les tables de la base de données on été correctement supprimer");
    }

    public void RemoveDatabase(){
        System.out.println("Supp");
        databases.clear();
        System.out.println("la bdd : " + basededonnee + " a été supp");
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

    public void ListTableInCurrentDatabase(){
        Database db = databases.get(basededonnee);
        if(db == null){
            System.err.println("La base de donnée n'existe pas");
            return;
        }

        Map<String, RelationBis> table = db.getTable(basededonnee);
        System.out.println("Voici les tables présentes dans la base de données : " + basededonnee);
        for(String tables : table.keySet()){
            System.out.println("Table : " + tables + "\n");
        }
    }

    public void SaveState() throws IOException{
        Path path = Paths.get(dbconfig.getDbPath(), "database.save");
        try(ObjectOutputStream obj = new ObjectOutputStream(new FileOutputStream(path.toString()))){
            obj.writeInt(databases.size());
        }
        System.out.println("On a sauvegarder dans le : " + path);
    }

    public void LoadState() throws IOException{
        Path path = Paths.get(dbconfig.getDbPath(), "database.save");
        if(Files.exists(path)){
            try(ObjectInputStream obj = new ObjectInputStream(new FileInputStream(path.toString()))){
                databases = (HashMap<String, Database>) obj.readObject();
                System.out.println("On a chargé depuis le : " + path);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }else{
            System.err.println("Aucun fichier trouvé pour la sauvegarde");
        }
    }
}










