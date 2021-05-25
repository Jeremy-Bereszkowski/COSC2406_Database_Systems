import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Node {
    private final List<Node> children = new ArrayList<>();

    void addChild(Node child) { this.children.add(child); }
    List<Node> getChildren() { return Collections.unmodifiableList(this.children); }
    void removeChild(Node child) { this.children.remove(child); }

    @Override
    public String toString() {
        return "Node{" +
                "children=" + children +
                '}';
    }

    public abstract List<Index> getIndexes();
}
