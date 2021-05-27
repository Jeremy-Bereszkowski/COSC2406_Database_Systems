import java.util.List;

public final class util {

    private static String tabString(int tabs) {
        StringBuilder string = new StringBuilder();
        string.append("\t".repeat(Math.max(0, tabs)));
        return string.toString();
    }
    public static String listToString(List<?> list, int tabs) {
        StringBuilder string = new StringBuilder();
        string.append(tabString(tabs));
        string.append("[");
        for (Object o : list) {
            string.append("\n\t");
            string.append(tabString(tabs));
            string.append(o.toString());
        }
        string.append("\n");
        string.append(tabString(tabs));
        string.append("]");
        return string.toString();
    }
}
