import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

public class SGBD {
    private DBConfig dbConfig;
    private DiskManager diskManager;
    private BufferManager bufferManager;
    private DBManager dbManager;

    public SGBD(DBConfig db) throws IOException {
        this.dbConfig = db;
        this.diskManager = new DiskManager(db);
        this.bufferManager = new BufferManager(dbConfig, diskManager);
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

    public BufferManager getBufferManager(){
        return bufferManager;
    }

    public DBManager getDBManager(){
        return dbManager;
    }

    public void Run() throws IOException {
        System.out.println("***********************Bienvenue dans la SGBD***********************");
        String texteCommande = "";
        boolean quit = false;

        // Boucle principale de commande
        while (!quit) {
            System.out.println("Entrez votre commande !");
            Scanner sc = new Scanner(System.in);
            texteCommande = sc.nextLine(); // Récupération de la commande entrée par l'utilisateur

            // Traitement des différentes commandes
            if (texteCommande.contains("CREATE DATABASE")) {
                if (!texteCommande.replace("CREATE DATABASE", "").isEmpty()) {
                    System.out.println("La commande choisie est " + texteCommande);
                    ProcessCreateDatabaseCommand(texteCommande); // Création de la base de données
                } else {
                    System.out.println("Vous n'avez pas tapé le nom de la database");
                }
            } else if (texteCommande.contains("CREATE TABLE")) {
                if (!texteCommande.replace("CREATE TABLE", "").isEmpty()) {
                    System.out.println("La commande choisie est " + texteCommande);
                    ProcessCreateTableCommand(texteCommande); // Création de la table
                } else {
                    System.out.println("Vous n'avez pas tapé le nom de la table");
                }
            } else if (texteCommande.contains("SET DATABASE")) {
                if (!texteCommande.replace("SET DATABASE", "").isEmpty()) {
                    System.out.println("La commande choisie est " + texteCommande);
                    ProcessSetDatabaseCommand(texteCommande); // Sélectionner la base de données
                } else {
                    System.out.println("Vous n'avez pas tapé le nom de la database");
                }
            } else if (texteCommande.contains("LIST TABLES")) {
                System.out.println("La commande choisie est " + texteCommande);
                ProcessListTablesCommand(texteCommande); // Lister les tables
            } else if (texteCommande.contains("LIST DATABASES")) {
                System.out.println("La commande choisie est " + texteCommande);
                ProcessListDatabasesCommand(texteCommande); // Lister les bases de données
            } else if (texteCommande.startsWith("DROP TABLE")) {
                System.out.println("La commande choisie est " + texteCommande);
                ProcessDropTableCommand(texteCommande); // Supprimer une table
            } else if (texteCommande.startsWith("DROP DATABASE")) {
                if (!texteCommande.replace("DROP DATABASE", "").isEmpty()) {
                    if (texteCommande.equals("DROP DATABASES")){
                        System.out.println("La commande choisie est " + texteCommande);
                        ProcessDropDatabasesCommand(texteCommande); // Supprimer toutes les bases de données
                    }   else {
                        System.out.println("La commande choisie est " + texteCommande);
                        ProcessDropDatabaseCommand(texteCommande); // Supprimer une base de données
                    }
                } else {
                    System.out.println("Vous n'avez pas tapé le nom de la database");
                }
            }  else if (texteCommande.contains("QUIT")) {
                System.out.println("La commande choisie est " + texteCommande);
                quit = true;
                ProcessQuitCommand(texteCommande); // Quitter le SGBD
            } else {
                System.out.println("Vous avez tapé une mauvaise commande");
            }
        }
        System.out.println("Vous allez quitter le SGBD");
    }

    public void ProcessCreateDatabaseCommand(String texteCommande) {
        String[] tok = texteCommande.trim().split("CREATE DATABASE ");
        if (tok.length > 1) {
            String nomDB = tok[1].trim();
            System.out.println("Le nom de la database : " + nomDB + " La taille est : " + nomDB.length());
            dbManager.CreateDatabase(nomDB);
            for (String key : dbManager.getDatabases().keySet()) {
                System.out.println("Le nom de la database : " + key + " La taille est : " + key.length());
            }
        } else {
            System.out.println("Commande invalide ou nom de base de données manquant.");
        }

    }

    // Traitement de la commande SET DATABASE
    public void ProcessSetDatabaseCommand(String texteCommande) {
        String[] tok = texteCommande.trim().split("SET DATABASE ");
        String nomDB = tok[1];
        System.out.println("Le nom de la database : " + nomDB + " La taille est : " + nomDB.length());
        dbManager.setCurrentDatabase(nomDB); // Sélection de la base de données
        for (String key : dbManager.getDatabases().keySet()) {
            System.out.println("Le nom de la database : " + key + " La taille est : " + key.length());
        }
    }

    // Traitement de la commande CREATE TABLE
    public void ProcessCreateTableCommand(String texteCommande) throws IOException {
        Relation r;
        String caracAsupp = "(:,)";
        System.out.println("La commande avant :" + texteCommande);

        for (char c : caracAsupp.toCharArray()) {
            texteCommande = texteCommande.replace(String.valueOf(c), " ");
        }

        texteCommande = texteCommande.replace("CREATE TABLE ", "");
        System.out.println("La commande après :" + texteCommande);
        StringTokenizer stz = new StringTokenizer(texteCommande, " ");
        List<ColInfo> infoColonne = new ArrayList<>();
        String nomTab = stz.nextToken();

        while (stz.hasMoreTokens()) {
            String nom = stz.nextToken();
            System.out.println("Le nom : " + nom);
            String type = stz.nextToken();
            System.out.println("Le type : " + type);
            if (type.equals("REAL") || type.equals("INT")) {
                infoColonne.add(new ColInfo(nom, type, 4));
            } else if (type.equals("VARCHAR") || type.equals("CHAR")) {
                int tailleCol = 2 * Integer.parseInt(stz.nextToken());
                System.out.println("La taille : " + tailleCol);
                infoColonne.add(new ColInfo(nom, type, tailleCol));
            } else {
                System.out.println("Le type n'existe pas\nRedirection au Menu SGBD");
            }
        }
        PageId headerPage = ajouteHeaderPage(this.diskManager, this.bufferManager);

        ColInfo[] colonnesArray = infoColonne.toArray(new ColInfo[0]);
        r = new Relation(nomTab, colonnesArray.length, colonnesArray, headerPage, this.diskManager, this.bufferManager);
        r.addDataPage();
        this.dbManager.addTableToCurrentDatabase(r);
    }

    // Traitement de la commande LIST TABLES
    public void ProcessListTablesCommand(String texteCommande) {
        this.dbManager.ListTableInCurrentDatabase(); // Liste des tables dans la base de données courante
    }

    // Traitement de la commande LIST DATABASES
    public void ProcessListDatabasesCommand(String texteCommande) {
        this.dbManager.ListDatabases(); // Liste des bases de données
    }

    // Traitement de la commande DROP TABLE
    public void ProcessDropTableCommand(String texteCommande) {
        String[] tok = texteCommande.trim().split("DROP TABLE ");
        String nomtable = tok[1];
        Collection<Relation> tables = this.dbManager.getTables();
        boolean tableFound = false;
        for (Relation r : tables) {
            if (r.getNom().equals(nomtable)) {
                tableFound = true;
                // Libération des pages de données associées à la table
                for (PageId dataPage : r.getDataPages()) {
                    this.diskManager.DeallocPage(dataPage); // Libération de la page de données
                }
                this.diskManager.DeallocPage(r.getHeaderPageId()); // Libération de la page d'en-tête

                // Suppression de la table de la base de données courante
                this.dbManager.RemoveTableFromCurrentDatabase(nomtable);
                System.out.println("Table " + nomtable + " supprimée avec succès.");
                break;
            }
        }
        if (!tableFound) {
            System.err.println("Table " + nomtable + " non trouvée dans la base de données.");
        }
    }

    /*public void ProcessDropDatabaseCommand(String texteCommande) {
        if (texteCommande.trim().startsWith("DROP DATABASE")) {
            String[] tok = texteCommande.trim().split("DROP DATABASE");
            if (tok.length > 1) {
                String nombdd = tok[2].trim();
                System.out.println("NOM BDD : " + nombdd); // Récupération du nom de la base de données
                System.out.println("Le nom de la base de données est : " + nombdd);
                Database db = dbManager.getDatabases().get(nombdd);
                if (db != null) {
                    for (Relation r : db.getTables()) {
                        for (PageId datapage : r.getDataPages()) {
                            this.diskManager.DeallocPage(datapage); // Libération des pages de données
                        }
                        this.diskManager.DeallocPage(r.getHeaderPageId()); // Libération de la page d'en-tête
                    }
                    //dbManager.RemoveDatabase(nombdd); Suppression de la base de données
                    dbManager.RemoveDatabase();
                    System.out.println("La base de données " + nombdd + " a bien été supprimée.");
                } else {
                    System.err.println("Base de données " + nombdd + " non trouvée.");
                }
            } else {
                System.err.println("Commande DROP DATABASE incorrecte, aucun nom de base fourni.");
            }
        }
    }*/

    public void ProcessDropDatabaseCommand(String texteCommande) {
        if (texteCommande.trim().startsWith("DROP DATABASE")) {
            String nombdd = texteCommande.trim().substring("DROP DATABASE".length());
            System.out.println("NOM BDD : " + texteCommande);
            if (!nombdd.isEmpty()) {
                System.out.println("Le nom de la base de données est : " + nombdd);
                Database db = dbManager.getDatabases().get(nombdd);

                if (db != null) {
                    for (Relation r : db.getTables()) {
                        for (PageId datapage : r.getDataPages()) {
                            this.diskManager.DeallocPage(datapage);
                        }
                        this.diskManager.DeallocPage(r.getHeaderPageId()); // Libération de la page d'en-tête
                    }
                    // Suppression de la base de données
                    dbManager.RemoveDatabase();
                    System.out.println("La base de données " + nombdd + " a bien été supprimée.");
                } else {
                    System.err.println("Base de données " + nombdd + " non trouvée.");
                }
            } else {
                System.err.println("Commande DROP DATABASE incorrecte, aucun nom de base fourni.");
            }
        }
    }

    public void ProcessDropDatabasesCommand(String texteCommande){
        for(String key : this.dbManager.getDatabases().keySet()){
            for(Relation r : dbManager.getDatabases().get(key).getTables()){
                for(PageId datapage : r.getDataPages()){
                    this.diskManager.DeallocPage(datapage);
                }
                this.diskManager.DeallocPage(r.getHeaderPageId());
            }
        ProcessDropDatabaseCommand("DROP DATABASE "+key);
        }
        //this.dbManager.RemoveDatabases();
    }

    // Traitement de la commande QUIT
    public void ProcessQuitCommand(String texteCommande) throws IOException {
        dbManager.SaveState(); // Sauvegarde de l'état de la base de données
        diskManager.saveState(); // Sauvegarde de l'état du gestionnaire de disque
    }

    public static PageId ajouteHeaderPage(DiskManager diskManager, BufferManager bufferManager) throws IOException {
        System.out.println("**************  Initialisation d'une headerPage   *********************");
        PageId headerPage = diskManager.AllocPage(); // On alloue une page disponible
        MyBuffer buff = bufferManager.getPage(headerPage);

        System.out.println("La header page est placée en " + headerPage);

        int position = (int) (headerPage.getPageIdx() * diskManager.getDBConfig().getPagesize());
        String cheminFichier = diskManager.getDBConfig().getDbPath() + "/F" + headerPage.getFileIdx() + ".rsdb"; // Chemin du fichier à lire
        File fichier = new File(cheminFichier);

        if (fichier.exists()) {
            try (RandomAccessFile raf = new RandomAccessFile(fichier, "rw")) {
                raf.seek(position);  // Positionnement sur le premier octet de la page voulue
                System.out.println(raf.readInt());
                System.out.println(raf.readInt());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                bufferManager.freePage(headerPage, false);
            }
        } else {
            System.out.println("Vous tentez de lire un fichier qui n'existe pas");
        }

        return headerPage;
    }

    /*PageId headerPage = ajouteHeaderPage(this.diskManager, this.bufferManager);

    ColInfo[] colonnesArray = infoColonne.toArray(new ColInfo[0]);
    Relation r = new Relation(nomTab, colonnesArray.length, colonnesArray, headerPage, this.diskManager, this.bufferManager);
    r.addDataPage();

    this.dbManager.addTableToCurrentDatabase(r);*/

}
