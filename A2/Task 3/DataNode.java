import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataNode extends Node {
    private final List<DataIndex> indexes = new ArrayList<>();
    private DataNode next = null;

    public DataNode getNext() { return this.next; }
    void setNext(DataNode next) { this.next = next; }

    void addIndex(DataIndex index) { this.indexes.add(index); }
    public List<Index> getIndexes() { return Collections.unmodifiableList(this.indexes); }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("DataNode: {\n");
        string.append("\tindex: {\n");
        string.append(util.listToString(indexes, 2));
        string.append("\n\t},\n");
        string.append("\tchildren: {\n");
        for (Node child : super.getChildren()) {
            string.append(util.listToString(child.getIndexes(), 3));
            string.append('\n');
        }
        string.append("\t}\n");
        string.append("}\n");
        return string.toString();
    }
}
