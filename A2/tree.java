import apple.laf.JRSUIUtils.Tree;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

    private static List<List<String>> readPage(String fileName, int pageOffset, int pageSize) {
        int RECORD_LENGTH = 56;
        List<List<String>> records = new ArrayList<>();

        try {
            DataInputStream inputStream = new DataInputStream(new FileInputStream(fileName));

            inputStream.skip(pageOffset * pageSize);

            int bytesRead = 0;

            // Read single page of data
            while (inputStream.available() > 0 && bytesRead + RECORD_LENGTH < pageSize) {
                List<String> record = new ArrayList<>();

                int record_id = inputStream.readInt();

                // Skip blank records
                if (record_id == 0)
                    continue;

                record.add(String.format("%d", record_id));
                record.add(dateStringBuilder(String.format("%d", inputStream.readInt())));
                record.add(String.format("%d", inputStream.readInt()));

                byte[] sensor_name = new byte[40];
                for (int i = 0; i < 40; i++) {
                    sensor_name[i] = inputStream.readByte();
                }

                record.add(new String(sensor_name, StandardCharsets.UTF_8));
                record.add(String.format("%d", inputStream.readInt()));

                records.add(record);
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

        // Read in first page
        int pageCounter = 0;
        List<List<String>> records = readPage(heapFile, pageCounter++, pageSize);

        // Continue searching and reading pages until all pages read
        while (records.size() > 0) {
            stringSearcher(searchText, records);
            records = readPage(heapFile, pageCounter++, pageSize);
        }

        // Stop timer
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();

        // Write to stdout
        System.out.println(String.format("Time to search: %dmS", timeElapsed));
    }
}
