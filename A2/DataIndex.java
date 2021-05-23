public class DataIndex {
    private String index;
    private int pageOffset, recordOffset;

    DataIndex(String index, int pageOffset, int recordOffset) {
        this.index = index;
        this.pageOffset = pageOffset;
        this.recordOffset = recordOffset;
    }

    public String getIndex() { return index; }
    public void setIndex(String index) { this.index = index; }

    public int getPageOffset() { return pageOffset; }
    public void setPageOffset(int pageOffset) { this.pageOffset = pageOffset; }

    public int getRecordOffset() { return recordOffset; }
    public void setRecordOffset(int recordOffset) { this.recordOffset = recordOffset; }
}
