import java.util.Comparator;

public class Index {
    private String index;

    Index(String index) {
        this.index = index;
    }

    public String getIndex() { return index; }

    public static Comparator<Index> IndexComparator = Comparator.comparing(Index::getIndex);

    @Override
    public String toString() {
        return index;
    }
}
