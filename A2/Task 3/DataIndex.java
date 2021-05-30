public class DataIndex extends Index  {
    private int pageOffset, recordOffset;

    DataIndex(String index, int pageOffset, int recordOffset) {
        super(index);
        this.pageOffset = pageOffset;
        this.recordOffset = recordOffset;
    }

    public int getPageOffset() { return pageOffset; }

    public int getRecordOffset() { return recordOffset; }

    @Override
    public String toString() {
        return super.getIndex() + ", pageOffset=" + pageOffset + ", recordOffset=" + recordOffset;
    }
}
