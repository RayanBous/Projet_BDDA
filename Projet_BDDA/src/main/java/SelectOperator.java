import java.util.ArrayList;
import java.util.List;

public class SelectOperator implements IRecordIterator{

    private RelationScanner selectOperator;
    List<Condition> condition;

    public SelectOperator(RelationScanner selectOperator) {
        this.selectOperator = selectOperator;
        this.condition = new ArrayList<Condition>();
    }

    public SelectOperator(RelationScanner selectOperator, List<Condition> condition) {
        this.selectOperator = selectOperator;
        this.condition = condition;
    }

    public RelationScanner getSelectOperator() {
        return selectOperator;
    }

    public void setSelectOperator(RelationScanner selectOperator) {
        this.selectOperator = selectOperator;
    }

    @Override
    public Record getNextRecord()
    {
        Record record;
        while((record = selectOperator.getNextRecord()) != null)
        {
            if(conditionValide(record)){
                return record;
            }

        }
        return null;
    }

    public boolean conditionValide(Record record) {
        for (Condition conditions : condition) {
            if (conditions.getValeurConstante() != null) {

                if (conditions.evaluation(record.getColonnes()[conditions.getIndexColonne()]) == false) {
                    return false;

                }
            }
        }
        return true;
    }

    public void close(){
        this.selectOperator.close();
    }

    @Override
    public void reset(){
        this.selectOperator.reset();
    }


}
