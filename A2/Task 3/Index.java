public class Index {
    private String index;

    Index(String index) {
        this.index = index;
    }

    public String getIndex() { return index; }
    public void setIndex(String index) { this.index = index; }

    @Override
    public String toString() {
        return "\n\t\t\t" + index;
    }
}
