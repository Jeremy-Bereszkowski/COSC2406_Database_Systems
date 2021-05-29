import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataNode extends Node {
    private final List<DataIndex> indexes = new ArrayList<>();
    private DataNode next = null;

    public DataNode(int id) {
        super(id);
    }

    public DataNode getNext() { return this.next; }
    void setNext(DataNode next) { this.next = next; }

    void addIndex(DataIndex index) { this.indexes.add(index); }
    public List<Index> getIndexes() { return Collections.unmodifiableList(this.indexes); }

    public String toFlatString() {
        StringBuilder string = new StringBuilder();
        string.append(getId());
        string.append("#");
        string.append(1);
        string.append("#");
        for (DataIndex index : indexes) {
            string.append(index.getIndex());
            string.append("%");
            string.append(index.getPageOffset());
            string.append("%");
            string.append(index.getRecordOffset());
            string.append(" ");
        }
        string.append("#");
        for (Node child : getChildren()) {
            string.append(child.getId());
            string.append(" ");
        }
        return string.toString();
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("DataNode: {\n");
        string.append("\tid: ");
        string.append(getId());
        string.append(",\n");
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
