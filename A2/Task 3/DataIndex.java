public class DataIndex extends Index {
    private int pageOffset, recordOffset;

    DataIndex(String index, int pageOffset, int recordOffset) {
        super(index);
        this.pageOffset = pageOffset;
        this.recordOffset = recordOffset;
    }

    public int getPageOffset() { return pageOffset; }
    public void setPageOffset(int pageOffset) { this.pageOffset = pageOffset; }

    public int getRecordOffset() { return recordOffset; }
    public void setRecordOffset(int recordOffset) { this.recordOffset = recordOffset; }

    @Override
    public String toString() {
        return "\n\t\t\t" + super.getIndex() + ", pageOffset=" + pageOffset + ", recordOffset=" + recordOffset;
    }
}
