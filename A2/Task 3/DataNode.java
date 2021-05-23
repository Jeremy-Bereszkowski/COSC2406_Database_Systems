import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;

public class DataNode extends Node {
    private final List<DataIndex> indexes = new ArrayList<>();
    private DataNode next = null;

    DataNode getNext() { return this.next; }
    void resetNext() { this.next = null; }
    void setNext(DataNode next) { this.next = next; }

    void addIndex(DataIndex index) { this.indexes.add(index); }
    Boolean containsIndex(DataIndex index) { return this.indexes.contains(index); }
    List<DataIndex> getIndexes() { return Collections.unmodifiableList(this.indexes); }
    void removeIndex(DataIndex index) { this.indexes.remove(index); }
    void sortIndexes() {
        Collections.sort(indexes, new SortByIndex());
    }

    class SortByIndex implements Comparator<Student> {
        public int compare(DataIndex a, DataIndex b) { return a.index - b.index; }
    }
}
