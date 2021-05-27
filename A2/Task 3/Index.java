public class Index {
    private String index;

    Index(String index) {
        this.index = index;
    }

    public String getIndex() { return index; }
    public void setIndex(String index) { this.index = index; }

    public String toString(Boolean newLine) {
        StringBuilder string = new StringBuilder();
        if (newLine) string.append('\n');
        string.append("\t\t\t");
        string.append(index);
        return string.toString();
    }

    @Override
    public String toString() {
        return index;
    }
}
