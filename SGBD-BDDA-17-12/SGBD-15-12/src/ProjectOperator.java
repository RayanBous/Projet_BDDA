import java.util.List;

public class ProjectOperator implements IRecordIterator {

    private SelectOperator selectOperator;
    private List<Integer> projectionAttributesIndex;

    public ProjectOperator(SelectOperator selectOperator, List<Integer> projectionAttributesIndex)    {
        this.selectOperator = selectOperator;
        this.projectionAttributesIndex = projectionAttributesIndex;
    }


    @Override
    public Record getNextRecord()   {
        Record record = this.selectOperator.getNextRecord();
        if (record == null || record.equals(new Record(new String [0])))  {
            return record;
        }
        String[] newRecord = new String[projectionAttributesIndex.size()];
        for (int i=0; i<projectionAttributesIndex.size(); i+=1) {
            newRecord[i] = record.getAttributs()[projectionAttributesIndex.get(i)]; 
        }
        return new Record(newRecord);
    }

    @Override
    public void close() {
        this.selectOperator.close();
    }

    @Override 
    public void reset() {
        this.selectOperator.close();
    }
}