import java.util.List;

public class ProjectOperator implements IRecordIterator{
    private IRecordIterator recordIterator;
    private List<String> colonneProjection;
    private Record record;

    public ProjectOperator(IRecordIterator recordIterator, List<String> colonneProjection) {
        this.recordIterator = recordIterator;
        this.colonneProjection = colonneProjection;
    }

    private Record project(Record record) {
        Record projectedRecord = new Record();  // Créer un nouveau record pour les colonnes projetées
        for (String column : colonneProjection) {
            // Copier uniquement les colonnes spécifiées dans la liste des colonnes à projeter
            projectedRecord.setValue(column, record.getValue(column));
        }
        return projectedRecord;
    }

    public Record getNextRecord() {
        while((record = recordIterator.getNextRecord()) != null){
            return project(record);
        }
        return null;
    }

    public void close(){
        recordIterator.close();
    }

    public void reset(){
        recordIterator.reset();
    }
}
