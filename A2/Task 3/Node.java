import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Node {
    private final List<Node> children = new ArrayList<>();
    private int id;

    public Node(int id) {
        this.id = id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    void addChild(Node child) { this.children.add(child); }
    List<Node> getChildren() { return Collections.unmodifiableList(this.children); }

    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                "children=" + children +
                '}';
    }

    public abstract List<Index> getIndexes();

    public abstract Node getNext();

    public abstract String toFlatString();
}
