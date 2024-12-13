import java.util.*;

public class RelationScannerWithSelect implements IRecordIterator {
    private SelectOperator selectOp;

    public RelationScannerWithSelect(List<Condition> c, Relation r) {
        selectOp = new SelectOperator(new RelationScanner(r),c);
    }

    public Record getNextRecord() {
        Record record;
        while((record = selectOp.getNextRecord()) != null)
        {
            return record;
        }
        return null;
    }

    public void close() {
        selectOp.close();
    }

    public void reset() {
        selectOp.reset();
    }

}
