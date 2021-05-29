import java.util.List;

public final class util {
    public static final int RECORD_LENGTH = 56;

    // Tree file delimiters
    public static final String RECORD_DELIMITER = "$";
    public static final String NODE_FIELD_DELIMITER = "#";
    public static final String INDEX_DELIMITER = "_";
    public static final String DATA_INDEX_FIELD_DELIMITER = "%";

    private static String tabString(int tabs) {
        StringBuilder string = new StringBuilder();
        for (int i=0; i < tabs; i++) string.append("\t");
        return string.toString();
    }

    public static String listToString(List<?> list) {
        return listToString(list, 0);
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

    public static String dateStringBuilder(String date) {
        String year = date.substring(0, 4);
        String month = date.substring(4, 6);
        String day = date.substring(6, 8);
        String hour = date.substring(8);

        if (hour.equals("12")) hour = "00";
        else if (hour.equals("24")) hour = "12";

        return String.format("%s/%s/%s %s:00:00", year, month, day, hour);
    }
}
