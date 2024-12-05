import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

public class Relation {

    private String nom;
    private int nbColonnes;
    private ColInfo[] colonnes;
    private PageId headerPageId;
    private DiskManager diskManager;
    private BufferManager bufferManager;


    public Relation(String nom, int nbColonnes, ColInfo[] colonnes, PageId headerPageId, DiskManager diskManager, BufferManager bufferManager) {
        this.nom = nom;
        this.nbColonnes = nbColonnes;
        this.colonnes = colonnes;
        this.headerPageId = headerPageId;
        this.diskManager = diskManager;
        this.bufferManager = bufferManager;
    }

    public Relation(String nom, int nbColonnes, ColInfo[] colonnes, PageId headerPageId, DBConfig dbConfig) {
        this.nom = nom;
        this.nbColonnes = nbColonnes;
        this.colonnes = colonnes;
        this.headerPageId = headerPageId;
        this.diskManager = new DiskManager(dbConfig);
        this.bufferManager = new BufferManager(dbConfig, diskManager);
    }

    public Relation(String nom, int nbColonnes, PageId headerPageId, DiskManager diskManager, BufferManager bufferManager)	{
        this.nom = nom;
        this.nbColonnes = nbColonnes;
        this.colonnes = new ColInfo[this.nbColonnes];
        this.headerPageId = headerPageId;
        this.diskManager = diskManager;
        this.bufferManager = bufferManager;
    }

    public String getNom()  {
        return this.nom;
    }
    public void setNom(String nom)  {
        this.nom = nom;
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

    public PageId getHeaderPageId()    {
        return this.headerPageId;
    }
    public void setHeaderPageId(PageId headerPageId)    {
        this.headerPageId = headerPageId;
    }

    public DiskManager getDiskManager() {
        return this.diskManager;
    }
    public void setDiskManager(DiskManager diskManager)    {
        this.diskManager = diskManager;
    }

    public BufferManager getBufferManager() {
        return this.bufferManager;
    }
    public void setBufferManager(BufferManager bufferManager)  {
        this.bufferManager = bufferManager;
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
            positionIemeElement = buffer.getInt()+pos;
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
                    int positionIemePlusUnElement = buffer.getInt(pos+((i+1)*Integer.BYTES)) + pos;
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
        return buffer.getInt();
    }

    public void initializeHeaderPage()  {
        this.headerPageId = this.diskManager.AllocPage();
        MyBuffer bufferHeaderPage = null;
        try{
            bufferHeaderPage = bufferManager.getPage(this.headerPageId);
        }  catch(IOException e) {
            e.printStackTrace();
        }
        bufferHeaderPage.position(0);
        bufferHeaderPage.putInt(0);
        this.bufferManager.freePage(this.headerPageId, true);
    }

    public void addDataPage()   {
        // On doit commencer par instancier une nouvelle page de data en mémoire
        PageId newPage = this.diskManager.AllocPage();
        // On doit commencer par effectuer des modifications dans la header page
        // Pour ce faire on va charger un buffer sur cette page
        MyBuffer bufferHeaderPage = null;
        try{
            bufferHeaderPage = bufferManager.getPage(this.headerPageId);
        }  catch(IOException e) {
            e.printStackTrace();
        }
        if (bufferHeaderPage==null)   {
            bufferHeaderPage = new MyBuffer(this.headerPageId, this.diskManager.getDBConfig().getPagesize(), this.bufferManager.getTimeCount());
        }
        // Lire le nombre actuel de data pages avant l'ajout de notre nouvelle page
        int nbDataPages = bufferHeaderPage.getInt(0);
        // On doit faire un test pour voir si on peut encore rajouter une nouvelle page dans la HeaderPage...
        int sizeOfHeaderPage = 4 + (12*nbDataPages);
        int maxSizeOfPage = (int) (this.diskManager.getDBConfig().getPagesize());
        if (sizeOfHeaderPage+12 > maxSizeOfPage) {
            throw new IndexOutOfBoundsException("Erreur : Impossible d'ajouter une nouvelle page au header... le nombre de pages maximal a été atteint !!");
        }
        // Dans ce cas, on doit ajouter la nouvelle page, et donc on commence par ajuster le nb de pages
        bufferHeaderPage.putInt(0, nbDataPages+1);
        // On doit enregistrer notre nouvelle page dans le header page
        bufferHeaderPage.position(sizeOfHeaderPage);
        bufferHeaderPage.putInt(newPage.getFileIdx());
        bufferHeaderPage.putInt(newPage.getPageIdx());
        bufferHeaderPage.putInt(maxSizeOfPage-8); // On met -8 car notre nouvelle page contient au début deux entiers (2*4octets)
        // On libère le buffer de la HeaderPage en mentionnant qu'elle a été modifiée
        this.bufferManager.freePage(this.headerPageId, true);
        // On doit écrire nos deux entiers dans la nouvelle data page, on les insere en fin de page!
        MyBuffer bufferNewPage = null;
        try {
            bufferNewPage = bufferManager.getPage(newPage);
        }  catch(IOException e) {
            e.printStackTrace();
        }
        if (bufferNewPage==null)   {
            bufferNewPage = new MyBuffer(newPage, this.diskManager.getDBConfig().getPagesize(), this.bufferManager.getTimeCount());
        }
        bufferNewPage.position(maxSizeOfPage-8);
        bufferNewPage.putInt(0); // 0 correspond à la position à partir de laquelle commence l’espace libre sur la page
        bufferNewPage.putInt(0); // 0 correspond au nombre de records dans le directory
        this.bufferManager.freePage(newPage, true);
    }

    public PageId getFreeDataPageId(int sizeRecord)   {
        MyBuffer bufferHeaderPage = null;
        try {
            bufferHeaderPage = this.bufferManager.getPage(this.headerPageId);
        }  catch(IOException e) {
            e.printStackTrace();
        }
        // Dans ce cas où bufferHeaderPage==null, tous les buffers du bufferManager sont occupés, c'est à dire que leurs pincount>0
        // On crée un buffer vite fait histoire de l'utiliser pour lire le contenu de la headerPage
        if (bufferHeaderPage==null)   {
            bufferHeaderPage = new MyBuffer(this.headerPageId, this.diskManager.getDBConfig().getPagesize(), this.bufferManager.getTimeCount());
        }
        PageId freeDataPage = null;
        int nbDataPages = bufferHeaderPage.getInt(0);
        for (int i=0; i<nbDataPages; i+=1)  {
            int pageOffset = 4 + (i * 12);
            int fileIdx = bufferHeaderPage.getInt(pageOffset);
            int pageIdx = bufferHeaderPage.getInt(pageOffset + 4);
            int availableSpace = bufferHeaderPage.getInt(pageOffset + 8);
            if (availableSpace >= sizeRecord+8) {
                freeDataPage = new PageId(fileIdx, pageIdx);
                break ;
            }
        }
        this.bufferManager.freePage(this.headerPageId, false);
        return freeDataPage;
    }

    public RecordId writeRecordToDataPage (Record record, PageId pageId)    {
        long pageSize = this.diskManager.getDBConfig().getPagesize();
        int recordSize = this.getRecordSize(record);
        boolean bufferProvisoire = false;
        MyBuffer bufferDataPage = null;
        try {
            bufferDataPage = this.bufferManager.getPage(pageId);
        }  catch(IOException e) {
            e.printStackTrace();
        }
        // Dans ce cas où bufferHeaderPage==null, tous les buffers du bufferManager sont occupés, c'est à dire que leurs pincount>0
        // On crée un buffer provisoirement pour l'utiliser à lire le contenu de la Data Page
        if (bufferDataPage == null)   {
            bufferDataPage = new MyBuffer(pageId, pageSize, this.bufferManager.getTimeCount());
            bufferProvisoire = true;
        }
        // On récupère la position de l'espace libre de la data page
        int freeSpacePosition = bufferDataPage.getInt((int) (pageSize-4));
        // On écrit le contenu du record dans le buffer
        ByteBuffer dataRecordByteBuffer = ByteBuffer.allocate(recordSize+((this.nbColonnes+1)*4));
        int nbBytesWritten = this.writeRecordToBuffer(record, dataRecordByteBuffer, 0);
        byte[] dataRecord = dataRecordByteBuffer.array();
        for (int index=0; index<nbBytesWritten; index+=1) {
            bufferDataPage.setData(index+freeSpacePosition, dataRecord[index]);
        }
        // mettre à jour le slot directory
        int M = bufferDataPage.getInt((int) (pageSize-8));
        bufferDataPage.position((int) (pageSize-((M+2)*8)));
        bufferDataPage.putInt(freeSpacePosition);
        bufferDataPage.putInt(nbBytesWritten);
        // Ajuster le nb de records M
        bufferDataPage.position((int) (pageSize-8));
        bufferDataPage.putInt(M+1);
        // Ajuster la position de l'espace libre
        bufferDataPage.putInt(freeSpacePosition+nbBytesWritten);
        if (bufferProvisoire)   {
            try{
                diskManager.WritePage(pageId, bufferDataPage.getData());
            } catch(IOException e){
                e.printStackTrace();
            }
        }
        this.bufferManager.freePage(pageId, true);
        return new RecordId(pageId, M);
    }


    public int getRecordSize(Record record)    {
        int recordSize = 0;
        int index=0;
        for (ColInfo c : this.colonnes)     {
            switch (c.getType())    {
                case "INT" :
                    recordSize += Integer.BYTES;
                    break ;
                case "REAL" :
                    recordSize += Integer.BYTES;
                    break ;
                case "CHAR" :
                    recordSize += (Character.BYTES * c.getTaille());
                    break ;
                case "VARCHAR" :
                    recordSize += (Character.BYTES * record.getAttributs()[index].length());
                    break ;
            }
            index+=1;
        }
        return recordSize;
    }

    public List<Record> getRecordsInDataPage(PageId pageId)    {
        long pageSize = this.diskManager.getDBConfig().getPagesize();
        MyBuffer bufferDataPage = null;
        try {
            bufferDataPage = this.bufferManager.getPage(pageId);
        }  catch(IOException e) {
            e.printStackTrace();
        }
        if (bufferDataPage==null)   {
            bufferDataPage = new MyBuffer(pageId, pageSize, this.bufferManager.getTimeCount());
        }
        List<Record> records = new ArrayList<>();
        bufferDataPage.position((int) (pageSize-8));
        int nbRecords = bufferDataPage.getInt();
        ByteBuffer dataPage = ByteBuffer.wrap(bufferDataPage.getData());
        for (int indexRecord=0; indexRecord<nbRecords; indexRecord+=1)  {
            // pour chaque record, on va lire la position de début du record, et la taille de ce dernier
            bufferDataPage.position((int) (pageSize - ((indexRecord+2)*8)));
            int recordstartPosition = bufferDataPage.getInt();
            int recordSize = bufferDataPage.getInt();
            Record record = new Record(this.nbColonnes);
            int recordSize2 = this.readFromBuffer(record, dataPage, recordstartPosition);
            records.add(record);
        }
        this.bufferManager.freePage(pageId, false);
        return records;
    }

    public List<PageId> getDataPages()  {
        List<PageId> pages = new ArrayList<>();
        System.out.println("DBCONFIG : " + this.diskManager.getDBConfig());
        long pageSize = this.diskManager.getDBConfig().getPagesize();
        MyBuffer bufferHeaderPage = null;
        try {
            bufferHeaderPage = this.bufferManager.getPage(this.headerPageId);
        }  catch(IOException e) {
            e.printStackTrace();
        }
        if (bufferHeaderPage==null)   {
            bufferHeaderPage = new MyBuffer(this.headerPageId, pageSize, this.bufferManager.getTimeCount());
        }
        int nbPages = bufferHeaderPage.getInt(0);
        for (int pageIndex=0; pageIndex<nbPages; pageIndex+=1)  {
            bufferHeaderPage.position(4 + (pageIndex * 12));
            int fileIdx = bufferHeaderPage.getInt();
            int pageIdx = bufferHeaderPage.getInt();
            if (fileIdx>=0 && pageIdx>=0)   {
                pages.add(new PageId(fileIdx, pageIdx));
            }
        }
        bufferManager.freePage(this.headerPageId, false);
        return pages;
    }

    public RecordId InsertRecord(Record record) {
        PageId pageId = this.getFreeDataPageId(this.getRecordSize(record));
        if (pageId == null) {
            addDataPage();
            pageId = this.getFreeDataPageId(this.getRecordSize(record));
        }
        return this.writeRecordToDataPage(record, pageId);
    }

    public List<Record> GetAllRecords() {
        List<Record> allRecords = new ArrayList<>();
        List<PageId> pages = this.getDataPages();
        for (PageId pageId : pages)   {
            List<Record> record = this.getRecordsInDataPage(pageId);
            allRecords.addAll(record);
        }
        return allRecords;
    }

}