import java.util.List;

public final class util {
    public static String listToString(List<?> list) {
        StringBuilder string = new StringBuilder("[");
        for (Object o : list) {
            string.append("\n\t");
            string.append(o.toString());
        }
        string.append("\n]");
        return string.toString();
    }
}
