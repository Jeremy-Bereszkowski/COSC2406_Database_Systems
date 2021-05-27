import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;

public class DataNode extends Node {
    private final List<DataIndex> indexes = new ArrayList<>();
    private DataNode next = null;

    public DataNode getNext() { return this.next; }
    void resetNext() { this.next = null; }
    void setNext(DataNode next) { this.next = next; }

    void addIndex(DataIndex index) { this.indexes.add(index); }
    Boolean containsIndex(DataIndex index) { return this.indexes.contains(index); }
    public List<Index> getIndexes() { return Collections.unmodifiableList(this.indexes); }
    void removeIndex(DataIndex index) { this.indexes.remove(index); }
    void sortIndexes() {
        Collections.sort(indexes, new SortByIndex());
    }

    class SortByIndex implements Comparator<DataIndex> {
        public int compare(DataIndex a, DataIndex b) { return a.getIndex().compareTo(b.getIndex()); }
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("DataNode: {\n");
        string.append("\tindex: {\n");
        for (Index index : indexes) {
            string.append("\t\tindexes: \"").append(index.getIndex()).append("\",\n");
        }
        string.append("\t},\n");
        string.append("\tchildren: {\n");
        for (Node child : super.getChildren()) {
            string.append("\t\tindexes: ").append(child.getIndexes()).append(",\n");
        }
        string.append("\t}\n");
        string.append("\tnext: {\n");
        if (next != null) {
            for (Index index : next.getIndexes()) {
                string.append("\t\tindexes: ").append(index).append(",\n");
            }
        }
        string.append("\t}\n");
        string.append("}\n");
        return string.toString();
    }
}
