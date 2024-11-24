/*import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;

public class RelationBis {
    private String nom;
    private int nbColonnes;
    private ColInfo[] colonnes;
    private PageId headerPageId;
    private DiskManager diskManager;
    private BufferManagerBis bufferManager;
    private boolean varchar;

    public RelationBis(String nom, int nbColonnes, ColInfo[] colonnes, PageId headerPageId, DiskManager diskManager, BufferManagerBis bufferManager, boolean var) throws IOException {
        this.nom = nom;
        this.nbColonnes = nbColonnes;
        this.colonnes = colonnes;
        this.diskManager = diskManager;
        this.bufferManager = bufferManager;
        this.varchar = var;
        if(headerPageId == null){
            headerPageId = diskManager.AllocPage();
            initializeHeaderPage();
        }else{
            this.headerPageId = headerPageId;
        }
    }

    public RelationBis(String nom, int nbColonnes)	{
        this.nom = nom;
        this.nbColonnes = nbColonnes;
        this.colonnes = new ColInfo[this.nbColonnes];
    }


    public DiskManager getDiskManager() {
        return diskManager;
    }

    public void setDiskManager(DiskManager diskManager) {
        this.diskManager = diskManager;
    }

    public BufferManagerBis getBufferManager() {
        return bufferManager;
    }

    public void setBufferManager(BufferManagerBis bufferManager) {
        this.bufferManager = bufferManager;
    }

    public boolean getVarchar(){
        return varchar;
    }

    public String getNom()  {
        return this.nom;
    }

    public void setNom(String nom)  {
        this.nom = nom;
    }

    public PageId getHeaderPageId() {
        return headerPageId;
    }

    public void setHeaderPageId(PageId headerPageId) {
        this.headerPageId = headerPageId;
    }

    public int getNbColonnes()  {
        return this.nbColonnes;
    }
    public void setNbColonnes(int nbColonnes) {
        this.nbColonnes = nbColonnes;
    }

    public ColInfo[] getColonnes()  {
        return this.colonnes;
    }

    public void setColonnes(ColInfo[] colonnes)   {
        this.colonnes = colonnes;
    }

    public int writeRecordToBuffer(Record record, ByteBuffer buffer, int pos) {
        //On commence par determiner le format d'écriture à utiliser et appeler directement la méthode d'écriture correspondante
        for (int i=0; i<this.nbColonnes; i+=1)  {
            if (this.colonnes[i].getType().equals("VARCHAR")){
                return writeRecordToBufferFormatVariable(record, buffer, pos);
            }
        }
        return writeRecordToBufferFormatFixe(record, buffer, pos);
    }

    // Cette méthode écrit dans un buffer en utilisant le format fixe
    public int writeRecordToBufferFormatFixe(Record record, ByteBuffer buffer, int pos)   {
        buffer.position(pos);
        for (int i=0; i<this.nbColonnes; i+=1)  {
            ColInfo colonne = this.colonnes[i];
            String value = record.getAttributs()[i];
            switch (colonne.getType()) {
                case "INT":
                    buffer.putInt(Integer.parseInt(value));
                    break;
                case "REAL":
                    buffer.putFloat(Float.parseFloat(value));
                    break;
                case "CHAR":
                    for (int j=0; j<value.length(); j+=1)    {
                        buffer.putChar(value.charAt(j));
                    }
                    break;
                default:
                    break;
            }
        }
        return buffer.position()-pos;
    }

    // Cette méthode écrit dans un buffer en utilisant le format variable
    public int writeRecordToBufferFormatVariable(Record record, ByteBuffer buffer, int pos)   {
        int positionIemeElement = pos + ((this.nbColonnes+1)*Integer.BYTES);
        buffer.position(pos);
        // Premierement on complete notre buffer avec le tableau de l'offset directory
        for (int i=0; i<this.nbColonnes; i+=1)  {
            buffer.putInt(positionIemeElement);
            switch (this.colonnes[i].getType()) {
                case "INT":
                    positionIemeElement += Integer.BYTES;
                    break;
                case "REAL":
                    positionIemeElement += Float.BYTES;
                    break;
                case "CHAR":
                    positionIemeElement += (Character.BYTES * this.colonnes[i].getTaille());
                    break;
                case "VARCHAR":
                    positionIemeElement += (record.getAttributs()[i].length() * Character.BYTES);
                    break;
                default:
                    break;
            }
        }
        buffer.putInt(positionIemeElement);
        // Maintenant on doit enregistrer nos attributs dans notre buffer
        for (int i=0; i<this.nbColonnes; i+=1)  {
            ColInfo colonne = this.colonnes[i];
            String value = record.getAttributs()[i];
            switch (colonne.getType()) {
                case "INT":
                    buffer.putInt(Integer.parseInt(value));
                    break;
                case "REAL":
                    buffer.putFloat(Float.parseFloat(value));
                    break;
                case "CHAR":
                case "VARCHAR":
                    for (int j=0; j<value.length(); j+=1)    {
                        buffer.putChar(value.charAt(j));
                    }
                    break;
                default:
                    break;
            }
        }
        return buffer.position()-pos;
    }

    public int readFromBuffer(Record record, ByteBuffer buffer, int pos)    {
        //On commence par determiner le format de lécture à utiliser et appeler directement la méthode lecture correspondante
        for (int i=0; i<this.nbColonnes; i+=1)  {
            if (this.colonnes[i].getType().equals("VARCHAR")){
                return readFromBufferFormatVariable(record, buffer, pos);
            }
        }
        return readFromBufferFormatFixe(record, buffer, pos);
    }

    public int readFromBufferFormatFixe(Record record, ByteBuffer buffer, int pos)  {
        buffer.position(pos);
        for (int i=0; i<this.nbColonnes; i+=1)  {
            ColInfo colonne = this.colonnes[i];
            switch(colonne.getType()){
                case "INT":
                    String attributInt = buffer.getInt()+"";
                    record.setAttribut(i, attributInt);
                    break;
                case "REAL":
                    String attributFloat = buffer.getFloat()+"";
                    record.setAttribut(i, attributFloat);
                    break;
                case "CHAR":
                    StringBuilder attributString = new StringBuilder();
                    for (int j=0; j<colonne.getTaille(); j+=1){
                        attributString.append(buffer.getChar());
                    }
                    record.setAttribut(i, attributString.toString());
                    break;
                default:
                    break;
            }
        }
        return buffer.position()-pos;
    }

    public int readFromBufferFormatVariable(Record record, ByteBuffer buffer, int pos)  {
        buffer.position(pos);
        int positionIemeElement;
        for (int i=0; i<this.nbColonnes; i+=1)  {
            ColInfo colonne = this.colonnes[i];
            positionIemeElement = buffer.getInt();
            switch(colonne.getType())  {
                case "INT":
                    String attributInt = buffer.getInt(positionIemeElement) + "";
                    record.setAttribut(i, attributInt);
                    break;
                case "REAL":
                    String attributFloat = buffer.getFloat(positionIemeElement) + "";
                    record.setAttribut(i, attributFloat);
                    break;
                case "CHAR":
                case "VARCHAR":
                    int positionIemePlusUnElement = buffer.getInt(pos+((i+1)*Integer.BYTES));
                    StringBuilder attributString = new StringBuilder();
                    for (int j=positionIemeElement; j<positionIemePlusUnElement; j+=Character.BYTES)  {
                        attributString.append(buffer.getChar(j));
                    }
                    record.setAttribut(i,  attributString.toString());
                    break;
                default:
                    break;
            }
        }
        return buffer.getInt()-pos;
    }

    public void addDataPage() throws IOException {
        System.out.println("\n**************  Début ajout d'une page de données *********************");

        // Étape 1 : Allouer une nouvelle page via AllocPage du DiskManager
        PageId nouvellePageId = diskManager.AllocPage();
        System.out.println("TEST addDataPage : On a alloué la page de données à l'emplacement " + nouvellePageId);

        // Étape 2 : Charger la page d'en-tête dans un buffer pour la mettre à jour
        ByteBuffer buffHeader = bufferManager.GetPage(headerPageId);

        // Vérification si buffHeader est null (éviter les NullPointerException)
        if (buffHeader == null) {
            throw new IOException("Impossible de charger la page d'en-tête. Le buffer est null.");
        }

        // Étape 3 : Lire le nombre actuel de pages de données dans la relation
        buffHeader.position(0);  // Se déplacer au début de la page d'en-tête
        int numPages = buffHeader.getInt();  // Lecture du nombre de pages (stocké au début de la page d'en-tête)

        // Calcul de l'offset où ajouter la nouvelle page dans le répertoire des pages (Page Directory)
        int offset = 4 + (numPages * 12);  // Chaque entrée du Page Directory prend 12 octets (2x PageId + espace libre)

        // Étape 4 : Vérification qu'il y a suffisamment de place dans la page d'en-tête
        int taillePage = (int) diskManager.getDBConfig().getPagesize();
        if ((offset + 12) > taillePage) { // Vérifier si on a assez d'espace pour ajouter une nouvelle page
            System.out.println("ERREUR : Il n'y a plus assez de place dans la page d'en-tête pour accueillir une nouvelle page de données.");
            // Dans un cas réel, il faudrait probablement gérer la pagination de l'en-tête
            return; // Sortie de la méthode si la page d'en-tête est pleine
        }

        // Étape 5 : Ajouter la nouvelle page dans le Page Directory
        buffHeader.position(offset);  // Se positionner à l'offset calculé
        buffHeader.putInt(nouvellePageId.getFileIdx());  // Mettre l'ID du fichier de la nouvelle page
        buffHeader.putInt(nouvellePageId.getPageIdx());  // Mettre l'ID de la page de la nouvelle page
        buffHeader.putInt(taillePage - 4 * 2);  // Espace disponible sur la nouvelle page, initialement toute la page sauf les 8 octets réservés

        // Étape 6 : Mettre à jour le nombre total de pages dans la page d'en-tête
        buffHeader.position(0);  // Revenir au début pour mettre à jour le nombre de pages
        buffHeader.putInt(numPages + 1);  // Incrémenter le nombre de pages de données

        // Étape 7 : Libérer la page d'en-tête (en la marquant comme "sale" pour l'écriture)
        bufferManager.FreePage(headerPageId, true);  // Marquer la page d'en-tête comme modifiée

        System.out.println("**************  Fin ajout d'une page de données *********************");
    }

    public PageId getFreeDataPageId(int sizeRecord) throws IOException {
        System.out.println("\n************** Début getFreeDataPageId *********************");

        PageId pageDisponible = null;
        boolean pageTrouve = false;
        int currentPosition = 12;  // Début du Page Directory dans l'en-tête
        int octetRestantPage;      // Variable pour récupérer l'espace libre restant dans chaque page
        int octetNecessaireInsertion = sizeRecord + 8;  // La taille nécessaire pour insérer un record (taille record + 8 octets)

        // Charger la page d'en-tête dans un buffer
        ByteBuffer buffHeader = bufferManager.GetPage(headerPageId);

        // Vérifier si le buffer est null
        if (buffHeader == null) {
            System.out.println("ERREUR : Impossible de charger la page d'en-tête, buffer null.");
            return null;
        }

        System.out.println("RELATION : getFreeDataPageId : buff info : " + buffHeader);

        // Lire le nombre de pages de données à partir de la page d'en-tête
        int numPages = buffHeader.getInt(0);
        System.out.println("Nombre de pages de données : " + numPages);

        // Parcourir les pages de données pour trouver celle avec suffisamment d'espace
        while (currentPosition + 12 <= buffHeader.limit() && !pageTrouve) {
            // Lire l'espace restant dans la page à la position actuelle
            buffHeader.position(currentPosition);
            octetRestantPage = buffHeader.getInt();
            System.out.println("Espace restant dans la page : " + octetRestantPage);

            if (octetRestantPage >= octetNecessaireInsertion) {
                // Si l'espace est suffisant, récupérer les informations sur la page
                buffHeader.position(currentPosition - 12);  // Retourner de 12 octets pour accéder aux ID de la page
                int numeroFichier = buffHeader.getInt();
                int numeroPage = buffHeader.getInt();

                // Créer l'objet PageId avec les informations trouvées
                pageDisponible = new PageId(numeroFichier, numeroPage);
                pageTrouve = true;
            }

            // Passer à la prochaine page dans le répertoire des pages
            currentPosition += 12;
        }
        // Libérer la page d'en-tête après utilisation
        bufferManager.FreePage(headerPageId, true);  // Marquer la page comme "sale" (modifiée) après usage
        if (pageDisponible != null) {
            System.out.println("RELATION : getFreeDataPageId : Page trouvée : " + pageDisponible);
        } else {
            System.out.println("RELATION : getFreeDataPageId : Aucune page disponible trouvée.");
        }
        System.out.println("\n************** Fin getFreeDataPageId *********************");
        return pageDisponible;  // Retourner la page trouvée ou null si aucune n'a été trouvée
    }


    public RecordId writeRecordToDataPage(Record record, PageId pageId) throws IOException {
        System.out.println("\n**************  Ecriture d'un record dans la page de donnée   *********************");

        // Récupération de la page de données dans un buffer
        ByteBuffer buffData = bufferManager.GetPage(pageId);  // Simulation de la récupération de la page avec pageId
        if (buffData == null) {
            System.out.println("Erreur : Impossible de récupérer la page de données.");
            return null;
        }

        int pageSize = (int) diskManager.getDBConfig().getPagesize(); // Taille de la page de données
        int offsetNombreSlot = pageSize - 8;  // Décalage du nombre de slots (en fonction de la taille de la page)

        buffData.position(offsetNombreSlot);  // Positionner le buffer à l'endroit du nombre de slots
        int nombreSlot = buffData.getInt();  // Lire le nombre de slots actuellement utilisés
        int positionEcrireRecord = buffData.getInt();  // Lire la position pour écrire le prochain record

        // Ecrire le record dans le buffer à la position déterminée
        buffData.flip();
        int tailleRecord = writeRecordToBuffer(record, buffData, positionEcrireRecord);  // Ecriture du record et retour de sa taille
        buffData.limit(buffData.capacity());  // Remettre la limite du buffer à sa capacité

        // Calculer la position pour écrire la nouvelle entrée du slot (position et taille du record)
        int positionEcrireSlot = pageSize - 8 - nombreSlot * 8 - 8;  // Calcul de la position du slot à mettre à jour
        buffData.position(positionEcrireSlot);
        buffData.putInt(positionEcrireRecord);  // Stocker la position du record
        buffData.putInt(tailleRecord);  // Stocker la taille du record

        // Mise à jour du nombre de slots et de la position pour le prochain record
        buffData.position(offsetNombreSlot);
        buffData.putInt(nombreSlot + 1);  // Incrémenter le nombre de slots
        buffData.putInt(positionEcrireRecord + tailleRecord);  // Mettre à jour la position pour le prochain enregistrement

        // Libérer la page de données après modification
        bufferManager.FreePage(pageId, true);

        // Mise à jour de la page d'en-tête pour ajuster l'espace libre
        // Rechercher la page correspondante dans la header page
        int numeroFichier, numeroPage;
        boolean pageTrouve = false;
        ByteBuffer buffHeader = bufferManager.GetPage(headerPageId);
        if (buffHeader == null) {
            System.out.println("Erreur : Impossible de récupérer la page d'en-tête.");
            return null;
        }

        int n = buffHeader.getInt(0);  // Nombre de pages de données
        int i = 0;
        buffHeader.position(4);  // Positionner sur le premier octet des informations de page
        while (i < n && !pageTrouve) {
            numeroFichier = buffHeader.getInt();  // Lire le numéro de fichier
            numeroPage = buffHeader.getInt();  // Lire le numéro de page
            if (pageId.getFileIdx() == numeroFichier && pageId.getPageIdx() == numeroPage) {
                pageTrouve = true;  // Page trouvée
                int octetRestantDispo = buffHeader.getInt();  // Lire l'espace restant
                buffHeader.position(buffHeader.position() - 4);  // Revenir de 4 octets pour modifier l'espace restant
                buffHeader.putInt(octetRestantDispo - tailleRecord - 8);  // Déduire la taille du record et les 8 octets du slot
                System.out.println("Octet Restant desormais: " + (octetRestantDispo - tailleRecord - 8));
            }
            i++;
            if (buffHeader.position() + 4 < buffHeader.capacity()) {
                buffHeader.position(buffHeader.position() + 4);  // Avancer de 4 octets pour vérifier la page suivante
            }
        }

        // Libérer la page d'en-tête après modification
        bufferManager.FreePage(headerPageId, true);

        System.out.println("\n**************  FIN Ecriture d'un record dans la page de donnée   *********************");

        // Retourner un RecordId avec le numéro de slot et de page
        return new RecordId(pageId, nombreSlot + 1);  // Retourner le RecordId du nouveau record
    }

    public byte[] serializeRecord(String[] colonnes) throws IOException {
        // Cette méthode convertit un tableau de chaînes de caractères en un tableau d'octets.
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);

        // Sérialiser chaque colonne en écrivant sa longueur et son contenu
        for (String colonne : colonnes) {
            dataStream.writeInt(colonne.length());  // écrire la longueur de la chaîne
            dataStream.writeBytes(colonne);         // écrire la chaîne elle-même
        }

        // Retourner le tableau d'octets résultant
        return byteStream.toByteArray();
    }

    public List<Record> getRecordsInDataPage(PageId pageId) throws IOException {
        List<Record> records = new ArrayList<>();
        // Charger la page dans un ByteBuffer via le BufferManager
        ByteBuffer buffer = bufferManager.GetPage(pageId);

        if (buffer == null) {
            throw new IOException("La page demandée n'est pas en mémoire.");
        }

        // Lire l'espace libre au début de la page
        int espacedispo = buffer.getInt(0);
        int offset = 4;  // L'offset commence après la valeur de l'espace libre

        while (offset < buffer.capacity() - espacedispo) {
            int numColumns = buffer.getInt(offset);  // Nombre de colonnes
            offset += 4;  // Avancer de 4 octets pour lire numColumns

            String[] colonnes = new String[numColumns];

            // Lire chaque colonne
            for (int i = 0; i < numColumns; i++) {
                int stringLength = buffer.getInt(offset);  // Longueur de la chaîne
                offset += 4;  // Avancer de 4 octets pour la longueur

                // Lire les octets de la chaîne
                byte[] stringBytes = new byte[stringLength];
                buffer.get(stringBytes, 0, stringLength);  // Lire directement les octets dans le tableau
                offset += stringLength;  // Avancer l'offset du nombre de bytes lus

                // Convertir les octets en chaîne de caractères
                String columnValue = new String(stringBytes);
                colonnes[i] = columnValue;
            }

            // Créer un nouvel enregistrement avec les colonnes lues
            Record record = new Record(colonnes);

            // Associer un RecordId à cet enregistrement
            RecordId recordId = new RecordId(pageId, offset);
            record.setRecordId(recordId);

            // Ajouter l'enregistrement à la liste
            records.add(record);
        }

        // Libérer la page après lecture (mettre à jour la page avec l'offset final)
        bufferManager.FreePage(pageId, true);

        // Retourner la liste des enregistrements
        return records;
    }

    public List<PageId> getDataPages() throws IOException {
        List<PageId> dataPages = new ArrayList<>();

        // Charger la page d'en-tête
        ByteBuffer buffHeader = bufferManager.GetPage(headerPageId);
        if (buffHeader == null) {
            throw new IOException("Erreur : impossible de charger la page d'en-tête.");
        }

        // Lire le nombre de pages de données
        int nbDataPage = buffHeader.getInt(0);
        System.out.println("Nombre de pages de données : " + nbDataPage);

        // Vérifiez que nbDataPage est valide
        if (nbDataPage < 0 || nbDataPage * 12 + 4 > buffHeader.capacity()) {
            bufferManager.FreePage(headerPageId, false); // Libérer la page proprement
            throw new IOException("Erreur : le nombre de pages de données est incorrect : " + nbDataPage);
        }

        // Parcourir les pages de données
        int offset = 4; // Offset initial après nbDataPage
        for (int i = 0; i < nbDataPage; i++) {
            int fid = buffHeader.getInt(offset);  // Lire le File ID
            offset += 4;
            int pid = buffHeader.getInt(offset);  // Lire le Page ID
            offset += 4;

            // Ajouter un PageId
            dataPages.add(new PageId(fid, pid));
        }

        // Libérer la page d'en-tête
        bufferManager.FreePage(headerPageId, false);

        return dataPages;
    }



    private ByteBuffer initializeHeaderPageAndGetBuffer(PageId headerPageId) throws IOException {
        if (headerPageId == null) {
            // Allouer une nouvelle page d'en-tête si le headerPageId est nul
            headerPageId = diskManager.AllocPage();
            System.out.println("Page d'en-tête allouée : " + headerPageId);

            // Créer un tableau de bytes pour la page d'en-tête
            int pageSize = 4096;  // Taille de la page (par exemple, 4096 octets)
            byte[] data = new byte[pageSize];
            ByteBuffer buffer = ByteBuffer.wrap(data);

            // Initialiser la page d'en-tête avec des données par défaut (par exemple, un entier à 0 pour le nombre de pages)
            buffer.putInt(0);  // Nombre de pages de données initialisé à 0

            // Écrire la page dans le disque
            diskManager.WritePage(headerPageId, data);

            // Retourner le buffer pour la page d'en-tête
            return buffer;
        }

        // Si la page d'en-tête existe déjà, la récupérer dans un ByteBuffer
        ByteBuffer buffer = bufferManager.GetPage(headerPageId);
        if (buffer == null) {
            System.err.println("*******************PAGE D'EN TÊTE PAS TROUVÉE DANS LE BUFFER********************");
            return null;
        }

        // Retourner le buffer contenant les données de la page d'en-tête
        return buffer;
    }


    public void initializeHeaderPage() throws IOException {
        if (headerPageId == null) {
            System.out.println("Création d'une nouvelle page d'en-tête.");

            // Allouer une nouvelle page
            headerPageId = diskManager.AllocPage();
            System.out.println("Page d'en-tête allouée : " + headerPageId);

            // Créer un buffer pour initialiser la page d'en-tête
            byte[] data = new byte[4096]; // Taille de la page
            ByteBuffer buffer = ByteBuffer.wrap(data);

            // Initialiser nbDataPage à 0
            buffer.putInt(0, 0);

            // Écrire la page sur le disque
            diskManager.WritePage(headerPageId, data);
            System.out.println("Page d'en-tête initialisée avec 0 pages de données.");
        } else {
            System.out.println("Page d'en-tête déjà existante : " + headerPageId);
        }
    }



    public List<Record> getAllRecords() throws IOException {
        List<Record> records = new ArrayList<>();
        System.out.println("**************  DEBUT Get All Records   *********************");

        // Récupérer les pages de données
        List<PageId> dataPages = getDataPages();
        System.out.println("Pages de données récupérées : " + dataPages.size());

        // Parcourir chaque page et récupérer les records
        for (PageId pageId : dataPages) {
            List<Record> pageRecords = getRecordsInDataPage(pageId);
            System.out.println("Records récupérés dans la page " + pageId + " : " + pageRecords.size());
            records.addAll(pageRecords);

            // Affichage des records
            for (Record record : pageRecords) {
                System.out.println("Record : " + record);
            }
        }

        System.out.println("**************  FIN Get All Records   *********************");
        return records;
    }


    public RecordId InsertRecord(Record record) throws IOException {
        RecordId rid=null;  // initialisation du rid
        int octetCumulerRecord=0;
        for( ColInfo c : colonnes){ // on obtient la somme en terme d'octet que la colonne fait
            octetCumulerRecord +=c.getTaille();
        }
        if(varchar){ // Si il y a un varchar dans les colonnes, alors il faut ajouter les octets que vont prendre les n+1 int pour délimiter les cases
            octetCumulerRecord+= 4* (getNbColonnes()+1);
        }
        PageId pageDispo = getFreeDataPageId(octetCumulerRecord); // On cherche une page disponible
        System.out.println("Page Dispo : "+pageDispo);
        if (pageDispo!=null){ // Si une page est disponible, on "cris le contenu du record dans la page et n sauvegarde le rid
            rid =writeRecordToDataPage(record,pageDispo);
            System.out.println("Insertion du record réussi !!  "+rid);
        }else{
            System.out.println(" !!!! Erreur lors de l'insertion d'un record : Aucune page ne semble disponible (Insert Record) !!!!");
        }
        return rid; // retour du rid
    }

    public int calculerTailleRecord(Record record){
        int taille_tot = 0;

        for(ColInfo c :  colonnes){
            taille_tot = taille_tot + c.getTaille();
        }

        if(varchar == true){
            taille_tot = taille_tot + (4*getNbColonnes());
        }
        return taille_tot;
    }

    private boolean verifVarchar(){ //Uniquement pour le format variable
        boolean var = false;
        int reponse = 0;
        for(ColInfo c : colonnes){
            if((c.getType().equals("VARCHAR"))){
                var = true;
                break;
            }
        }
        return var;
    }
}*/

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;

public class RelationBis {

    private String nomRelation;
    private int nbColonnes;
    private List<ColInfo> colonnes;
    private boolean varchar;
    private PageId headerPageId; // identifiant de Header Page de la relation
    private DiskManager diskManager;
    private BufferManagerBis bufferManager;

    public RelationBis(String nomRelation, int nbColonnes, PageId headerPageId, DiskManager diskManager, BufferManagerBis bufferManager) {
        this.nomRelation = nomRelation;
        this.nbColonnes = nbColonnes;
        this.headerPageId = headerPageId;
        this.diskManager = diskManager;
        this.bufferManager = bufferManager;
        this.varchar= possedeUnVarchar();
    }
    public RelationBis(String nomRelation, int nbColonnes, PageId headerPageId, DiskManager diskManager, BufferManagerBis bufferManager,List<ColInfo> colonnes) {
        this.nomRelation = nomRelation;
        this.nbColonnes = nbColonnes;
        this.colonnes = colonnes;
        this.headerPageId = headerPageId;
        this.diskManager = diskManager;
        this.bufferManager = bufferManager;
        this.varchar=possedeUnVarchar();
    }

    public RelationBis(String nomRelation, int nbColonnes) {
        this.nomRelation = nomRelation; // Correction de l'attribut nomRelation (au lieu de nomCol)
        this.nbColonnes = nbColonnes;
        this.colonnes = new ArrayList<>(this.nbColonnes); // Utilisation d'une ArrayList pour respecter le type List
    }

    public RelationBis(String nomRelation, int nbColonnes, DiskManager diskManager, BufferManagerBis bufferManager) throws IOException {
        this.nomRelation = nomRelation;
        this.nbColonnes = nbColonnes;
        this.diskManager = diskManager;
        this.bufferManager = bufferManager;
        this.colonnes = new ArrayList<>(this.nbColonnes); // Utilisation d'une ArrayList pour stocker les colonnes

        // Créer et initialiser la headerPage directement dans le constructeur
        this.headerPageId = diskManager.AllocPage(); // Allocation d'une nouvelle page via DiskManager

        // Obtention de la page associée à headerPageId
        ByteBuffer headerPage = bufferManager.GetPage(headerPageId);

        // Initialisation de la headerPage avec les données de base
        // Exemple d'initialisation simplifiée : écrire le nombre de colonnes à l'offset 0
        headerPage.putInt(0, nbColonnes); // Par exemple, le nombre de colonnes à l'offset 0
        headerPage.putInt(4, 0); // Initialisation d'un compteur de pages de données à l'offset 4 (exemple)

        // Libérer la page après initialisation
        bufferManager.ReleasePage(headerPage);
    }


    public DiskManager getDiskManager() {
        return diskManager;
    }

    public void setDiskManager(DiskManager diskManager) {
        this.diskManager = diskManager;
    }

    public BufferManagerBis getBufferManager() {
        return bufferManager;
    }

    public void setBufferManager(BufferManagerBis bufferManager) {
        this.bufferManager = bufferManager;
    }

    public boolean getVarchar(){
        return varchar;
    }

    public String getNom()  {
        return this.nomRelation;
    }

    public void setNom(String nom)  {
        this.nomRelation = nom;
    }

    public PageId getHeaderPageId() {
        return headerPageId;
    }

    public void setHeaderPageId(PageId headerPageId) {
        this.headerPageId = headerPageId;
    }

    public void setNbColonnes(int nbColonnes) {
        this.nbColonnes = nbColonnes;
    }

    public List<ColInfo> getColonnes()  {
        return this.colonnes;
    }

    public void setColonnes(List<ColInfo> colonnes)   {
        this.colonnes = colonnes;
    }

    public int writeRecordToBuffer(Record record, ByteBuffer buffer, int pos) {
        //On commence par determiner le format d'écriture à utiliser et appeler directement la méthode d'écriture correspondante
        for (int i=0; i<this.nbColonnes; i+=1)  {
            if (this.colonnes.get(i).getType().equals("VARCHAR")){
                return writeRecordToBufferFormatVariable(record, buffer, pos);
            }
        }
        return writeRecordToBufferFormatFixe(record, buffer, pos);
    }

    // Cette méthode écrit dans un buffer en utilisant le format fixe
    public int writeRecordToBufferFormatFixe(Record record, ByteBuffer buffer, int pos)   {
        buffer.position(pos);
        for (int i=0; i<this.nbColonnes; i+=1)  {
            ColInfo colonne = this.colonnes.get(i);
            String value = record.getAttributs()[i];
            switch (colonne.getType()) {
                case "INT":
                    buffer.putInt(Integer.parseInt(value));
                    break;
                case "REAL":
                    buffer.putFloat(Float.parseFloat(value));
                    break;
                case "CHAR":
                    for (int j=0; j<value.length(); j+=1)    {
                        buffer.putChar(value.charAt(j));
                    }
                    break;
                default:
                    break;
            }
        }
        return buffer.position()-pos;
    }

    // Cette méthode écrit dans un buffer en utilisant le format variable
    public int writeRecordToBufferFormatVariable(Record record, ByteBuffer buffer, int pos)   {
        int positionIemeElement = pos + ((this.nbColonnes+1)*Integer.BYTES);
        buffer.position(pos);
        // Premierement on complete notre buffer avec le tableau de l'offset directory
        for (int i=0; i<this.nbColonnes; i+=1)  {
            buffer.putInt(positionIemeElement);
            switch (this.colonnes.get(i).getType()) {
                case "INT":
                    positionIemeElement += Integer.BYTES;
                    break;
                case "REAL":
                    positionIemeElement += Float.BYTES;
                    break;
                case "CHAR":
                    positionIemeElement += (Character.BYTES * this.colonnes.get(i).getTaille());
                    break;
                case "VARCHAR":
                    positionIemeElement += (record.getAttributs()[i].length() * Character.BYTES);
                    break;
                default:
                    break;
            }
        }
        buffer.putInt(positionIemeElement);
        // Maintenant on doit enregistrer nos attributs dans notre buffer
        for (int i=0; i<this.nbColonnes; i+=1)  {
            ColInfo colonne = this.colonnes.get(i);
            String value = record.getAttributs()[i];
            switch (colonne.getType()) {
                case "INT":
                    buffer.putInt(Integer.parseInt(value));
                    break;
                case "REAL":
                    buffer.putFloat(Float.parseFloat(value));
                    break;
                case "CHAR":
                case "VARCHAR":
                    for (int j=0; j<value.length(); j+=1)    {
                        buffer.putChar(value.charAt(j));
                    }
                    break;
                default:
                    break;
            }
        }
        return buffer.position()-pos;
    }

    public int readFromBuffer(Record record, ByteBuffer buffer, int pos)    {
        //On commence par determiner le format de lécture à utiliser et appeler directement la méthode lecture correspondante
        for (int i=0; i<this.nbColonnes; i+=1)  {
            if (this.colonnes.get(i).getType().equals("VARCHAR")){
                return readFromBufferFormatVariable(record, buffer, pos);
            }
        }
        return readFromBufferFormatFixe(record, buffer, pos);
    }

    public int readFromBufferFormatFixe(Record record, ByteBuffer buffer, int pos)  {
        buffer.position(pos);
        for (int i=0; i<this.nbColonnes; i+=1)  {
            ColInfo colonne = this.colonnes.get(i);
            switch(colonne.getType()){
                case "INT":
                    String attributInt = buffer.getInt()+"";
                    record.setAttribut(i, attributInt);
                    break;
                case "REAL":
                    String attributFloat = buffer.getFloat()+"";
                    record.setAttribut(i, attributFloat);
                    break;
                case "CHAR":
                    StringBuilder attributString = new StringBuilder();
                    for (int j=0; j<colonne.getTaille(); j+=1){
                        attributString.append(buffer.getChar());
                    }
                    record.setAttribut(i, attributString.toString());
                    break;
                default:
                    break;
            }
        }
        return buffer.position()-pos;
    }

    public int readFromBufferFormatVariable(Record record, ByteBuffer buffer, int pos)  {
        buffer.position(pos);
        int positionIemeElement;
        for (int i=0; i<this.nbColonnes; i+=1)  {
            ColInfo colonne = this.colonnes.get(i);
            positionIemeElement = buffer.getInt();
            switch(colonne.getType())  {
                case "INT":
                    String attributInt = buffer.getInt(positionIemeElement) + "";
                    record.setAttribut(i, attributInt);
                    break;
                case "REAL":
                    String attributFloat = buffer.getFloat(positionIemeElement) + "";
                    record.setAttribut(i, attributFloat);
                    break;
                case "CHAR":
                case "VARCHAR":
                    int positionIemePlusUnElement = buffer.getInt(pos+((i+1)*Integer.BYTES));
                    StringBuilder attributString = new StringBuilder();
                    for (int j=positionIemeElement; j<positionIemePlusUnElement; j+=Character.BYTES)  {
                        attributString.append(buffer.getChar(j));
                    }
                    record.setAttribut(i,  attributString.toString());
                    break;
                default:
                    break;
            }
        }
        return buffer.getInt()-pos;
    }

    public void addDataPage() throws IOException {
        System.out.println("\n************** Début Ajout d'une page de données *********************");

        int taillePage = (int) diskManager.getDBConfig().getPagesize();
        ByteBuffer buffHeader = bufferManager.GetPage(headerPageId);

        // Récupération du nombre de pages de données déjà enregistrées
        int nbDataPage = buffHeader.getInt(0);
        int indice_dernierOctetLecture = nbDataPage * 12 + 4;

        System.out.println("RELATION : Nombre actuel de pages de données : " + nbDataPage);
        System.out.println("RELATION : Indice dernier octet lecture : " + indice_dernierOctetLecture);

        // Vérification de la place disponible
        if ((indice_dernierOctetLecture + 12) > taillePage) {
            System.out.println("ERREUR : Pas assez de place dans la headerPage pour ajouter une nouvelle page de données.");
            boolean dirtyPage = bufferManager.getDirtyPage(headerPageId);
            bufferManager.FreePage(headerPageId, dirtyPage);
        } else {
            // Allocation d'une nouvelle page de données
            PageId nouvellePageDonnee = diskManager.AllocPage();
            System.out.println("TEST addDataPage: Nouvelle page allouée : " + nouvellePageDonnee);

            // Mise à jour de la headerPage
            buffHeader.position(indice_dernierOctetLecture);
            buffHeader.putInt(nouvellePageDonnee.getFileIdx());
            buffHeader.putInt(nouvellePageDonnee.getPageIdx());

            int octetDisponibles = (int) diskManager.getDBConfig().getPagesize() - 4 * 2;
            buffHeader.putInt(octetDisponibles);

            // Mise à jour du compteur de pages de données
            nbDataPage++;
            buffHeader.putInt(0, nbDataPage);

            System.out.println("RELATION : Mise à jour de la headerPage réussie.");
            System.out.println("RELATION : Nombre total de pages de données : " + nbDataPage);

            // Libération de la page (modifiée)
            bufferManager.FreePage(headerPageId, true);
        }

        System.out.println("************** Fin Ajout d'une page de données *********************");
    }

    public PageId getFreeDataPageId(int sizeRecord) throws IOException {
        System.out.println("\n**************  Debut get free data pageId   *********************");

        PageId pageDisponible = null;
        boolean pageTrouve = false;
        int currentPosition = 12; // Position initiale dans le buffer (en supposant que la page commence après le PageId et la position du slot directory)
        int octetRestantPage; // Nombre d'octets restants dans la page
        int octetNecessaireInsertion = sizeRecord + 8; // Taille nécessaire pour insérer le record (taille + 8 octets pour les métadonnées)

        ByteBuffer buffHeader = bufferManager.GetPage(headerPageId);
        System.out.println("RELATION : getFreeDataPageId : buff info : " + buffHeader);

        // Vérification du nombre de pages dans le header
        int n = buffHeader.getInt(0); // Nombre de pages dans le header
        if (n <= 0) {
            System.out.println("Nombre de pages invalide : " + n);
            return null;
        }

        // Parcours des pages pour trouver une page avec suffisamment d'espace libre
        int i = 0;
        buffHeader.position(currentPosition); // Positionnement du buffer à la première page de données
        while ((i < n) && (!pageTrouve)) {
            // Lire la position du premier espace libre et le nombre de cases dans le slot directory
            if (buffHeader.remaining() >= 8) {
                int positionEspaceLibre = buffHeader.getInt(); // Position du premier espace libre
                int nbCases = buffHeader.getInt(); // Nombre de cases dans le slot directory
                System.out.println("Position espace libre : " + positionEspaceLibre + ", Nombre de cases : " + nbCases);

                // Vérifier chaque case dans le slot directory
                for (int j = 0; j < nbCases; j++) {
                    if (buffHeader.remaining() >= 8) {
                        int positionRecord = buffHeader.getInt(); // Position du i-ème record
                        int tailleRecord = buffHeader.getInt(); // Taille du i-ème record
                        System.out.println("Case " + j + ": position = " + positionRecord + ", taille = " + tailleRecord);

                        // Vérifier si l'espace est suffisant pour insérer le nouveau record
                        if (tailleRecord == 0 || tailleRecord >= octetNecessaireInsertion) {
                            // Lire les informations nécessaires pour instancier le PageId
                            if (buffHeader.remaining() >= 8) {
                                int numeroFichier = buffHeader.getInt(); // Numéro de fichier
                                int numeroPage = buffHeader.getInt(); // Numéro de page
                                pageDisponible = new PageId(numeroFichier, numeroPage);
                                pageTrouve = true;
                                System.out.println("Page disponible trouvée : " + pageDisponible);
                                break; // Page trouvée, sortir de la boucle
                            } else {
                                System.out.println("Pas assez d'octets pour lire le numéro de fichier et de page !");
                            }
                        }
                    } else {
                        System.out.println("Pas assez d'octets pour lire la case du slot directory !");
                        break; // Sortir si on ne peut pas lire les cases
                    }
                }
            } else {
                System.out.println("Pas assez d'octets pour lire le slot directory !");
                break; // Sortir si on ne peut pas lire les données nécessaires
            }

            // Mise à jour de la position dans le buffer (avancer de 12 octets par page)
            i++;
            currentPosition += 12; // Décalage de la position pour la prochaine page
            System.out.println("TEST getFreeDataPageId : positionPlaceDispo  " + currentPosition);

            // Vérifier si on dépasse la limite du buffer
            if (currentPosition < buffHeader.limit()) {
                buffHeader.position(currentPosition); // Mettre à jour la position dans le buffer
            } else {
                System.out.println("Dépassement de la capacité du buffer à la position " + currentPosition);
                break; // Sortir si on dépasse la capacité du buffer
            }
        }

        // Libérer la page après utilisation
        boolean dirtyHeaderPage = bufferManager.getDirtyPage(headerPageId);
        bufferManager.FreePage(headerPageId, dirtyHeaderPage);

        System.out.println("RELATION : getFreeDataPageId  : Page trouvé : " + pageDisponible);
        System.out.println("\n**************  Fin get free data pageId   *********************");

        return pageDisponible;
    }

    public RecordId writeRecordToDataPage(Record record, PageId pageId) throws IOException {

        System.out.println("\n**************  Ecriture d'un record dans la page de donnée   *********************");
        ByteBuffer buffData = bufferManager.GetPage(pageId); //On obtient un buffer du bufferManager qui prends en compte la page de donnée pageId

        int offsetNombreSlot = (int) diskManager.getDBConfig().getPagesize() -8; // on se positionne au premier octet du nombre de slot
        buffData.position(offsetNombreSlot);

        // On récupère le nombre de slot actuelle + la position pour écrire le record
        int nombreSlot = buffData.getInt();
        int positionEcrireRecord = buffData.getInt(); // la position pour écrire le record
        buffData.flip();
        int tailleRecord= writeRecordToBuffer(record,buffData,positionEcrireRecord); // On écrit le record dans le buffer qui représente la page de données + On sauvegarde la taille du record
        buffData.limit( buffData.capacity()); // on se remets une limite de la taille de la capacité du buffer, la limite avait changer à cuase de la fonction writeRecordToBuffer
        int positiontEcrireSlot = (int) diskManager.getDBConfig().getPagesize() -8 -nombreSlot*8 - 8; // nombre d'octet d'une page - les 8 octets (du nombre de slot + la position d'ecriture d'un futur record)  -m*8 octets représentes les  m slots - 8 (se laisser de la place pour ecrire la position et la taille du record)
        buffData.position(positiontEcrireSlot);
        buffData.putInt(positionEcrireRecord); // On ecrit la position du premier octet du record
        buffData.putInt(tailleRecord); // on écrit la taille du record

        // Mise à jour du nombre de cases m du slots (corresponds aux nombres de records) + réajustement de la position pour l'écriture d'un nouveau record
        buffData.position(offsetNombreSlot);
        buffData.putInt(nombreSlot+1);  // on augmente de 1 le nombre de cases
        buffData.putInt(positionEcrireRecord+tailleRecord);  // on se décale de la taille du record par rapport à la position
        buffData.flip();
        //System.out.println("Page de données "+Arrays.toString(buffData.array()));
        bufferManager.FreePage(pageId,true);

        // Il faut aussi modifier le nombre d'octet dispoible dans la case de page de données correspondate dans la headerPage

        int numeroFichier,numeroPage;
        boolean pageTrouve=false;
        ByteBuffer buffHeader = bufferManager.GetPage(headerPageId);
        int i=0,n=buffHeader.getInt(0);
        buffHeader.position(4); // On se positionne au premier octet qui décrit le fichier  de la première page de donnée
        while((i<n) && (!pageTrouve)){
            numeroFichier = buffHeader.getInt();    // numéro de fichier
            numeroPage = buffHeader.getInt();       // numéro de Page
            if ((pageId.getFileIdx()== numeroFichier) && (pageId.getPageIdx()== numeroPage)){
                pageTrouve=true; // va provoquer la fin de la boucle
                int octetRestantDispo = buffHeader.getInt();
                buffHeader.position(buffHeader.position()-4);
                buffHeader.putInt( octetRestantDispo- tailleRecord -8); // on déduit du nombre d'octet libre, la taille du record plus 8 octets pris par l'espace du slot +taille du record
                System.out.println("Octet Restant desormais: " +(octetRestantDispo - tailleRecord -8));
            }
            i++;
            if(buffHeader.position()+4 < buffHeader.capacity()){
                buffHeader.position(buffHeader.position()+4);
            }

        }
        //System.out.println("Header Page"+Arrays.toString(buffHeader.array()));
        bufferManager.FreePage(headerPageId,true);
        System.out.println("\n**************  FIN Ecriture d'un record dans la page de donnée   *********************");

        return new RecordId(pageId,nombreSlot+1); // retour du recordID
    }

    public ArrayList<Record> getRecordsInDataPage(PageId pageId) throws IOException {
        int nombreSlot; // le nombre de slot ,utile pour la boucle
        Record record ;
        ArrayList<Record> listeRecord = new ArrayList<>();

        ByteBuffer buffData = bufferManager.GetPage(pageId);
        buffData.position( (int) diskManager.getDBConfig().getPagesize() -8);
        nombreSlot = buffData.getInt();
        int currentPosition = buffData.position()-12; // la position de la première case indiquant la position du premier record à prendre
        buffData.position(currentPosition); // on se met à l'offset du slot
        int i=0;
        int positionRecord;
        while (i<nombreSlot){
            positionRecord= buffData.getInt();
            if (positionRecord!=-1){
                record = new Record();
                readFromBuffer(record,buffData,positionRecord);
                buffData.limit(buffData.capacity());
                listeRecord.add(record);
            }
            currentPosition-=8;
            buffData.position(currentPosition);
            i++;
        }

        boolean dirtyDataPage = bufferManager.getDirtyPage(pageId);
        bufferManager.FreePage(pageId,dirtyDataPage);

        return listeRecord;
    }

    public List<PageId> getDataPages() throws IOException {
        List<PageId> dataPages = new ArrayList<>();
        ByteBuffer buffHeader = bufferManager.GetPage(headerPageId);

        // Vérifiez si le buffer contient suffisamment de données pour lire nbDataPage
        if (buffHeader.remaining() < 4) {
            throw new IOException("Insufficient data to read nbDataPage.");
        }

        // Lecture brute de nbDataPage en tant qu'entier (4 octets)
        int nbDataPage = buffHeader.getInt();
        System.out.println("nbDataPage read from header: " + nbDataPage);

        // Vérification de la validité de nbDataPage
        if (nbDataPage <= 0 || nbDataPage > buffHeader.capacity() / 8) {
            throw new IOException("Invalid nbDataPage value: " + nbDataPage);
        }

        // Positionner le buffer pour lire les PageIds
        buffHeader.position(4); // Positionner à 4 octets après nbDataPage pour lire les PageIds

        // Lire les PageIds pour chaque page de données
        for (int i = 0; i < nbDataPage; i++) {
            if (buffHeader.remaining() < 8) {
                throw new IOException("Insufficient data to read PageId for data page " + i);
            }

            // Lire le fileId et le pageId
            int fid = buffHeader.getInt();
            int pid = buffHeader.getInt();
            PageId pageId = new PageId(fid, pid);
            dataPages.add(pageId);

            System.out.println("PageId trouvé : " + pageId);
        }

        // Libérer la page après utilisation
        boolean dirtyPage = bufferManager.getDirtyPage(headerPageId);
        bufferManager.FreePage(headerPageId, dirtyPage);

        return dataPages;
    }

    public RecordId InsertRecord(Record record) throws IOException {
        RecordId rid=null;  // initialisation du rid
        int octetCumulerRecord=0;
        for( ColInfo c : colonnes){ // on obtient la somme en terme d'octet que la colonne fait
            octetCumulerRecord +=c.getTaille();
        }
        if(varchar){ // Si il y a un varchar dans les colonnes, alors il faut ajouter les octets que vont prendre les n+1 int pour délimiter les cases
            octetCumulerRecord+= 4* (getNbColonnes()+1);
        }
        PageId pageDispo =getFreeDataPageId(octetCumulerRecord); // On cherche une page disponible
        System.out.println("Page Dispo : "+pageDispo);
        if (pageDispo!=null){ // Si une page est disponible, on "cris le contenu du record dans la page et n sauvegarde le rid
            rid =writeRecordToDataPage(record,pageDispo);
            System.out.println("Insertion du record réussi !!  "+rid);
        }else{
            System.out.println(" !!!! Erreur lors de l'insertion d'un record : Aucune page ne semble disponible (Insert Record) !!!!");
        }
        return rid; // retour du rid
    }

    public List<Record> GetAllRecords() throws IOException {
        System.out.println("\n**************  DEBUT Get All Records   *********************");

        List<Record> records = new ArrayList<>();

        // On obtient l'ensemble des pages de données de la relation, contenu dans la header page
        List<PageId> listePageDonnees = getDataPages();
        System.out.println("RELATION : Get All Records : liste des pages disponibles : " + listePageDonnees);

        if (listePageDonnees == null || listePageDonnees.isEmpty()) {
            System.out.println("Aucune page de données disponible dans la relation.");
            return records; // Retourne une liste vide si aucune page de données n'est trouvée
        }

        for (PageId pageDonnee : listePageDonnees) {
            // On parcourt l'ensemble des pages et on récupère les records pour chaque page
            List<Record> recordsInPage = getRecordsInDataPage(pageDonnee);
            if (recordsInPage != null) {
                records.addAll(recordsInPage); // Ajouter tous les records extraits de la page
            } else {
                System.out.println("Aucun record trouvé dans la page : " + pageDonnee);
            }
        }

        System.out.println("\n**************  Fin Get All Records   *********************");
        return records;
    }


    public int getNbColonnes() {
        return nbColonnes;
    }

    private boolean possedeUnVarchar(){
        boolean var=false;
        for( ColInfo Col :colonnes ){
            if( (Col.getType().equals("VARCHAR")) || (Col.getType().equals("varchar")) ) {
                var = true;
                break;
            }
        }
        return var;
    }
}



