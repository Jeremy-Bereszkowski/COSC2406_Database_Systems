import java.util.Collections;
import java.util.List;

public class TreeNode {
    private List<String> indexes = new List<>();;
    private List<TreeNode> children = new List<>();
    private TreeNode next = null;

    TreeNode(String ...indexes) { for (String index : indexes) this.indexes.add(index); }
    
    TreeNode getNext() { return this.next; }
    void resetNext() { this.next = null; }
    void setNext(TreeNode next) { this.next = next; }

    void addChild(TreeNode child) { this.children.add(child); }
    List<String> getChildren() { return Collections.unmodifiableList(this.children); }
    void removeChild(TreeNode child) { this.children.remove(child); }
    void removeChild(String index) {
        for (TreeNode child : children) {
            if (child.containsIndex(index)) {
                children.remove(child);
                break;
            }
        }
    }
    
    void addIndex(String index) { this.indexes.add(index); }
    Boolean containsIndex(String index) { this.indexes.contains(index); }
    List<String> getIndexes() { return Collections.unmodifiableList(this.indexes); }
    void removeIndex(String index) { this.indexes.remove(index); }
}