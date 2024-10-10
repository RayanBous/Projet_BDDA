import java.util.List;

public class Record {
    private List<Object> record;
    private PageId recordid;

    public Record(List<Object> record, PageId recordid) {
        this.record = record;
        this.recordid = recordid;
    }

    public Record(){
    }

    public List<Object> getRecord() {
        return record;
    }

    public PageId getRecordid() {
        return recordid;
    }

    public void setRecord(List<Object> record) {
        this.record = record;
    }

    public String toString(){
        return record.toString();
    }
}
