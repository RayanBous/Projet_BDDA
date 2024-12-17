import java.io.IOException;

public class DataPageHoldRecordIterator implements IRecordIterator{
    private PageId pageId;
    private MyBuffer myBuffer;
    private int offsetRecord;
    private int nbRecords;
    private BufferManager bufferManager;
    private Relation relation;

    public DataPageHoldRecordIterator(PageId pageId, BufferManager bufferManager, Relation relation) {
        this.pageId = pageId;
        this.bufferManager = bufferManager;
        this.relation = relation;
        this.offsetRecord = 0;
        this.nbRecords = 0;
        try{
            this.myBuffer = bufferManager.getPage(pageId);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /*Je redéfinie cette méthode pour vérifier si il y'a encore des
    enregistrement à énumérer
     */

    private void calculNbRecord(){
        int taille_record = relation.getRecordSize(null);
        int taille_page = (int) myBuffer.getPageSize();
        System.out.println("calculNbRecord : Calcul de la taille des records....");
        nbRecords = taille_record - taille_page;
    }

    public boolean hasNext(){
        return offsetRecord < nbRecords;
    }

    @Override
    public Record getNextRecord(){
        if(!hasNext()){
            return null;
        }

        int taille_record = relation.getRecordSize(null);

        //Je lis et je récupère les données du record, qui sont contenu dans le buffer
        int recOffset = offsetRecord * taille_record;
        String[] dataRecord = new String[taille_record];
        myBuffer.position(recOffset);
        myBuffer.get();

        Record record = new Record(dataRecord);
        offsetRecord = offsetRecord + 1;

        return record;
    }

    @Override
    public void reset(){
        offsetRecord = 0;
    }

    @Override
    public void close(){
        bufferManager.freePage(pageId, false);
    }
}