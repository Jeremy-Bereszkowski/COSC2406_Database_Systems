import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TreeNode extends Node {
    private final List<Index> indexes = new ArrayList<>();

    void addIndex(Index index) { this.indexes.add(index); }
    Boolean containsIndex(Index index) { return this.indexes.contains(index); }
    public List<Index> getIndexes() { return Collections.unmodifiableList(this.indexes); }
    void removeIndex(Index index) { this.indexes.remove(index); }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("TreeNode: {\n");
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
        string.append("}\n");
        return string.toString();
    }
}
