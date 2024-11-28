import java.io.IOException;
import java.util.Scanner;

public class SGBD {
    private DBConfig dbConfig;
    private DiskManager diskManager;
    private BufferManagerBis bufferManager;
    private DBManager dbManager;

    public SGBD(DBConfig db){
        this.dbConfig = db;
        this.diskManager = new DiskManager(db);
        this.bufferManager = new BufferManagerBis(dbConfig, diskManager);
        this.dbManager = new DBManager(db);
        try{
            dbManager.LoadState();
            diskManager.loadState();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public DBConfig getDbConfig(){
        return dbConfig;
    }

    public DiskManager getDiskManager(){
        return diskManager;
    }

    public BufferManagerBis getBufferManager(){
        return bufferManager;
    }

    public DBManager getDBManager(){
        return dbManager;
    }

    public void Run() throws IOException {
        Scanner commande = new Scanner(System.in);
        String choix;

        System.out.println("Initialisation du SGBD");

        //La boucle s'arrête quand l'utilisateur va sélectionner la commande "QUIT", c'est pour ça que j'ai mis une boucle infinie
        while(true) {
            System.out.println("Choisissez votre commande");
            choix = commande.nextLine();
            switch (choix) {
                case "CREATE DATABASE" :
                    ProcessCreateDatabaseCommand(choix);
                case "SET DATABASE" :
                    ProcessSetDatabaseCommand(choix);
                case "CREATE TABLE" :
                    //A FAIRE PLUS TARD
                case "DROP TABLE" :
                    ProcessDropDatabaseCommand(choix);
                case "LIST TABLE" :
                    ProcessListTableCommand();
                case "LIST DATABASE" :
                    ProcessListDatabaseCommand();
                case "DROP DATABASE" :
                    ProcessDropDatabaseCommand();
                case "QUIT" :
                    ProcessQuitCommand();
                default:
                    System.out.println("Aucune commande n'a été reconnu");
            }
            commande.close();
        }
    }

    private void ProcessQuitCommand() throws IOException {
        dbManager.SaveState();
        bufferManager.FlushBuffers();
        System.out.println("FIN");
    }

    private void ProcessCreateDatabaseCommand(String commande) throws IOException {
        /*Pour la méthode trim(), je l'ai vu sur la documentation Java, elle permet de ne pas comptabiliser les
        espaces, ou les retours à la ligne, ça peut nous éviter d'avoir des erreurs lorsqu'on veut taper une commande*/
        String nomBDD = commande.substring(15).trim();
        dbManager.CreateDatabase(nomBDD);
        System.out.println("La base de données : " + nomBDD + " à été créer");
    }

    private void ProcessSetDatabaseCommand(String commande) throws IOException {
        String nomBDD = commande.substring(15).trim();
        dbManager.setCurrentDatabase(nomBDD);
        System.out.println("La base de données : " + nomBDD + " à bien été mise à jour");
    }

    //Dans cette méthode, faut initialiser la HEADERPAGE, vu qu'on a encore des problèmes dessus, j'ai mis le code en commentaire
    /*private void ProcessCreateTableCommand(String commande) throws IOException {
        String nomBDD = commande.substring(15).trim();
        RelationBis relation = new RelationBis(nomBDD);
        dbManager.addTableToCurrentDatabase(relation);
    }*/

    private void ProcessDropDatabaseCommand(String commande) throws IOException {
        String nomTable = commande.substring(15).trim();
        dbManager.RemoveTableFromCurrentDatabase(nomTable);
        System.out.println("La table : " + nomTable + " a été correctement supprimer");
    }

    private void ProcessListTableCommand() throws IOException {
        dbManager.ListTableInCurrentDatabase();
    }

    private void ProcessListDatabaseCommand() throws IOException {
        dbManager.ListDatabases();
    }

    private void ProcessDropDatabaseCommand(){
        dbManager.RemoveDatabase();
    }
}
