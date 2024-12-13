import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


public class SGBD {

    private DBConfig dbConfig;
    private DiskManager diskManager;
    private BufferManager bufferManager;
    private DBManager dbManager;

    public SGBD(DBConfig dbconfig) throws IOException{
        this.dbConfig = dbconfig;
        this.diskManager = new DiskManager(dbconfig);
        this.bufferManager = new BufferManager(this.dbConfig, this.diskManager);
        this.dbManager = new DBManager(dbconfig, diskManager, bufferManager);
        this.diskManager.loadState();
        this.dbManager.LoadState();
    }

    // Getters & Setters
    public DBConfig getDbConfig() {
        return dbConfig;
    }
    public void setDbConfig(DBConfig dbConfig) {
        this.dbConfig = dbConfig;
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

    public DBManager getDbManager() {
        return dbManager;
    }
    public void setDbManager(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    public void Run()     {
        System.out.println("********************    Bienvenue dans votre SGBD   ********************");
        String texteCommande;
        boolean quit = false;
        Scanner sc = new Scanner(System.in);

        while (!quit) {
            System.out.println("Veuillez taper votre commande :  ");
            texteCommande = sc.nextLine();
            texteCommande = texteCommande.replaceAll("\\s+", " ").trim();
            String start;
            pr:
            if (true){
                if (texteCommande.toUpperCase().startsWith("CREATE DATABASE "))   {
                    try {
                        ProcessCreateDataBaseCommand(texteCommande);
                    }   catch (IOException e)   {
                        System.out.println(e.getMessage());
                    }
                    break pr;
                }
                if (texteCommande.toUpperCase().startsWith("CREATE TABLE ")) {
                    try {
                        ProcessCreateTableCommand(texteCommande);
                    }   catch (IOException e)   {
                        System.out.println(e.getMessage());
                    }
                    break pr;
                }
                if (texteCommande.toUpperCase().startsWith("SET DATABASE "))     {
                    try {
                        ProcessSetDataBaseCommand(texteCommande);
                    }   catch (IOException e)   {
                        System.out.println(e.getMessage());
                    }
                    break pr;
                }
                if (texteCommande.toUpperCase().equals("LIST TABLES"))  {
                    ProcessListTablesCommand(texteCommande);
                    break pr;
                }
                if (texteCommande.toUpperCase().equals("LIST DATABASES"))   {
                    ProcessListDataBasesCommand(texteCommande);
                    break pr;
                }
                if (texteCommande.toUpperCase().startsWith("DROP TABLE "))   {
                    try {
                        ProcessDropTableCommand(texteCommande);
                    }   catch (IOException e)   {
                        System.out.println(e.getMessage());
                    }
                    break pr;
                }
                if (texteCommande.toUpperCase().equals("DROP TABLES"))   {
                    ProcessDropTablesCommand(texteCommande);
                    break pr;
                }
                if (texteCommande.toUpperCase().equals("DROP DATABASES"))   {
                    ProcessDropDataBasesCommand(texteCommande);
                    break pr;
                }
                if (texteCommande.toUpperCase().startsWith("DROP DATABASE "))    {
                    try {
                        ProcessDropDataBaseCommand(texteCommande);
                    }   catch (IOException e)   {
                        System.out.println(e.getMessage());
                    }
                    break pr;
                }
                if (texteCommande.toUpperCase().startsWith("INSERT INTO ")) {
                    try    {
                        ProcessInsertIntoCommand(texteCommande);
                    }   catch (IOException e)   {
                        System.out.println(e.getMessage());
                    }
                    break pr;
                }
                if (texteCommande.toUpperCase().startsWith("BULKINSERT INTO ")) {
                    try{
                        ProcessBulkinsertIntoCommand(texteCommande);
                    }   catch (IOException e)   {
                        System.out.println(e.getMessage());
                    }
                    break pr;
                }
                if (texteCommande.toUpperCase().equals("QUIT")) {
                    ProcessQuitCommand(texteCommande);
                    quit = true;
                    break;
                }
                System.err.println("Erreur : Erreur de syntaxe dans " + texteCommande + " !!");
            }
        }
        System.out.println("********************    Au revoir !   ********************");
        sc.close();
    }

    public void ProcessCreateDataBaseCommand(String texteCommande) throws IOException {
        String [] splitCommand = texteCommande.split(" ");
        if (splitCommand.length==3) {
            this.dbManager.CreateDatabase(splitCommand[2]);
        }   else {
            throw new IOException("Erreur de syntaxe dans la commande CREATE DATABASE. Syntaxe à suivre : \"CREATE DATABASE X\" où X est remplacé par le nom de la nouvelle base de données.");
        }
    }

    public void ProcessCreateTableCommand(String texteCommande)  throws IOException {
        if (this.dbManager.getActiveDB()==null) {
            throw new IOException("Erreur : Aucune base de données active.");
        }
        String [] splitCommand = texteCommande.split(" ");
        if (splitCommand.length==4) {
            String tableName = splitCommand[2];
            String[] colonnesEntry = splitCommand[3].substring(1, splitCommand[3].length()-1).split(",");
            ColInfo[] colonnes = new ColInfo[colonnesEntry.length];
            for (int i=0; i<colonnesEntry.length; i++)  {
                String[] nameAndType = colonnesEntry[i].split(":");
                if (nameAndType.length==2){
                    String nom = nameAndType[0];
                    int taille = 1;
                    String type = null;
                    if (nameAndType[1].toUpperCase().startsWith("CHAR(") || nameAndType[1].toUpperCase().startsWith("VARCHAR("))    {
                        type = nameAndType[1].substring(0, nameAndType[1].indexOf("(")).toUpperCase();
                        if (nameAndType[1].indexOf("(")==-1 || nameAndType[1].indexOf(")")==-1)  {
                            throw new IOException("Erreur de syntaxe dans " + nameAndType[1] + " ! précisez la taille du " + type + " !!");
                        }
                        try{
                            taille = Integer.parseInt( nameAndType[1].substring( nameAndType[1].indexOf("(")+1 , nameAndType[1].indexOf(")") ) );
                        } catch(NumberFormatException e)    {
                            System.err.println("Erreur de syntaxe dans " + nameAndType[1] + " ! la partie entre parenthèses doit correspondre à la taille du "+ type+ " et doit être un entier.");
                        }
                    }   else if (nameAndType[1].toUpperCase().equals("INT"))    {
                        type = "INT";
                    }   else if (nameAndType[1].toUpperCase().equals("REAL"))   {
                        type = "REAL";
                    }   else{
                        throw new IOException("Erreur de syntaxe : le type \"" + nameAndType[1] + "\" n'est pas géré par notre SGBD");
                    }
                    colonnes[i] = new ColInfo(nom, type, taille);
                }  else {
                    throw new IOException("Erreur de syntaxe dans \"" + colonnesEntry[i] + "\"");
                }
            }
            Relation table = new Relation(tableName, colonnesEntry.length, colonnes, null, this.diskManager, this.bufferManager);
            table.initializeHeaderPage();
            this.dbManager.AddTableToCurrentDatabase(table);
        }   else {
            throw new IOException("Erreur de syntaxe dans la commande CREATE TABLE. Syntaxe à suivre : \"CREATE TABLE X (C1:T1,C2:T2(4),C3:T3)\" \noù X est remplacé par le nom de la nouvelle table, C1 le nom du premier attribut, T1 son type (INT,REAL,CHAR(n),VARCHAR(n))...");
        }
    }

    public void ProcessSetDataBaseCommand(String texteCommande)  throws IOException    {
        String [] splitCommand = texteCommande.split(" ");
        if (splitCommand.length==3) {
            this.dbManager.SetCurrentDatabase(splitCommand[2]);
        }   else {
            throw new IOException("Erreur de syntaxe dans la commande SET DATABASE. Syntaxe à suivre : \"SET DATABASE X\" où X est remplacé par le nom d'une base de données existante.");
        }
    }

    public void ProcessListTablesCommand(String texteCommande)      {
        this.dbManager.ListTablesInCurrentDatabase();
    }

    public void ProcessListDataBasesCommand(String texteCommande)   {
        this.dbManager.ListDatabasesNames();
    }

    public void ProcessDropTableCommand (String texteCommande) throws IOException {
        String [] splitCommand = texteCommande.split(" ");
        if (splitCommand.length==3) {
            this.dbManager.RemoveTableFromCurrentDatabase(splitCommand[2]);
        }   else {
            throw new IOException("Erreur de syntaxe dans la commande DROP TABLE. Syntaxe à suivre : \"DROP TABLE X\" où X est remplacé par le nom de la table à supprimer.");
        }
    }

    public void ProcessDropTablesCommand (String texteCommande)     {
        this.dbManager.RemoveTablesFromCurrentDatabase();
    }

    public void ProcessDropDataBasesCommand (String texteCommande)  {
        this.dbManager.RemoveDatabases();
    }

    public void ProcessDropDataBaseCommand (String texteCommande) throws IOException   {
        String [] splitCommand = texteCommande.split(" ");
        if (splitCommand.length==3) {
            this.dbManager.RemoveDatabase(splitCommand[2]);
        }   else {
            throw new IOException("Erreur de syntaxe dans la commande DROP DATABASE. Syntaxe à suivre : \"DROP DATABASE X\" où X est remplacé par le nom de la base de données à supprimer.");
        }
    }

    public void ProcessInsertIntoCommand (String texteCommande) throws IOException {
        if (this.dbManager.getActiveDB() == null)   {
            throw new IOException("Erreur : Aucune base de données active.");
        }
        String [] splitCommand = texteCommande.split(" ");
        if (splitCommand.length == 5 && splitCommand[3].toUpperCase().equals("VALUES"))   {
            String tableName = splitCommand[2];
            // Vérifions si la table existe bien dans la bdd active
            if (! this.dbManager.getActiveDB().containsTable(tableName))    {
                throw new IOException("Erreur : la table (" + tableName + ") n'existe pas dans la base de données actuellement active {" + this.dbManager.getActiveDB().getNom() + "}");
            }
            Relation table = this.dbManager.getActiveDB().getTable(tableName);
            String [] values = splitCommand[4].substring(1, splitCommand[4].length()-1).split(",");
            if (table.getNbColonnes() == values.length)    {
                String [] recordValues = new String[values.length];
                for (int i=0; i<values.length;  i+=1)   {
                    String type = table.getColonnes()[i].getType();
                    // On vérifie si les entrées correspondent aux types prédéfinies dans la relation
                    switch (type)   {
                        case "INT" :
                            try {
                                int myInt = Integer.parseInt(values[i]);
                            }   catch (NumberFormatException e) {
                                throw new IOException("Erreur : l'attribut n°" + (i+1) + " devrait être un INT.");
                            }
                            recordValues[i] = values[i];
                            break;
                        case "REAL" :
                            try {
                                double myFloat = Double.parseDouble(values[i]);
                            }   catch(NumberFormatException e)  {
                                throw new IOException("Erreur : l'attribut n°" + (i+1) + " devrait être un REAL.");
                            }
                            recordValues[i] = values[i];
                            break;
                        case "CHAR" :
                            if (values[i].startsWith("\"") && values[i].endsWith("\"")) {
                                int taille = table.getColonnes()[i].getTaille();
                                if (values[i].length()==taille+2) {
                                    recordValues[i] = values[i].substring(1, values[i].length()-1);
                                }   else{
                                    throw new IOException("Erreur : l'attribut "+ values[i] + " doit être de taille " + taille + " !");
                                }
                            }   else{
                                throw new IOException("Erreur de syntaxe : un attribut de type CHAR doit être mis entre guillemets");
                            }
                            break;
                        case "VARCHAR" :
                            if (values[i].startsWith("\"") && values[i].endsWith("\"")) {
                                int taille = table.getColonnes()[i].getTaille();
                                if (values[i].length()<=taille+2) {
                                    recordValues[i] = values[i].substring(1, values[i].length()-1);
                                }   else{
                                    throw new IOException("Erreur : l'attribut "+ values[i] + " ne doit pas dépasser la taille " + taille + " !");
                                }
                            }   else{
                                throw new IOException("Erreur de syntaxe : un attribut de type VARCHAR doit être mis entre guillemets");
                            }
                            break;
                        default:
                            break;
                    }
                }
                Record recordToInsert = new Record(recordValues);
                RecordId insertion = table.InsertRecord(recordToInsert);
                if (insertion!=null)    {
                    System.out.println("Insertion d'un record effectuée avec succées dans la table (" + tableName + ")");
                }   else    {
                    throw new IOException("Insertion échouée ! Erreur produite lors de l'insertion du record.");
                }
            }   else {
                throw new IOException("Erreur : Le nombre d'attributs en entrée ne correspond pas au nombre de colonnes dans la table (" + tableName + ")");
            }
        }   else    {
            throw new IOException("Erreur de syntaxe dans la commande INSERT INTO. Syntaxe à suivre : \"INSERT INTO X VALUES (V1,V2,V3)\" \noù X est remplacé par le nom de la table; et V1,V2,V3 correspondent aux valeurs d'un n-uplet de la table, qui seront listés dans le même ordre choisi lors de la création de la table.");
        }
    }

    public void ProcessBulkinsertIntoCommand (String texteCommande) throws IOException {
        if (this.dbManager.getActiveDB() == null)   {
            throw new IOException("Erreur : Aucune base de données active.");
        }
        String[] splitCommand = texteCommande.split(" ");
        if (splitCommand.length==4) {
            String tableName = splitCommand[2];
            if (! this.dbManager.getActiveDB().containsTable(tableName))  {
                throw new IOException("Erreur : la table (" + tableName + ") n'existe pas dans la base de données actuellement active {" + this.dbManager.getActiveDB().getNom() + "}");
            }
            if (! splitCommand[3].endsWith(".csv"))   {
                throw new IOException("Erreur : le fichier en entrée doit être un fichier CSV d'extention .csv");
            }
            File file = new File ("./src/main/data/"+splitCommand[3]);
            if (!file.exists())  {
                throw new IOException("Erreur : le fichier en entrée n'existe pas dans l'arborescence \"./src/data\" de ce projet.");
            }
            try {
                FileReader fileReader = new FileReader(file);
                BufferedReader buffer = new BufferedReader(fileReader);
                String line = null;
                while ((line=buffer.readLine()) != null)    {
                    String query = "INSERT INTO " + tableName + " VALUES (" + line + ")";
                    this.ProcessInsertIntoCommand(query);
                }
                buffer.close();
            }   catch(FileNotFoundException e)  {
                throw new IOException("Erreur : tentative d'ouverture du fichier ("+ splitCommand[3] + ") échouée !");
            }
        }   else    {
            throw new IOException("Erreur de syntaxe dans la commande BULKINSERT INTO. Syntaxe à suivre : \"BULKINSERT INTO X Y.csv\noù X est remplacé par le nom de la table; et Y correspondant au nom du fichier qui contient les records, le fichier doit être placé dans le dossier \"/src/data\" du projet.");
        }
    }

    /*public void processSelectCommand(String textecommande) {
        // Étape 1 : Analyser la commande
        // Diviser la commande en parties SELECT, FROM et WHERE (si présente)
        String[] parts = textecommande.split("FROM");
        if (parts.length != 2) {
            System.err.println("Commande SELECT mal formée");
            return;
        }

        String selectPart = parts[0].trim();
        String fromPart = parts[1].trim();

        // Analyser la partie SELECT
        String[] selectTokens = selectPart.replace("SELECT", "").trim().split(",");
        List<String> columnsToProject = new ArrayList<>();
        boolean selectAllColumns = false;

        if (selectTokens.length == 1 && selectTokens[0].trim().equals("*")) {
            selectAllColumns = true; // Si SELECT * alors on sélectionne toutes les colonnes
        } else {
            // Ajouter les colonnes à projeter
            columnsToProject.addAll(Arrays.asList(selectTokens));
        }

        // Extraire le nom de la relation et l'alias
        String[] fromTokens = fromPart.split("WHERE");
        String relationAlias = fromTokens[0].trim().split(" ")[0]; // Extrait l'alias de la relation
        String relationName = fromTokens[0].trim().split(" ")[1]; // Extrait le nom de la relation

        // On analyse la partie WHERE
        ArrayList<Object> conditions = new ArrayList<>();
        if (fromTokens.length > 1) {

            String whereClause = fromTokens[1].trim();
            String[] conditionTokens = whereClause.split("AND");

            for (String conditionString : conditionTokens) {
                String[] conditionParts = conditionString.trim().split(" ");
                if (conditionParts.length != 3) {
                    throw new IllegalArgumentException("Condition mal formée : " + conditionString);
                }
                String column = conditionParts[0].trim();
                String operator = conditionParts[1].trim();
                String value = conditionParts[2].trim();

                int columnIndex = Integer.parseInt(column.replace(relationAlias + ".", "").replace("col", "")) - 1;
                conditions.add(String.valueOf(new Condition(columnIndex, value, operator, "VARCHAR")));
            }
        }

        RelationScanner relationScanner = new RelationScanner(relationName);
        SelectOperator selectOperator = new SelectOperator(relationScanner, conditions);

        if (selectAllColumns) {
            ProjectOperator projectOperator = new ProjectOperator(selectOperator);
            Record record;
            int count = 0;

            // On affiche les résultats
            while ((record = projectOperator.getNextRecord()) != null) {
                count++;
                System.out.print(record.toString().replace(" | ", " ; ").replace("[", "").replace("]", ""));
                System.out.println(".");
            }
            System.out.println("Total records=" + count);
        } else {
            ArrayList<Object> columnIndexes = new ArrayList<>();
            for (String col : columnsToProject) {
                columnIndexes.add(String.valueOf(Integer.parseInt(col.replace(relationAlias + ".", "").replace("col", "")) - 1));
            }

            ProjectOperator projectOperator = new ProjectOperator(selectOperator);
            Record record;
            int count = 0;

            // On affiche les résultats de la requête
            while ((record = projectOperator.getNextRecord()) != null) {
                count++;
                System.out.print(record.toString().replace(" | ", " ; ").replace("[", "").replace("]", ""));
                System.out.println(".");
            }
            System.out.println("Total records=" + count);
        }
    }*/


    public void ProcessQuitCommand (String texteCommande)   {
        this.bufferManager.flushBuffers();
        this.diskManager.saveState();
        this.dbManager.SaveState();
    }


    public static void main (String [] args) throws IOException {
        DBConfig config = DBConfig.loadDBConfig("src/main/java/config.json");
        SGBD mySGBD = new SGBD(config);
        mySGBD.Run();
    }
}

