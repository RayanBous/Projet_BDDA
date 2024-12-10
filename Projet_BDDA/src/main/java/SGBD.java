import java.io.IOException;
import java.util.*;

public class SGBD {

    private DBConfig dbConfig;
    private DiskManager diskManager;
    private BufferManager bufferManager;
    private DBManager dbManager;

    public SGBD(DBConfig dbconfig) throws IOException  {
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

    public void Run() {
        System.out.println("********************    Bienvenue dans votre SGBD   ********************");
        String texteCommande;
        boolean quit = false;
        Scanner sc = new Scanner(System.in);

        while (!quit) {
            System.out.println("Veuillez choisir votre commande : \n");
            texteCommande = sc.nextLine();
            texteCommande = texteCommande.replaceAll("\\s+", " ").trim();
            pr:
            if (true) {
                if (texteCommande.toUpperCase().startsWith("CREATE DATABASE ")) {
                    try {
                        ProcessCreateDataBaseCommand(texteCommande);
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                    break pr;
                }
                if (texteCommande.toUpperCase().startsWith("CREATE TABLE ")) {
                    try {
                        ProcessCreateTableCommand(texteCommande);
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                    break pr;
                }
                if (texteCommande.toUpperCase().startsWith("SET DATABASE ")) {
                    try {
                        ProcessSetDataBaseCommand(texteCommande);
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                    break pr;
                }
                if (texteCommande.toUpperCase().equals("LIST TABLES")) {
                    ProcessListTablesCommand(texteCommande);
                    break pr;
                }
                if (texteCommande.toUpperCase().equals("LIST DATABASES")) {
                    ProcessListDataBasesCommand(texteCommande);
                    break pr;
                }
                if (texteCommande.toUpperCase().startsWith("DROP TABLE ")) {
                    try {
                        ProcessDropTableCommand(texteCommande);
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                    break pr;
                }
                if (texteCommande.toUpperCase().equals("DROP TABLES")) {
                    ProcessDropTablesCommand(texteCommande);
                    break pr;
                }
                if (texteCommande.toUpperCase().equals("DROP DATABASES")) {
                    ProcessDropDataBasesCommand(texteCommande);
                    break pr;
                }
                if (texteCommande.toUpperCase().startsWith("DROP DATABASE ")) {
                    try {
                        ProcessDropDataBaseCommand(texteCommande);
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                    break pr;
                }
                if (texteCommande.toUpperCase().startsWith("INSERT ")) {
                    try {
                        ProcessInsertCommand(texteCommande);
                    } catch (IOException e) {
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
        this.dbManager.ListDatabases();
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

    public void ProcessQuitCommand (String texteCommande)   {
        this.bufferManager.flushBuffers();
        this.diskManager.saveState();
        this.dbManager.SaveState();
    }

    public void ProcessInsertCommand(String commande) throws IOException {
        Database currentDB = this.dbManager.getActiveDB();
        System.out.println("BASE DE DONNÉES ACTUELLE : " + currentDB);
        if (currentDB == null) {
            System.err.println("Erreur : Aucune base de données active. Activez une base de données pour exécuter cette commande.");
            return;
        }

        try {
            // Nettoyage de la commande
            commande = commande.replaceAll("\\s+", " ").trim();
            String[] tokens = commande.split(" ", 4);

            if (tokens.length < 4 || !tokens[0].equalsIgnoreCase("INSERT") || !tokens[1].equalsIgnoreCase("INTO")) {
                System.err.println("Erreur : Syntaxe incorrecte. Utilisez : INSERT INTO table_name VALUES (val1, val2, ...)");
                return;
            }

            String tableName = tokens[2];
            System.out.println("Nom de la table : " + tableName);

            if (!currentDB.containsTable(tableName)) {
                System.err.println("Erreur : La table (" + tableName + ") n'existe pas dans la base de données {" + currentDB.getNom() + "}.");
                return;
            }

            Relation table = currentDB.getTable(tableName);

            String colValPart = tokens[3];
            if (!colValPart.toUpperCase().startsWith("VALUES (") || !colValPart.endsWith(")")) {
                System.err.println("Erreur : Syntaxe incorrecte. Utilisez : INSERT INTO table_name VALUES (val1, val2, ...)");
                return;
            }
                // VALUES (....)
            String valuesPart = colValPart.substring(8, colValPart.length() - 1);
            String[] values = valuesPart.split("[,]");
            for(String value : values) {
                System.out.println("Nom de la column : " + value);
            }

            ColInfo[] tableColumns = table.getColonnes();
            if (values.length != tableColumns.length) {
                System.err.println("Erreur : Le nombre de valeurs ne correspond pas au nombre de colonnes dans la table (" + tableName + ").");
                return;
            }

            Object[] recordValues = new Object[tableColumns.length];

            for (int i = 0; i < tableColumns.length; i++) {
                String value = values[i].trim();
                ColInfo column = tableColumns[i];

                switch (column.getType().toUpperCase()) {
                    case "INT":
                        try {
                            recordValues[i] = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            System.err.println("Erreur : La valeur (" + value + ") pour la colonne (" + column.getNom() + ") doit être un entier.");
                            return;
                        }
                        break;

                    case "REAL":
                        try {// Validation sans conversion explicite
                            recordValues[i] = Double.parseDouble(value);
                        } catch (NumberFormatException e) {
                            System.err.println("Erreur : La valeur (" + value + ") pour la colonne (" + column.getNom() + ") doit être un flottant.");
                            return;
                        }
                        break;

                    case "VARCHAR":
                        recordValues[i] = value.replaceAll("^ʺ|ʺ$", ""); // Suppression des guillemets (ʺ)
                        break;

                    case "CHAR":
                        int columnSize = column.getTaille();
                        value = value.replaceAll("^ʺ|ʺ$", ""); // Suppression des guillemets (ʺ)
                        if (value.length() > columnSize) {
                            System.err.println("Erreur : La valeur (" + value + ") pour la colonne (" + column.getNom() + ") dépasse la taille définie (" + columnSize + ").");
                            return;
                        }
                        recordValues[i] = String.format("%-" + columnSize + "s", value); // Remplissage à la taille fixe
                        break;

                    default:
                        System.err.println("Erreur : Type inconnu pour la colonne " + column.getNom());
                        return;
                }
            }

            Record record = new Record();
            String[] recordValuesString = new String[recordValues.length];
            for (int i = 0; i < recordValues.length; i++) {
                recordValuesString[i] = recordValues[i].toString();  // Convertir chaque objet en chaîne de caractères
            }
            record.setAttributs(recordValuesString);  // Passer le tableau de String

            table.InsertRecord(record);

            System.out.println("Insertion correcte dans la table : " + tableName);
            System.out.println("Contenu du Record : " + record);


        } catch (Exception e) {
            System.err.println("Erreur lors du traitement de la commande : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /*public void ProcessSelectCommand(String commande) {
        Database currentDB = this.dbManager.getActiveDB();
        System.out.println("BASE DE DONNÉES ACTUELLE : " + currentDB);

        if (currentDB == null) {
            System.err.println("Erreur : Aucune base de données active. Activez une base de données pour exécuter cette commande.");
            return;
        }

        try {
            // Nettoyage de la commande
            commande = commande.replaceAll("\\s+", " ").trim();

            // Séparation des parties de la commande
            String[] tokens = commande.split(" ", 4);

            // Vérifier la structure de la commande SELECT
            if (tokens.length < 4 || !tokens[0].equalsIgnoreCase("SELECT") || !tokens[1].equals("*") || !tokens[2].equalsIgnoreCase("FROM")) {
                System.err.println("Erreur : Syntaxe incorrecte. Utilisez : SELECT * FROM table_name");
                return;
            }

            // Récupérer le nom de la table et les colonnes
            String tableName = tokens[3].split(" ")[0];  // Nom de la table
            String whereClause = null;

            // Vérifier s'il y a une clause WHERE
            if (tokens[3].contains("WHERE")) {
                int whereIndex = tokens[3].indexOf("WHERE");
                whereClause = tokens[3].substring(whereIndex + 5).trim();  // Extraire la condition
                tableName = tokens[3].substring(0, whereIndex).trim();  // Extraire le nom de la table
            }

            System.out.println("Nom de la table : " + tableName);

            if (!currentDB.containsTable(tableName)) {
                System.err.println("Erreur : La table (" + tableName + ") n'existe pas dans la base de données {" + currentDB.getNom() + "}.");
                return;
            }

            // Récupérer la table
            Relation table = currentDB.getTable(tableName);
            ColInfo[] columns = table.getColonnes();  // Récupérer les colonnes de la table

            // Récupérer tous les enregistrements de la table
            List<Record> records = table.GetAllRecords();

            // Appliquer la clause WHERE (si présente)
            List<Record> filteredRecords = new ArrayList<>();
            for (Record record : records) {
                if (whereClause == null || evaluateWhereClause(record, whereClause, columns)) {
                    filteredRecords.add(record);
                }
            }

            // Affichage des résultats
            int totalRecords = filteredRecords.size();
            for (Record record : filteredRecords) {
                StringBuilder row = new StringBuilder();
                String[] recordValues = record.getAttributs();
                for (int i = 0; i < recordValues.length; i++) {
                    row.append(recordValues[i]);
                    if (i < recordValues.length - 1) {
                        row.append(" ; ");
                    }
                }
                System.out.println(row.toString());
            }

            // Affichage du total des enregistrements
            System.out.println("Total records = " + totalRecords);

        } catch (Exception e) {
            System.err.println("Erreur lors du traitement de la commande : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode pour évaluer la condition WHERE directement dans la méthode
    private boolean evaluateWhereClause(Record record, String whereClause, ColInfo[] columns) {
        String[] conditions = whereClause.split("AND");

        for (String condition : conditions) {
            condition = condition.trim();

            // Recherche des égalités (C1=1, etc.)
            String[] conditionParts = condition.split("=");
            if (conditionParts.length == 2) {
                String columnName = conditionParts[0].trim();
                String value = conditionParts[1].trim();

                // Rechercher la colonne correspondant à columnName
                boolean conditionMet = false;
                for (int i = 0; i < columns.length; i++) {
                    if (columns[i].getNom().equalsIgnoreCase(columnName)) {
                        String recordValue = record.getAttributs()[i].toString().trim();
                        if (recordValue.equals(value)) {
                            conditionMet = true;
                            break;
                        }
                    }
                }

                if (!conditionMet) {
                    return false;  // Si la condition échoue, on retourne false
                }
            }

            // Recherche des conditions de type <, >, <=, >=, etc.
            else {
                String[] comparisonParts = condition.split("[<>]=?");
                if (comparisonParts.length == 2) {
                    String columnName = comparisonParts[0].trim();
                    String value = comparisonParts[1].trim();

                    // Rechercher la colonne correspondant à columnName
                    boolean conditionMet = false;
                    for (int i = 0; i < columns.length; i++) {
                        if (columns[i].getNom().equalsIgnoreCase(columnName)) {
                            String recordValue = record.getAttributs()[i].toString().trim();

                            // Comparaison de la valeur selon l'opérateur
                            if (condition.contains("=")) {
                                if (recordValue.equals(value)) {
                                    conditionMet = true;
                                    break;
                                }
                            } else if (condition.contains("<")) {
                                if (Integer.parseInt(recordValue) < Integer.parseInt(value)) {
                                    conditionMet = true;
                                    break;
                                }
                            } else if (condition.contains(">")) {
                                if (Integer.parseInt(recordValue) > Integer.parseInt(value)) {
                                    conditionMet = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (!conditionMet) {
                        return false;  // Si la condition échoue, on retourne false
                    }
                }
            }
        }

        return true;  // Si toutes les conditions sont satisfaites
    }*/
}


    /*public void ProcessBulkInsertCommand(String texteCommande) throws IOException{
        try{
            if(!texteCommande.startsWith("BULKINSERT INTO")){
                System.err.println("La syntaxe de la commande pour BULKINSERT est invalide");
            }

            String[] partie = texteCommande.replace("BULKINSERT INTO", "").split("\\s+");
            if(partie.length != 2){
                System.err.println("La commande a mal été taper");
            }

            String nomRelation = partie[0].trim();
            String nomFichier = partie[1].trim();

            if(!nomFichier.endsWith(".csv")){
                System.err.println("Le format du fichier doit être en .csv");
            }

            Database db = dbManager.getDatabase();
            if (db == null) {
                System.err.println("La base de données n'existe pas");
            }

            Relation table = db.getTable(nomRelation).get(nomRelation);
            if(table == null){
                System.err.println("La table n'existe pas");
            }

            //On veut lire le fichier csv
            Path fichier = Paths.get(nomFichier);
            if(!Files.exists(fichier)){
                System.err.println("Le path/fichier n'existe pas");
                return;
            }

            try(BufferedReader br = Files.newBufferedReader(fichier)){
                String line;
                int nombreligne = 0;

                while((line = br.readLine()) != null){
                    nombreligne = nombreligne + 1;
                    String[] values = line.split(",");

                    if(values.length != table.getColonnes().length){
                        System.out.println("Le nombre de colonne est incorrecte");
                        continue;
                    }

                    String[] recordValues = new String[table.getColonnes().length];
                    ColInfo[] colonnes = table.getColonnes();

                    boolean valide = true;
                    for(int i = 0; i < colonnes.length; i++){
                        ColInfo co = colonnes[i];
                        String val = values[i].trim();

                        try{
                            if(co.getType().equals("INT")){
                                Integer.parseInt(val);
                                recordValues[i] = val;
                            }else if(co.getType().equals("REAL")){
                                Double.parseDouble(val);
                                recordValues[i] = val;
                            }else if(co.getType().equals("VARCHAR") || co.getType().equals("CHAR")){
                                if(val.startsWith("\"")){
                                    val = val.substring(1, val.length() - 1);
                                }
                                if(val.length() > co.getTaille()){
                                    System.err.println("Erreur ligne : " + nombreligne);
                                    valide = false;
                                    break;
                                }
                                recordValues[i] = val;
                            }else{
                                System.err.println("Erreur ligne : " + nombreligne);
                                valide = false;
                                break;
                            }
                        }catch(IllegalArgumentException e){
                            e.printStackTrace();
                        }
                    }
                    if(valide == true){
                        Record recordObj = new Record();
                        recordObj.setAttributs(recordValues);
                        table.InsertRecord(recordObj);
                        System.out.println("Insertion correcte dans la table : " + nomRelation);
                    }
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
*/

