import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Relation {
    private String nom;
    private int nbColonnes;
    private ColInfo[] colonnes;
    private PageId headerPageId;
    private DiskManager diskManager;
    private BufferManager bufferManager;
    private boolean varchar;

    public Relation(String nom, int nbColonnes, ColInfo[] colonnes, PageId headerPageId, DiskManager diskManager, BufferManager bufferManager, boolean var) {
        this.nom = nom;
        this.nbColonnes = nbColonnes;
        this.colonnes = colonnes;
        this.headerPageId = headerPageId;
        this.diskManager = diskManager;
        this.bufferManager = bufferManager;
        this.varchar = var;
    }

    public Relation(String nom, int nbColonnes)	{
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

    public BufferManager getBufferManager() {
        return bufferManager;
    }

    public void setBufferManager(BufferManager bufferManager) {
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
        return this.headerPageId;
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
        // Étape 1 : Allouer une nouvelle page via AllocPage du DiskManager
        PageId newPageId = diskManager.AllocPage();
        // Étape 2 : Charger la header page dans un buffer pour la mettre à jour
        NewBuffer buffer = bufferManager.GetPage(headerPageId);
        // Étape 3 : Lire le nombre actuel de pages de données dans la relation
        int numPages = buffer.getInt(0);  // Le nombre de pages est stocké au début de la page
        int offset = 4 + (numPages * 12); // Chaque entrée du Page Directory prend 12 octets (2x PageId + espace libre)
        // Étape 4 : Ajouter la nouvelle page dans le Page Directory
        buffer.position(offset);
        buffer.putInt(newPageId.getFileIdx());  // ID de fichier de la nouvelle page
        buffer.putInt(newPageId.getPageIdx());  // Numéro de page de la nouvelle page
        buffer.putInt(diskManager.getDBConfig().getPagesize()); // Espace disponible sur cette nouvelle page (au départ, la taille d'une page entière)
        // Étape 5 : Mettre à jour le nombre total de pages
        buffer.putInt(numPages + 1);  // Incrémenter le nombre de pages dans le header
        // Étape 6 : Libérer la header page (indiquant qu'elle est modifiée)
        bufferManager.FreePage(newPageId, offset);
    }

    public PageId getFreeDataPageId(int sizeRecord) throws IOException {
        NewBuffer buffer = bufferManager.GetPage(headerPageId);
        PageId pageid = null;
        int numPages = buffer.getInt(0);
        int offset = 4;
        for(int i=0; i<numPages; i++)  {
            // Calculer l'offset pour accéder à chaque entrée du répertoire (12 octets par entrée)
            int pageOffset = offset + i * 12;

            // Lire l'ID de la page (fichier et index de page)
            int fileIdx = buffer.getInt(pageOffset);          // ID du fichier de la page
            int pageIdx = buffer.getInt(pageOffset + 4);      // Index de la page
            int availableSpace = buffer.getInt(pageOffset + 8); // Espace libre sur cette page

            // Si l'espace libre est suffisant pour insérer le record, retourner le PageId
            if (availableSpace >= sizeRecord) {
                pageid = new PageId(fileIdx, pageIdx); // Créer et retourner le PageId
                break;  // Sortir de la boucle une fois qu'une page adéquate est trouvée
            }
        }
        return pageid;
    }

    public RecordId writeRecordToDataPage(Record record, PageId pageId) throws IOException {
        // Récupérer le buffer de la page de données
        NewBuffer buffer = bufferManager.GetPage(pageId);

        // Calculer l'espace disponible pour insérer le record
        int espacedispo = buffer.getInt(0);  // L'espace libre est stocké au début de la page
        int offset = 4096 - espacedispo;     // Décalage à partir de la fin de la page

        // Sérialiser les données du record (ici, on assume que les colonnes sont stockées sous forme de chaîne de caractères)
        String[] colonnes = record.getColonnes();
        byte[] recordData = serializeRecord(colonnes); // On suppose que serializeRecord est une méthode qui convertit les colonnes en tableau d'octets

        // Écrire les données dans le buffer à l'offset calculé
        for (int i = 0; i < recordData.length; i++) {
            buffer.put(recordData[i]);  // Insérer chaque octet du record à l'offset calculé
        }

        // Mettre à jour l'espace disponible
        int updatedEspacedispo = espacedispo - recordData.length;
        buffer.putInt( updatedEspacedispo);  // Mettre à jour l'espace libre dans le buffer (au début de la page)

        // Créer un nouveau RecordId avec le pageId et le slotIdx (le slotIdx est l'offset où le record a été inséré)
        RecordId recordId = new RecordId(pageId, offset);

        // Associer le RecordId au record
        record.setRecordId(recordId);

        // Retourner le RecordId de l'enregistrement inséré
        return recordId;
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
        NewBuffer buffer = bufferManager.GetPage(pageId);  // Récupérer la page via le bufferManager

        // Lire l'espace libre au début de la page
        int espacedispo = buffer.getInt(0);
        int offset = 4;

        while (offset < 4096 - espacedispo) {
            int numColumns = buffer.getInt(offset);
            // On décale de 4 octets
            offset += 4;

            String[] colonnes = new String[numColumns];

            // Lire chaque colonne comme une chaîne de caractères
            for (int i = 0; i < numColumns; i++) {
                int stringLength = buffer.getInt(offset);
                offset += 4; // Avancer de 4 octets pour la longueur

                // Lire les octets de la chaîne
                byte[] stringBytes = new byte[stringLength];
                for (int j = 0; j < stringLength; j++) {
                    stringBytes[j] = buffer.get(offset);
                    offset++;
                }

                // Convertir les octets en chaîne de caractères
                String columnValue = new String(stringBytes);
                colonnes[i] = columnValue;
            }

            Record record = new Record(colonnes);

            RecordId recordId = new RecordId(pageId, offset);  // Utiliser l'offset comme identifiant
            record.setRecordId(recordId);

            // Ajouter le Record à la liste des records
            records.add(record);
        }

        // Libérer la page après lecture
        bufferManager.FreePage(pageId, offset);

        // Retourner la liste des records lus
        return records;
    }

    public List<PageId> getDataPages() throws IOException {
        List<PageId> res = new ArrayList<>();
        PageId headerPageId = getHeaderPageId();

        // On charge la header page à partir du BufferManager
        NewBuffer buffer = bufferManager.GetPage(headerPageId);

        int offset = 0;
        int numeroDataPages = buffer.getInt(offset);
        offset += 4;

        for (int i = 0; i < numeroDataPages; i++) {
            int fileIdx = buffer.getInt(offset);
            offset += 4; // On avance de 4 octets
            int pageIdx = buffer.getInt(offset);
            offset += 4;

            PageId pageId = new PageId(fileIdx, pageIdx);
            res.add(pageId);
        }
        bufferManager.FreePage(headerPageId, offset);
        return res;
    }

    public List<Record> getAllRecords() throws IOException {
        List<Record> res = new ArrayList<>();
        List<PageId> pageIds = getDataPages();
        for(PageId pageId : pageIds) {
            res.addAll(getRecordsInDataPage(pageId));
        }
        return res;
    }

    public RecordId insertRecord(Record record) throws IOException {
        RecordId recordId = null;
        int octetaccumler = 0;
        for(ColInfo c : colonnes){
            octetaccumler = octetaccumler + c.getTaille();
        }
        System.out.println("Page disponible : ");
        PageId pagedisponible = getFreeDataPageId(octetaccumler);

        if(varchar == true){
            octetaccumler = octetaccumler + (4*getNbColonnes());
        }
        if(pagedisponible == null){
            System.err.println("On peut pas insérer le Record");
        }else{
            System.out.println("Voici l'insertion du record");
            recordId = writeRecordToDataPage(record, pagedisponible);
        }
        return recordId;
    }

    private boolean verifVarchar(){
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

    /*public void addDataPage2() throws IOException {
        // Allouer une nouvelle page via AllocPage du DiskManager
        PageId newPageId = diskManager.AllocPage();
        // Initialiser une nouvelle page de données (buffer)
        NewBuffer newBuffer = new NewBuffer(newPageId, 0);
        byte[] initialPageData = newBuffer.toBytes();
        ByteBuffer byteBuffer = ByteBuffer.wrap(initialPageData);
        diskManager.WritePage(headerPageId, initialPageData);
    }*/

}