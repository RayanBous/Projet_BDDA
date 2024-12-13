import java.util.*;

public class ProjectOperator implements IRecordIterator{

    private SelectOperator selectOperator;
    private List<Integer> colonne;

    public ProjectOperator(SelectOperator selectOperator){
        this.selectOperator = selectOperator;
    }

    public ProjectOperator(SelectOperator selectOperator, List<Integer> colonne){
        this.selectOperator = selectOperator;
        this.colonne = colonne;
    }

    public SelectOperator getSelectOperator() {
        return selectOperator;
    }

    public void setSelectOperator(SelectOperator selectOperator) {
        this.selectOperator = selectOperator;
    }

    @Override
    public Record getNextRecord() {
        Record record = this.selectOperator.getNextRecord();
        if(record == null){
            System.err.println("getNextRecord : Le record n'a pas été trouver");
            return null;
        }
        //Le record1 correspond au record que l'on va projeter
        Record record1 = new Record(colonne.size());
        for(int i = 0; i < this.colonne.size(); i++){
            int index = this.colonne.get(i);
            record1.setValue("colonne : " + index, record.getValue("colonne : " + index));
        }
        return record1;
    }

    @Override
    public void close(){
        selectOperator.close();
    }

    @Override
    public void reset(){
        selectOperator.reset();
    }
}
