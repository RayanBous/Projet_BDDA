import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class RelationScanner implements IRecordIterator {

    private Relation relationCourante;
    private List<PageId> dataPages;
    private int indexDataPageCourante;
    private int indexRecordCourant;
    private PageId pageCourante;
    private int tailleMaximalePage;
    private int nombreRecordPageCourante;

    public RelationScanner(Relation relationCourante){
        this.relationCourante = relationCourante;
        this.dataPages = this.relationCourante.getDataPages();
        this.indexDataPageCourante = -1;
        this.indexRecordCourant = 0;
        this.pageCourante = null;
        this.tailleMaximalePage = (int) (this.relationCourante.getDiskManager().getDBConfig().getPageSize());
    }

    public void loadNextPage(){
        this.indexDataPageCourante++;
        if(indexDataPageCourante < dataPages.size()) {
            this.pageCourante = dataPages.get(this.indexDataPageCourante);
            this.indexRecordCourant = 0;
        }   else{
            this.pageCourante = null;
        }
    }

    public Record getNextRecord () {
        if(this.pageCourante == null || this.indexRecordCourant >= nombreRecordPageCourante){
            this.loadNextPage();
            if(this.pageCourante == null){
                return null;
            }
        }
        MyBuffer bufferDataPage = null;
        try {
            bufferDataPage = relationCourante.getBufferManager().getPage(pageCourante);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (bufferDataPage == null){
            bufferDataPage = new MyBuffer(pageCourante, relationCourante.getDiskManager().getDBConfig().getPageSize(), relationCourante.getBufferManager().getTimeCount());
        }
        
        this.nombreRecordPageCourante = bufferDataPage.getInt(this.tailleMaximalePage-8);


        bufferDataPage.position((int) (this.tailleMaximalePage-8));
        ByteBuffer dataPage = ByteBuffer.wrap(bufferDataPage.getData());
        bufferDataPage.position((int) (this.tailleMaximalePage - ((indexRecordCourant+2)*8)));
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
        this.indexDataPageCourante = 0;
        this.indexRecordCourant = 0;
    }

    public void reset(){
        indexDataPageCourante = 0;
        indexRecordCourant = 0;
        pageCourante = dataPages.get(0);
    }

}