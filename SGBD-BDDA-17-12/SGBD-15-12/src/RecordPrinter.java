
import java.util.ArrayList;

public class RecordPrinter {
    private Relation relation;
    private ArrayList<Condition> conditions;
    private ArrayList<Integer> projectionAttributtesIndex;

    public RecordPrinter (Relation relation, ArrayList<Condition> conditions, ArrayList<Integer> projectionAttributtesIndex)    {
        this.relation = relation;
        this.conditions = conditions; 
        this.projectionAttributtesIndex = projectionAttributtesIndex;
    }

    public ArrayList<Record> loadRecords() {
        ArrayList<Record> records  = new ArrayList<>();
        RelationScanner relationScanner = new RelationScanner(this.relation);
        SelectOperator selectOperator = new  SelectOperator(relationScanner, this.conditions);
        ProjectOperator projectOperator = new ProjectOperator(selectOperator, this.projectionAttributtesIndex);
        Record currentRecord = null;
        while ((currentRecord = projectOperator.getNextRecord()) != null)   {
            if (! currentRecord.equals(new Record(new String[0])))    {
                records.add(currentRecord);
            }
        }
        return records;
    }

    public void printRecords()  {
        ArrayList<Record> records = this.loadRecords();
        for(int i=0 ; i<this.projectionAttributtesIndex.size() ; i+=1)   {
            System.out.print(this.relation.getColonnes()[this.projectionAttributtesIndex.get(i)].getNom()  +  ((i!=this.projectionAttributtesIndex.size()-1) ? " , " : " .\n"));
        }
        for (Record record : records) {
            System.out.println(record);
        }
        System.out.println("Total Records = " + records.size());
    }
}
