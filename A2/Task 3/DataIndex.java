import java.util.Comparator;

public class DataIndex extends Index  {
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
        return super.getIndex() + ", pageOffset=" + pageOffset + ", recordOffset=" + recordOffset;
    }

    public static Comparator<DataIndex> IndexComparator= new Comparator<DataIndex>() {
        public int compare(DataIndex i1, DataIndex i2) {
            return i1.getIndex().compareTo(i2.getIndex());
        }};
}
