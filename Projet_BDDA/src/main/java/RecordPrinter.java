public class RecordPrinter {
    private SelectOperator selectOperator;

    public RecordPrinter(SelectOperator iRecordIterator) {
        this.selectOperator = iRecordIterator;
    }

    public SelectOperator getSelectOperator() {
        return selectOperator;
    }

    public void setSelectOperator(SelectOperator selectOperator) {
        this.selectOperator = selectOperator;
    }

    public String affichageTuples() {
        StringBuffer sb = new StringBuffer();
        IRecordIterator iterator = selectOperator;

        Record record;
        while ((record = iterator.getNextRecord()) != null) {
            sb.append(record.toString()).append("\n");
        }

        return sb.toString();
    }
}
