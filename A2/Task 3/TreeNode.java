import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TreeNode extends Node {
    private final List<Index> indexes = new ArrayList<>();
    private final List<Integer> childrenIds = new ArrayList<>();

    public TreeNode(int id) {
        super(id);
    }

    void addIndex(Index index) { this.indexes.add(index); }
    public List<Index> getIndexes() { return Collections.unmodifiableList(this.indexes); }

    void addChildId(Integer childId) { this.childrenIds.add(childId); }
    List<Integer> getChildrenIds() { return Collections.unmodifiableList(this.childrenIds); }

    @Override
    public Node getNext() {
        return null;
    }

    public String toFlatString() {
        StringBuilder string = new StringBuilder();
        string.append(getId());
        string.append(util.NODE_FIELD_DELIMITER);
        string.append(0);
        string.append(util.NODE_FIELD_DELIMITER);
        for (Index index : getIndexes()) {
            string.append(index.getIndex());
            string.append(" ");
        }
        string.append(util.NODE_FIELD_DELIMITER);
        for (Node child : getChildren()) {
            string.append(child.getId());
            string.append(" ");
        }
        return string.toString();
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("TreeNode: {\n");
        string.append("\tid: ");
        string.append(getId());
        string.append("\n");
        string.append("\tindex: {\n");
        string.append(util.listToString(indexes, 2));
        string.append("\n\t},\n");
        string.append("\tchildren: {\n");
        for (Node child : super.getChildren()) {
            string.append(child.getId());
            string.append('\n');
        }
        string.append("\t}\n");
        string.append("}\n");
        return string.toString();
    }
}
