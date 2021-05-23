import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class tree {
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

    private static List<DataNode> readHeapFile(String fileName, int pageSize) {
        int RECORD_LENGTH = 56;
        List<DataNode> dataNodes = new ArrayList<>();

        try {
            DataInputStream inputStream = new DataInputStream(new FileInputStream(fileName));
            int pageCounter = 0;

            // Read whole file
            while (inputStream.available() > 0) {

                int bytesRead = 0;
                DataNode node = new DataNode();

                // Read single page of data - add indexes to node
                while (bytesRead + RECORD_LENGTH < pageSize) {
                    int record_id = inputStream.readInt();

                    // Skip blank records
                    if (record_id == 0) continue;

                    String id = String.format("%d", recordb_id);
                    String dateString = dateStringBuilder(String.format("%d", inputStream.readInt()));

                    String index = id.concat(dateString);
                    int recordOffset = bytesRead / RECORD_LENGTH;

                    node.addIndex(new DataIndex(index, pageCounter, recordOffset));
                    bytesRead += RECORD_LENGTH;
                }

                //Sort indexes and add node to list
                pageCounter++;
                node.sortIndexes();
                dataNodes.add(node);
            }

            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return records;
    }

    private static void stringSearcher(String searchString, List<List<String>> records) {
        // for (List<String> record : records) {
        //     String sdt_name = record.get(2) + record.get(1);

        //     if (sdt_name.contains(searchString)) {
        //         System.out.println(record.toString());
        //     }
        // }
    }
    public static void main(String[] args) {
        // Fetch input args
        String searchText = args[0];
        int pageSize = Integer.parseInt(args[1]);
        String heapFile = String.format("heap.%d", pageSize);

        // Start timer
        Instant start = Instant.now();

        // Read in heap file
        List<DataNode> records = readHeapFile(heapFile, pageCounter++, pageSize);

        // Stop timer
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();

        // Write to stdout
        System.out.println(String.format("Time to search: %dmS", timeElapsed));
    }
}
