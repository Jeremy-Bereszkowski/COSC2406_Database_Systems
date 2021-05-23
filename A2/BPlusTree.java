import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class BPlusTree {
    private TreeNode root = null;

    BPlusTree(String index) { root = new TreeNode(index); }

    void addIndex(String index) {

    }

    void searchTree(String index) {

    }

    private static String dateStringBuilder(String date) {
        String year = date.substring(0, 4);
        String month = date.substring(4, 6);
        String day = date.substring(6, 8);
        String hour = date.substring(8);
        String twelveHour = "AM";

        if (Integer.parseInt(hour) / 12 >= 1) {
            hour = String.format("%02d", Integer.parseInt(hour) - 12);
            twelveHour = "PM";
        }

        return String.format("%s/%s/%s %s:00:00 %s", month, day, year, hour, twelveHour);
    }

    private static List<DataNode> readHeapFile(String fileName, int pageOffset, int pageSize) {
        int RECORD_LENGTH = 56;
        List<DataNode> records = new ArrayList<>();

        try {
            DataInputStream inputStream = new DataInputStream(new FileInputStream(fileName));
            inputStream.skip(pageOffset * pageSize);

            int bytesRead = 0;

            DataNode node = new DataNode();

            // Read single page of data
            while (inputStream.available() > 0 && bytesRead + RECORD_LENGTH < pageSize) {
                int record_id = inputStream.readInt();

                // Skip blank records
                if (record_id == 0) continue;

                String id = String.format("%d", record_id);
                String dateString = dateStringBuilder(String.format("%d", inputStream.readInt()));

                String index = id.concat(dateString);
                int recordOffset = bytesRead / RECORD_LENGTH;

                node.addIndex(new DataIndex(index, pageOffset, recordOffset));

                bytesRead += RECORD_LENGTH;
            }

            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return records;
    }

    public static void main(String[] args) {
        // Fetch input args
        String searchText = args[0];
        int pageSize = Integer.parseInt(args[1]);
        String heapFile = String.format("heap.%d", pageSize);

        // Start timer
        Instant start = Instant.now();

        // Read in first page
        int pageCounter = 0;
        List<List<String>> records = readPage(heapFile, pageCounter++, pageSize);
    }
}
