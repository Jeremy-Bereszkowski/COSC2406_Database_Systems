import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

class dbload {

    private static String dateTimeStampBuilder(char[] datetime) {
        if (datetime.length < 12)
            return null;

        StringBuilder dateTimeString = new StringBuilder();

        // Append date
        // Format - yyyymmdd
        dateTimeString.append(String.format("%c%c%c%c", datetime[6], datetime[7], datetime[8], datetime[9]));
        dateTimeString.append(String.format("%c%c", datetime[0], datetime[1]));
        dateTimeString.append(String.format("%c%c", datetime[3], datetime[4]));

        // Convert time to 24Hr format
        int twentyFourHourTime = Integer.parseInt(String.format("%c%c", datetime[11], datetime[12]));
        if (datetime[20] == 'P') {
            twentyFourHourTime += 12;
        }

        // Append time
        // Format - hh
        dateTimeString.append(String.format("%02d", twentyFourHourTime));

        return dateTimeString.toString();
    }

    private static List<String> getDataFromLine(String line) {
        List<String> values = new ArrayList<String>();

        try (Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter("(,)+");

            int i = 0;
            while (rowScanner.hasNext()) {
                String next = rowScanner.next();
                switch (i) {
                case 0:
                case 7:
                case 8:
                case 9:
                    values.add(next);
                    break;
                case 1:
                    values.add(dateTimeStampBuilder(next.toCharArray()));
                    break;
                }
                i++;
            }
        }

        return values;
    }

    private static void readDataFile(String file_name, String heapFile, int pageSize) {
        int recordCount = 0;
        int pageCount = 0;
        try {
            List<List<String>> records = new ArrayList<>();
            Scanner scanner = new Scanner(new File(file_name));
            int i = 0;
            while (scanner.hasNextLine()) {
                if (i++ == 0) {
                    scanner.nextLine();
                } else {
                    records.add(getDataFromLine(scanner.nextLine()));
                    recordCount++;
                }

                if ((records.size() + 1 > pageSize / util.RECORD_LENGTH) || !scanner.hasNextLine()) {
                    writeFile(heapFile, records, pageSize, pageCount != 0);
                    pageCount++;
                }
            }

            // Write to stdout
            System.out.printf("Records read: %d%n", recordCount);
            System.out.printf("Page count: %d%n", pageCount);

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private static int writeFile(String fileName, List<List<String>> records, int pageSize, Boolean append) {
        try {
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(fileName, append));
            int pageCount = 0;

            // Loop till all records written
            while (records.size() > 0) {

                byte[] page = new byte[pageSize];
                int k = 0;
                pageCount++;

                // Write records to page until page full or until all records written
                while (k + util.RECORD_LENGTH < pageSize - 1 && records.size() > 0) {

                    List<String> record = records.remove(0);

                    for (int i = 0; i < 5; i++) {
                        String entry = record.get(i);

                        // Total length = 56 Bytes
                        switch (i) {
                        case 0:
                        case 1:
                        case 2:
                        case 4:
                            // Length = 4 Bytes * 4 fields = 16 Bytes
                            for (byte by : ByteBuffer.allocate(4).putInt(Integer.parseInt(entry)).array()) {
                                page[k++] = by;
                            }
                            break;
                        case 3:
                            // Length = 40 Bytes
                            // Pad string
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();

                            for (int j = 0; j < 40 - entry.length(); j++) {
                                baos.write(0);
                            }
                            baos.write(entry.getBytes());

                            for (byte by : baos.toByteArray()) {
                                page[k++] = by;
                            }
                            break;
                        }
                    }
                }

                // Write page to file
                for (byte by : page) {
                    outputStream.writeByte(by);
                }
            }

            outputStream.close();

            return pageCount;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static HashMap<String, String> readArgs(String[] args) {
        HashMap<String, String> optsList = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            if (args[i].charAt(0) == '-') {
                optsList.put(args[i++], args[i]);
            } else {
                optsList.put("file", args[i]);
            }
        }

        return optsList;
    }

    public static void main(String[] args) {
        // Fetch input args
        HashMap<String, String> optsList = readArgs(args);
        int pageSize = Integer.parseInt(optsList.get("-p"));
        String dataFile = optsList.get("file");
        String heapFile = String.format("heap.%d", pageSize);

        // Start timer
        Instant start = Instant.now();

        // Load data into memory
        readDataFile(dataFile, heapFile, pageSize);

        // Stop timer
        long timeElapsed = Duration.between(start, Instant.now()).toMillis();

        // Write to stdout
        System.out.printf("Time to write: %dmS%n", timeElapsed);
    }
}
