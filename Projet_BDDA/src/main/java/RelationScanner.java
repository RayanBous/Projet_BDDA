import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class RelationScanner implements IRecordIterator {

    private List<Record> records;
    private Relation relationCourante;
    private List<PageId> dataPages;
    private int indexDataPageCourante;
    private int indexRecordCourant;
    private PageId pageCourante;
    private int tailleMaximalePage;
    private int nombreRecordPageCourante;

    public RelationScanner(Relation r){

        relationCourante =r;
        dataPages = r.getDataPages();
        this.indexDataPageCourante = 0;
        this.indexRecordCourant = 0;
        this.pageCourante = null;
        tailleMaximalePage = (int) (r.getDiskManager().getDBConfig().getPagesize());

    }

    public void loadNextPage(){
        indexDataPageCourante++;
        if(indexDataPageCourante < dataPages.size()){
            pageCourante = dataPages.get(indexDataPageCourante);
            indexRecordCourant = 0;
        }else{
            pageCourante = null;
        }
    }

    public Record getNextRecord(){
        MyBuffer bufferPageCourante = null;

        try {
            bufferPageCourante = relationCourante.getBufferManager().getPage(pageCourante);

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(bufferPageCourante == null){
            bufferPageCourante = new MyBuffer(pageCourante, relationCourante.getDiskManager().getDBConfig().getPagesize(), relationCourante.getBufferManager().getTimeCount());
        }
        nombreRecordPageCourante = bufferPageCourante.getInt(tailleMaximalePage-8);

        if(pageCourante == null || indexRecordCourant >= nombreRecordPageCourante){
            loadNextPage();
            if(pageCourante == null){
                return null;
            }
        }

        MyBuffer bufferDataPage = null;
        try {
            bufferDataPage = relationCourante.getBufferManager().getPage(pageCourante);
        }  catch(IOException e) {
            e.printStackTrace();
        }

        if (bufferDataPage==null)   {
            bufferDataPage = new MyBuffer(pageCourante, tailleMaximalePage, relationCourante.getBufferManager().getTimeCount());
        }

        bufferDataPage.position((int) (tailleMaximalePage-8));
        ByteBuffer dataPage = ByteBuffer.wrap(bufferDataPage.getData());
        bufferDataPage.position((int) (tailleMaximalePage - (indexRecordCourant+2)*8));
        int recordstartPosition = bufferDataPage.getInt();

        int recordSize = bufferDataPage.getInt();
        Record record = new Record(relationCourante.getNbColonnes());

        int recordSize2 = relationCourante.readFromBuffer(record, dataPage, recordstartPosition);
        relationCourante.getBufferManager().freePage(pageCourante, false);
        indexRecordCourant++;

        return record;
    }

    public void close(){
        pageCourante = null;
        indexDataPageCourante = 0;
        indexRecordCourant = 0;

    }

    public void reset(){
        indexDataPageCourante = 0;
        indexRecordCourant = 0;
        pageCourante = dataPages.get(indexDataPageCourante);

    }

}
