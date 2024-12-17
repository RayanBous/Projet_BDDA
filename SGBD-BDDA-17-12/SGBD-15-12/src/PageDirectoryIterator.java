import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PageDirectoryIterator implements IRecordIterator{
    private Relation relation;
    private PageId pageId;
    private int indexRecord;
    private MyBuffer myBuffer;
    private List<Record> records;
    private BufferManager bufferManager;

    public PageDirectoryIterator(Relation relation, PageId pageId, BufferManager bufferManager) {
        this.relation = relation;
        this.pageId = pageId;
        this.bufferManager = bufferManager;
        this.records = new ArrayList<Record>();
        this.indexRecord = 0;
        chargeDataPage(pageId);
    }

    private void chargeDataPage(PageId pageId){
        try{
            myBuffer = bufferManager.getPage(pageId);
            this.records = relation.getRecordsInDataPage(pageId);
            this.indexRecord = 0;
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private PageId getNextPageId() {
        int pageParFichier = 3; //J'ai fais 12287/3 = 4096
        int fileIdx = pageId.getFileIdx();
        int pageIdx = pageId.getPageIdx();

        pageIdx = pageIdx + 1;
        if(pageIdx > pageParFichier){
            pageIdx = 0;
            fileIdx = fileIdx + 1;
        }

        return new PageId(pageIdx, fileIdx);
    }

    public PageId getNextDataPageId(){
        if(indexRecord < records.size()){
            return pageId;
        }

        PageId prochainePageId = getNextPageId();
        if(prochainePageId == null){
            chargeDataPage(prochainePageId);
        }
        bufferManager.freePage(prochainePageId, false);
        return prochainePageId;
    }

    public Record getNextRecord(){
        if(indexRecord < records.size()){
            // On cherche à passer à l'enregistrement suivant
            return records.get(indexRecord++);
        }

        PageId prochainePageId = getNextPageId();
        if(prochainePageId == null){
            return getNextRecord();
        }
        return null;
    }

    public void reset(){
        this.pageId = relation.getHeaderPageId();
        this.indexRecord = 0;
        chargeDataPage(pageId);
    }

    public void close(){
        if(myBuffer != null){
            bufferManager.freePage(pageId, false);
        }
    }
}