import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;



public class SGBD {
    
    private DBConfig dbConfig;
    private DiskManager diskManager;
    private BufferManager bufferManager;
    private DBManager dbManager;

    public SGBD(DBConfig dbconfig)  {
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
            System.out.println("Veuillez tapez votre commande :  ");
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
                if (texteCommande.toUpperCase().startsWith("SELECT "))  {
                    try {
                        ProcessSelectCommand(texteCommande);
                    }   catch(IOException e)    {
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
            this.dbManager.CreateDatabase(splitCommand[2].toUpperCase());
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
            String tableName = splitCommand[2].toUpperCase();
            String[] colonnesEntry = splitCommand[3].substring(1, splitCommand[3].length()-1).split(",");
            ColInfo[] colonnes = new ColInfo[colonnesEntry.length];
            for (int i=0; i<colonnesEntry.length; i++)  {
                String[] nameAndType = colonnesEntry[i].split(":");
                if (nameAndType.length==2){
                    String nom = nameAndType[0].toLowerCase();
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
            this.dbManager.SetCurrentDatabase(splitCommand[2].toUpperCase());
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
            this.dbManager.RemoveTableFromCurrentDatabase(splitCommand[2].toUpperCase());
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
            this.dbManager.RemoveDatabase(splitCommand[2].toUpperCase());
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
            String tableName = splitCommand[2].toUpperCase();
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
            String tableName = splitCommand[2].toUpperCase();
            if (! this.dbManager.getActiveDB().containsTable(tableName))  {
                throw new IOException("Erreur : la table (" + tableName + ") n'existe pas dans la base de données actuellement active {" + this.dbManager.getActiveDB().getNom() + "}");
            }
            if (! splitCommand[3].endsWith(".csv"))   {
                throw new IOException("Erreur : le fichier en entrée doit être un fichier CSV d'extention .csv");
            }
            File file = new File ("./src/data/"+splitCommand[3]);
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

    public void ProcessQuitCommand (String texteCommande)   {
        this.bufferManager.flushBuffers();
        this.diskManager.saveState();
        this.dbManager.SaveState();
    }

    public void ProcessSelectCommand(String textecommande)  throws IOException  {
        String [] repartition1 = textecommande.toUpperCase().split(" ");
        if (repartition1.length<5  ||  !repartition1[2].equals("FROM"))   {
            throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT. La commande doit obligatoirement contenir les mots \"SELECT et FROM\" en troisième position. (à la position du from on a :"+repartition1[2]+ ")");
        }

        String tableName = repartition1[3];
        if (! this.dbManager.getActiveDB().containsTable(tableName))  {
            throw new IOException("Erreur : la table (" + tableName + ") n'existe pas dans la base de données actuellement active {" + this.dbManager.getActiveDB().getNom() + "}");
        }

        Relation table = this.dbManager.getActiveDB().getTable(tableName);
        String tableAlias = repartition1[4];

        ArrayList<Integer> projectionIndexs = new ArrayList<>();
        if (repartition1[1].equals("*"))    {
            for (int i=0; i<table.getNbColonnes(); i+=1)    {
                projectionIndexs.add(i);
            }
        }   else    {
            String [] repartition2 = repartition1[1].split(",");
            if (repartition2.length>table.getNbColonnes()) {
                throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + textecommande.split(" ")[1] + "\", consultez la documentation pour en savoir plus.");
            }
            for (int i=0; i<repartition2.length; i+=1)  {
                String[] repartition3 = repartition2[i].split("\\.");
                if (repartition3.length != 2)   {
                    throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + textecommande.split(" ")[1] + "\", consultez la documentation pour en savoir plus.");
                }
                if (!repartition3[0].equals(tableAlias))    {
                    throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + textecommande.split(" ")[1] + "\", Pensez à garder le même alias pour la table durant toute la requête.");
                }
                String attributeName = repartition3[1].toLowerCase();
                String indexAndTypeOfAttribute = table.getIndexAndTypeOfAttribute(attributeName);
                if (indexAndTypeOfAttribute.equals("")) {
                    throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT, la table (" + tableName + ") ne contient aucun attribut s'appelant \"" + attributeName + "\" !!");
                }
                projectionIndexs.add(Integer.parseInt(indexAndTypeOfAttribute.split(";")[0]));
            }
        }

        ArrayList<Condition> conditions = new ArrayList<>();
        if (repartition1.length>5)  {
            if (!repartition1[5].equals("WHERE"))   {
                throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT, la position 6 de cette commande est résérvée pour le terme WHERE pour exprimer une condition.");
            }
            String where = textecommande.split(" ")[5];
            String [] repartition4 = textecommande.split(where);
            if (repartition4.length!=2)    {
                throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT, la requête doit contenir au minimum une seule condition après \"WHERE\" !");
            }
            repartition4 = repartition4[1].trim().split(" ");
            for (int i=0; i<repartition4.length; i+=1)     {
                if (i%2==0) {
                    String conditionString = repartition4[i];
                    String operateur = "";
                    if (conditionString.contains("!=")) {
                        operateur = "!=";
                    }   else if (conditionString.contains("<="))  {
                        operateur = "<=";
                    }   else if (conditionString.contains(">="))    {
                        operateur = ">=";
                    }   else if (conditionString.contains("=")) {
                        operateur = "=";
                    }   else if (conditionString.contains("<")) {
                        operateur = "<";
                    }   else if (conditionString.contains(">")) {
                        operateur = ">";
                    }
                    if (operateur.equals(""))  {
                        throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", les seuls opérateurs autorisés sont :  =  !=  <  >  <=  >=");
                    }
                    String [] repartition5 = conditionString.split(operateur);
                    if (repartition5.length!=2) {
                        throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", veuillez regarder la documentation..!");
                    }
                    if (repartition5[1].contains(".") && !repartition5[1].contains("."))  {
                        String aux = repartition5[0];
                        repartition5[0] = repartition5[1];
                        repartition5[1] = aux;
                    }
                    String [] repartition6 = repartition5[0].toUpperCase().split("\\.");
                    if (repartition6.length!=2)   {
                        throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", veuillez regarder la documentation..!");
                    }
                    if (!repartition6[0].equals(tableAlias))    {
                        throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", Pensez à garder le même alias pour la table durant toute la requête.");
                    }
                    String indexAndType = table.getIndexAndTypeOfAttribute(repartition6[1].toLowerCase());
                    String valeurConstante = repartition5[1];
                    if (indexAndType.endsWith("CHAR") || indexAndType.endsWith("VARCHAR"))  {
                        if (!(valeurConstante.startsWith("\"") && valeurConstante.endsWith("\"")))  {
                            throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", Pensez à mettre les constantes CHAR et VARCHAR entre guillemets \"constante\".");
                        }
                        valeurConstante = valeurConstante.substring(1,valeurConstante.length()-1);
                    }   else if (indexAndType.endsWith("INT"))  {
                        try {
                            Integer.parseInt(valeurConstante);
                        }   catch (NumberFormatException e) {
                            throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", Pensez à lire la documentation.");
                        }
                    }   else {
                        try {
                            Double.parseDouble(valeurConstante);
                        }   catch (NumberFormatException e) {
                            throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", Pensez à lire la documentation.");
                        }
                    }
                    Condition condition = new Condition(valeurConstante, Integer.parseInt(indexAndType.split(";")[0]), operateur, indexAndType.split(";")[1]);
                    conditions.add(condition);

                }   else if (!repartition4[i].toUpperCase().equals("AND"))  {
                    throw new IOException("Erreur : le seul operateur possible entre les conditions est l'opérateur logique de conjenction AND !");
                }
            }
        }

        RecordPrinter recordPrinter = new RecordPrinter(table, conditions, projectionIndexs);
        recordPrinter.printRecords();
    }

    /*public void ProcessSelectCommand(String textecommande)  throws IOException  {
        String [] repartition1 = textecommande.toUpperCase().split(" ");
        if (repartition1.length<5  ||  !repartition1[2].equals("FROM"))   {
            throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT. La commande doit obligatoirement contenir les mots \"SELECT et FROM\" en troisième position.");
        }
        String tableName = repartition1[3];
        if (! this.dbManager.getActiveDB().containsTable(tableName))  {
            throw new IOException("Erreur : la table (" + tableName + ") n'existe pas dans la base de données actuellement active {" + this.dbManager.getActiveDB().getNom() + "}");
        }
        Relation table = this.dbManager.getActiveDB().getTable(tableName);
        String tableAlias = repartition1[4];

        ArrayList<Integer> projectionIndexs = new ArrayList<>();
        if (repartition1[1].equals("*"))    {
            for (int i=0; i<table.getNbColonnes(); i+=1)    {
                projectionIndexs.add(i);
            }
        }   else    {
            String [] repartition2 = repartition1[1].split(",");
            if (repartition2.length>table.getNbColonnes()) {
                throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + textecommande.split(" ")[1] + "\", consultez la documentation pour en savoir plus.");
            }
            for (int i=0; i<repartition2.length; i+=1)  {
                String[] repartition3 = repartition2[i].split("\\.");
                if (repartition3.length != 2)   {
                    throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + textecommande.split(" ")[1] + "\", consultez la documentation pour en savoir plus.");
                }
                if (!repartition3[0].equals(tableAlias))    {
                    throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + textecommande.split(" ")[1] + "\", Pensez à garder le même alias pour la table durant toute la requête.");
                }
                String attributeName = repartition3[1].toLowerCase();
                String indexAndTypeOfAttribute = table.getIndexAndTypeOfAttribute(attributeName);
                if (indexAndTypeOfAttribute.equals("")) {
                    throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT, la table (" + tableName + ") ne contient aucun attribut s'appelant \"" + attributeName + "\" !!");
                }
                projectionIndexs.add(Integer.parseInt(indexAndTypeOfAttribute.split(";")[0]));
            }
        }

        ArrayList<Condition> conditions = new ArrayList<>();
        if (repartition1.length>5)  {
            if (!repartition1[5].equals("WHERE"))   {
                throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT, la position 6 de cette commande est résérvée pour le terme WHERE pour exprimer une condition.");
            }
            String where = textecommande.split(" ")[5];
            String [] repartition4 = textecommande.split(where);
            if (repartition4.length!=2)    {
                throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT, la requête doit contenir au minimum une seule condition après \"WHERE\" !");
            }
            repartition4 = repartition4[1].trim().split(" ");
            for (int i=0; i<repartition4.length; i+=1)     {
                if (i%2==0) {
                    String conditionString = repartition4[i];
                    String operateur = "";
                    if (conditionString.contains("!=")) {
                        operateur = "!=";
                    }   else if (conditionString.contains("<="))  {
                        operateur = "<=";
                    }   else if (conditionString.contains(">="))    {
                        operateur = ">=";
                    }   else if (conditionString.contains("=")) {
                        operateur = "=";
                    }   else if (conditionString.contains("<")) {
                        operateur = "<";
                    }   else if (conditionString.contains(">")) {
                        operateur = ">";
                    }
                    if (operateur.equals(""))  {
                        throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", les seuls opérateurs autorisés sont :  =  !=  <  >  <=  >=");
                    }
                    String [] repartition5 = conditionString.split(operateur);
                    if (repartition5.length!=2) {
                        throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", veuillez regarder la documentation..!");
                    }
                    if (repartition5[1].contains(".") && !repartition5[1].contains("."))  {
                        String aux = repartition5[0];
                        repartition5[0] = repartition5[1];
                        repartition5[1] = aux;
                    }
                    String [] repartition6 = repartition5[0].toUpperCase().split("\\.");
                    if (repartition6.length!=2)   {
                        throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", veuillez regarder la documentation..!");
                    }
                    if (!repartition6[0].equals(tableAlias))    {
                        throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", Pensez à garder le même alias pour la table durant toute la requête.");
                    }
                    String indexAndType = table.getIndexAndTypeOfAttribute(repartition6[1].toLowerCase());
                    String valeurConstante = repartition5[1];
                    if (indexAndType.endsWith("CHAR") || indexAndType.endsWith("VARCHAR"))  {
                        if (!(valeurConstante.startsWith("\"") && valeurConstante.endsWith("\"")))  {
                            throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", Pensez à mettre les constantes CHAR et VARCHAR entre guillemets \"constante\".");
                        }
                        valeurConstante = valeurConstante.substring(1,valeurConstante.length()-1);
                    }   else if (indexAndType.endsWith("INT"))  {
                        try {
                            Integer.parseInt(valeurConstante);
                        }   catch (NumberFormatException e) {
                                throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", Pensez à lire la documentation.");
                        }
                    }   else {
                        try {
                            Double.parseDouble(valeurConstante);
                        }   catch (NumberFormatException e) {
                                throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + conditionString + "\", Pensez à lire la documentation.");
                        }
                    }
                    Condition condition = new Condition(valeurConstante, Integer.parseInt(indexAndType.split(";")[0]), operateur, indexAndType.split(";")[1]);
                    conditions.add(condition);

                }   else if (!repartition4[i].toUpperCase().equals("AND"))  {
                    throw new IOException("Erreur : le seul operateur possible entre les conditions est l'opérateur logique de conjenction AND !");
                }
            }
        } 

        RecordPrinter recordPrinter = new RecordPrinter(table, conditions, projectionIndexs); 

        recordPrinter.printRecords();
    }

    public void ProcessSelectCommandJointure(String textecommande) throws Exception {

        String[] repartition1 = textecommande.toUpperCase().split(" ");
    
        // Vérification de la syntaxe de la commande
        if (repartition1.length < 5 || !repartition1[2].equals("FROM")) {
            throw new IOException("Erreur : La commande doit obligatoirement contenir les mots 'SELECT' et 'FROM'.");
        }

        // Extraction des tables à partir de "FROM"
        String[] tables = repartition1[3].split(",");
        if (tables.length != 2) {
            throw new IOException("Erreur : La commande SELECT doit contenir exactement deux tables après FROM.");
        }

        String tableName1 = tables[0].trim(); // Première table (R)
        String tableName2 = tables[1].trim(); // Deuxième table (S)

        // Vérification de l'existence des deux tables
        if (!this.dbManager.getActiveDB().containsTable(tableName1)) {
            throw new IOException("Erreur : La table " + tableName1 + " n'existe pas.");
        }
        if (!this.dbManager.getActiveDB().containsTable(tableName2)) {
            throw new IOException("Erreur : La table " + tableName2 + " n'existe pas.");
        }

        Relation table1 = this.dbManager.getActiveDB().getTable(tableName1);
        Relation table2 = this.dbManager.getActiveDB().getTable(tableName2);

        /*String tableAlias1 = repartition1[4]; // Alias de la première table (R)
        String tableAlias2 = repartition1[5]; // Alias de la deuxième table (S)

        ArrayList<Integer> projectionIndexs = new ArrayList<>();

        if (repartition1[1].equals("*")) {
            // Sélectionner toutes les colonnes des deux tables
            for (int i = 0; i < table1.getNbColonnes(); i++) {
                projectionIndexs.add(i); // Colonnes de la première table (R)
            }
            for (int i = 0; i < table2.getNbColonnes(); i++) {
                projectionIndexs.add(i); // Colonnes de la deuxième table (S)
            }
        } else {

            String[] projections = repartition1[1].split(",");  // Séparer les colonnes par ","

            if(projections.length >(table1.getNbColonnes()+table2.getNbColonnes())){
                throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + textecommande.split(" ")[1] + "\", consultez la documentation pour en savoir plus."); 
            }

            for (String projection : projections) {
                String[] partie = projection.split("\\.");  // Séparer l'alias de la colonne (ex: R.C3 -> R et C3)
                
                if (partie.length != 2) {
                    throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT dans la partie \"" + textecommande.split(" ")[1] + "\", consultez la documentation pour en savoir plus."); 
                }

                String alias = partie[0]; 
                String attributeName = partie[1].toLowerCase();

                if (alias.equals(tableName1)) {

                    String indexAndTypeOfAttribute = table1.getIndexAndTypeOfAttribute(attributeName);
                    if (indexAndTypeOfAttribute.equals("")) {
                        throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT, la table (" + alias + ") ne contient aucun attribut s'appelant \"" + attributeName + "\" !!");
                    }
                    projectionIndexs.add(Integer.parseInt(indexAndTypeOfAttribute.split(";")[0]));

                } else if (alias.equals(tableName2)) {

                    String indexAndTypeOfAttribute = table1.getIndexAndTypeOfAttribute(attributeName);
                    if (indexAndTypeOfAttribute.equals("")) {
                        throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT, la table (" + alias + ") ne contient aucun attribut s'appelant \"" + attributeName + "\" !!");
                    }
                    projectionIndexs.add(Integer.parseInt(indexAndTypeOfAttribute.split(";")[0]));

                } else {
                    throw new IOException("Erreur : L'alias de la table ne correspond pas.");
                }

            }

            ArrayList<Condition> conditions = new ArrayList<>();
            
            if (repartition1.length>5)  {
                if (!repartition1[5].equals("WHERE"))   {
                    throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT, la position 6 de cette commande est résérvée pour le terme WHERE pour exprimer une condition.");
                }
            }

            String where = textecommande.split(" ")[5];
            String [] apresWhere = textecommande.split(where);
            String[] conditionsArray = apresWhere[1].split("AND");
            if (apresWhere.length!=2)    {
                throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT, la requête doit contenir au minimum une seule condition après \"WHERE\" !");
            }
            apresWhere = apresWhere[1].trim().split(" ");

            for (String condition : conditionsArray) {

                String operator = "";
                if (condition.contains("=")) {
                    operator = "=";
                } else if (condition.contains(">=")) {
                    operator = ">=";
                } else if (condition.contains("<=")) {
                    operator = "<=";
                } else if (condition.contains(">")) {
                    operator = ">";
                } else if (condition.contains("<")) {
                    operator = "<";
                } else if (condition.contains("!=")) {
                    operator = "!=";
                }

                // Si un opérateur est trouvé, on sépare la condition en deux parties
                if (!operator.isEmpty()) {
                    String[] conditionParts = condition.trim().split(operator);
                    if (conditionParts.length != 2) {
                        throw new IOException("Erreur : Condition mal formatée.");
                    }

                    String droit = conditionParts[0].trim(); // Par exemple, R.C3
                    String gauche = conditionParts[1].trim(); // Par exemple, S.BB
            
                    // Ajouter la condition de jointure ou de filtrage
                    if (gauche.contains(".") && droit.contains(".")) {
                        String droitAttribut = droit.split("\\.")[1];
                        String droitIndexAndType = table1.getIndexAndTypeOfAttribute(droitAttribut.toLowerCase());
                        String droitType = droitIndexAndType.split(",")[1];

                        String gaucheAttribut = droit.split("\\.")[1];
                        String gaucheIndexAndType = table1.getIndexAndTypeOfAttribute(gaucheAttribut.toLowerCase());
                        String gaucheType = gaucheIndexAndType.split(",")[1];

                        if(!gaucheType.equals(droitType)){
                            throw new IOException("Erreur : Erreur de syntaxe dans la commande SELECT, la type des attributs "+droitAttribut+" et "+gaucheAttribut+" ne sont pas les memes.");
                        }
                        
                        conditions.add(new Condition(gauche, droit, operator,droitType));  // Condition de jointure
                    }   
                }
                else {
                    throw new IOException("Erreur : L'opérateur n'est pas valide dans la condition : " + condition);
                }


            }

            ArrayList<Record> joinedRecords = new ArrayList<>();

            // Charger les enregistrements de la première table (R2)
            RelationScanner scanner1 = new RelationScanner(table1); // Scanner pour charger les records un par un
            Record currentRecordTable1;
            ColInfo[] coolonne = table1.getColonnes();


            // Charger les enregistrements de la deuxième table (R1)
            Record currentRecordTable2;

            while ((currentRecordTable1 = scanner1.getNextRecord()) != null) {
                // Charger les records de R2 qui satisfont la condition avec R1.C1
                ArrayList<Condition> conditionsForR2 = new ArrayList<>();

                for(Condition condition:conditions){

                    String Relation1 = condition.getAttributRelation1();
                    String nomRelation1 = Relation1.split("\\.")[0];
                    String attributRelation1 = Relation1.split("\\.")[1];


                    String Relation2 = condition.getAttributRelation2();
                    String nomRelation2 = Relation2.split("\\.")[0];
                    String attributRelation2 = Relation2.split("\\.")[1];

                    if(nomRelation1.equals(tableName1)){
                        String indexTypeAttribut1 = table1.getIndexAndTypeOfAttribute(attributRelation1);
                        int indexAttribut1 = Integer.parseInt(indexTypeAttribut1.split(",")[0]);
                        String valeurColonne = currentRecordTable1.getAttributs()[indexAttribut1];

                        String indexTypeAttribut2 = table2.getIndexAndTypeOfAttribute(attributRelation2);
                        int indexAttribut2 = Integer.parseInt(indexTypeAttribut2.split(",")[0]);

                        Condition nouvCondition = new Condition(valeurColonne,indexAttribut2, condition.getOperateur(),condition.getTypeColonne());
                        conditionsForR2.add(nouvCondition);
                    }

                }
            
                RecordPrinter recordPrinter2 = new RecordPrinter(table2, conditionsForR2, projectionIndexs);
                for(Record rec:recordPrinter2.loadRecords()){
                    joinedRecords.add(rec);
                }
                                
            }
        }*/


    public static void main (String [] args)    {
        DBConfig config = DBConfig.loadDBConfig("src/config.json");
        SGBD mySGBD = new SGBD (config);
        mySGBD.Run();
    }
}

    