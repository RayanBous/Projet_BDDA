public class SelectOperator implements IRecordIterator{
    private IRecordIterator recordIterator;
    private Condition condition;
    private Record record;

    public SelectOperator(IRecordIterator recordIterator, Condition condition) {
        this.recordIterator = recordIterator;
        this.condition = condition;
    }

    @Override
    public Record getNextRecord(){
        while((record = recordIterator.getNextRecord()) != null){
            if(condition.test(record)){
                return record;
            }
        }
        return null;
    }

    @Override
    public void close(){
        recordIterator.close();
    }

    public void reset(){
        recordIterator.reset();
    }
}


