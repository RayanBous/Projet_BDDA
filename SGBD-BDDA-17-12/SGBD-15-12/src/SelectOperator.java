import java.util.List;

import javax.management.relation.Relation;

public class SelectOperator implements IRecordIterator  {
    
    private RelationScanner relationScanner;
    private List<Condition> conditions;

    public RelationScanner getRelationScanner() {
        return relationScanner;
    }
    public void setRelationScanner(RelationScanner relationScanner) {
        this.relationScanner = relationScanner;
    }

    public List<Condition> getConditions() {
        return conditions;
    }
    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public SelectOperator(RelationScanner relationScanner, List<Condition> conditions) {
        this.relationScanner = relationScanner;
        this.conditions = conditions;
    }

    @Override
    public Record getNextRecord()   {
        Record record =  this.relationScanner.getNextRecord();
        if (record == null) {
            return record;
        }
        for (Condition condition : conditions)  {
            if (!condition.evaluate(record.getAttributs()[condition.getIndexColonne()]))
                return new Record(new String[0]);
        }
        return record;
    }

    @Override
    public void close() {
        this.relationScanner.close();
    }

    @Override 
    public void reset() {
        this.relationScanner.reset();
    }
}