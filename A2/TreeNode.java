import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TreeNode extends Node {
    private final List<String> indexes = new ArrayList<>();

    void addIndex(String index) { this.indexes.add(index); }
    Boolean containsIndex(String index) { return this.indexes.contains(index); }
    List<String> getIndexes() { return Collections.unmodifiableList(this.indexes); }
    void removeIndex(String index) { this.indexes.remove(index); }
}
