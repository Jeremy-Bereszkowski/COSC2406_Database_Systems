import java.util.Comparator;

public class Index {
    private String index;

    Index(String index) {
        this.index = index;
    }

    public String getIndex() { return index; }

    public static Comparator<Index> IndexComparator = (i1, i2) -> i1.getIndex().compareTo(i2.getIndex());

    @Override
    public String toString() {
        return index;
    }
}
