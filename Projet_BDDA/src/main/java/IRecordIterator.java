public interface IRecordIterator {

    public Record getNextRecord();

    public void close();

    public void reset();
}
