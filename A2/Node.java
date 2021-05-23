import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Node {
    private final List<TreeNode> children = new ArrayList<>();

    void addChild(TreeNode child) { this.children.add(child); }
    List<TreeNode> getChildren() { return Collections.unmodifiableList(this.children); }
    void removeChild(TreeNode child) { this.children.remove(child); }
}
