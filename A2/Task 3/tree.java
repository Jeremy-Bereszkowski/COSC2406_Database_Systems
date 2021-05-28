import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

public class tree {
    private static final int RECORD_LENGTH = 56;

    private static String dateStringBuilder(String date) {
        String year = date.substring(0, 4);
        String month = date.substring(4, 6);
        String day = date.substring(6, 8);
        String hour = date.substring(8);

        if (hour.equals("12")) hour = "00";
        else if (hour.equals("24")) hour = "12";

        return String.format("%s/%s/%s %s:00:00", year, month, day, hour);
    }

    private static List<DataNode> readHeapFile(String fileName, int pageSize, String indexType) {
        List<DataIndex> indexes = new ArrayList<>();

        try {
            DataInputStream inputStream = new DataInputStream(new FileInputStream(fileName));

            int pageCount = 0;
            // Read whole file
            while (inputStream.available() > 0) {
                int bytesRead = 0;

                // Read single page of data - add indexes to node
                do {
                    int record_id = inputStream.readInt();

                    // If record_id is 0, rest of page is empty, read remaining empty bytes
                    if (record_id == 0) {
                        //Remaining bytes minus single int read for record_id
                        inputStream.readNBytes(pageSize - bytesRead - 4);
                        continue;
                    }

                    String recordId = String.format("%d", record_id);
                    String dateString = dateStringBuilder(String.format("%d", inputStream.readInt()));
                    String sensorId = String.format("%d", inputStream.readInt());

                    byte[] sensor_name = new byte[40];
                    for (int i = 0; i < 40; i++) {
                        sensor_name[i] = inputStream.readByte();
                    }

                    String sensorName = new String(sensor_name, StandardCharsets.UTF_8);
                    String hourlyCounts = String.format("%d", inputStream.readInt());

                    String index;
                    if ("date".equals(indexType)) index = dateString.substring(0, 10);
                    else if ("time".equals(indexType)) index = dateString.substring(11);
                    else if ("datetime".equals(indexType)) index = dateString;
                    else index = recordId.concat(dateString);

                    indexes.add(new DataIndex(index, pageCount, bytesRead/RECORD_LENGTH));

                } while ((bytesRead += RECORD_LENGTH) < pageSize && inputStream.available() > 0);

                pageCount++;
            }

            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Sort all records by index
        indexes.sort(Index.IndexComparator);

        // Compile DataIndexes into Data Nodes
        List<DataNode> dataNodes = new ArrayList<>();
        for (int i = 0; i < indexes.size(); ) {
            DataNode node = new DataNode();

            for (int j = 0; j < pageSize/RECORD_LENGTH && i < indexes.size(); i++, j++) {
                node.addIndex(indexes.get(i));
            }

            dataNodes.add(node);
        }

        return dataNodes;
    }

    private static List<TreeNode> buildLayer(List<TreeNode> nodes, int pageSize) {
        List<TreeNode> layer = new ArrayList<>();

        int maxIndexPerNode = pageSize / RECORD_LENGTH;

        for (int i = 0; i < nodes.size(); ) {
            TreeNode node = new TreeNode();

            node.addChild(nodes.get(i++));

            for (int k = 1; k < maxIndexPerNode + 1 && i < nodes.size(); i++, k++) {
                // Add child to new node, add index to new node
                node.addChild(nodes.get(i));
                node.addIndex(nodes.get(i).getIndexes().get(0));
            }

            if (node.getIndexes().size() > 0) layer.add(node);
        }

        return layer;
    }

    private static List<TreeNode> buildBase(List<DataNode> nodes, int pageSize) {
        List<TreeNode> layer = new ArrayList<>();

        int maxIndexPerNode = pageSize / RECORD_LENGTH;

        for (int i = 0; i < nodes.size(); ) {
            TreeNode node = new TreeNode();

            node.addChild(nodes.get(i++));

            for (int k = 1; k < maxIndexPerNode + 1 && i < nodes.size(); i++, k++) {
                // Add child to new node, add index to new node
                node.addChild(nodes.get(i));
                node.addIndex(new Index(nodes.get(i).getIndexes().get(0).getIndex()));
            }

            if (node.getIndexes().size() > 0) layer.add(node);
        }

        return layer;
    }

    private static void chainDataNodes(List<DataNode> nodes) {
        for (int i = 0; i < nodes.size()-1; i++) {
            nodes.get(i).setNext(nodes.get(i+1));
        }
    }

    private static TreeNode buildTree(String fileName, int pageSize, String indexType) {
        // Read in heap file to data nodes
        List<DataNode> nodes = readHeapFile(fileName, pageSize, indexType);

        // Chain data nodes
        chainDataNodes(nodes);

        // Build base layer of tree
        List<TreeNode> baseLayer = buildBase(nodes, pageSize);
        if (baseLayer.size() == 1) return baseLayer.get(0);

        // Build tree layers until root node is found
        List<TreeNode> next = buildLayer(baseLayer, pageSize);
        while (next.size() > 1) next = buildLayer(baseLayer, pageSize);

        // Return root node
        return next.get(0);
    }

    private static List<Index> searchTree(TreeNode rootNode, String searchText) {
        List<Index> matchingIndexes = new ArrayList<>();

        Node node = rootNode;

        while (node.getChildren().size() != 0) {
            boolean updated = false;
            //Search through current nodes indexes
            for (int i = 0; i < node.getIndexes().size(); i++) {
                // If less, than set node to current child node
                if (searchText.compareTo(node.getIndexes().get(i).getIndex()) < 0) {
                    node = node.getChildren().get(i);
                    updated = true;
                    break;
                } else if (searchText.compareTo(node.getIndexes().get(i).getIndex()) == 0) {
                    node = node.getChildren().get(i);
                    updated = true;
                    break;
                }
            }

            //Set node to last child
            if (!updated) node = node.getChildren().get(node.getChildren().size()-1);
        }

        int startingIndex = -1;
        //Get stating matching index
        for (Index index : node.getIndexes()) {
            if (index.getIndex().equals(searchText)) {
                startingIndex = node.getIndexes().indexOf(index);
                break;
            }
        }

        // If not matching index, return empty set
        if (startingIndex == -1) return matchingIndexes;

        //  Traverse nodes from starting index looking for matches
        boolean match = true;
        while (match && node != null) {
            for (int i = startingIndex; i < node.getIndexes().size(); i++) {
                if (node.getIndexes().get(i).getIndex().equals(searchText)) {
                    matchingIndexes.add(node.getIndexes().get(i));
                } else {
                    match = false;
                }
            }
            node = node.getNext();
            startingIndex = 0;
        }

        return matchingIndexes;
    }

    private static Record readRecordFromFile(String fileName, int pageOffset, int recordOffset, int pageSize) {
        Record record = null;

        try {
            DataInputStream inputStream = new DataInputStream(new FileInputStream(fileName));
            // Skip = PAGE_SIZE*PAGE_OFFSET + RECORD_SIZE*RECORD_OFFSET
            inputStream.skipBytes(pageOffset*pageSize+recordOffset*RECORD_LENGTH);

            int record_id = inputStream.readInt();

            String recordId = String.format("%d", record_id);
            String dateString = dateStringBuilder(String.format("%d", inputStream.readInt()));
            String sensorId = String.format("%d", inputStream.readInt());

            byte[] sensor_name = new byte[40];
            for (int i = 0; i < 40; i++) {
                sensor_name[i] = inputStream.readByte();
            }

            String sensorName = new String(sensor_name, StandardCharsets.UTF_8);
            String hourlyCounts = String.format("%d", inputStream.readInt());

            record = new Record(recordId, dateString, sensorId, sensorName, hourlyCounts);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return record;
    }

    public static void main(String[] args) {
        // Fetch input args
        String searchText = args[0];
        int pageSize = Integer.parseInt(args[1]);
        String indexType = args[2];
        String heapFile = String.format("heap.%d", pageSize);

        // Start timer
        Instant startBuild = Instant.now();

        // Build Tree
        TreeNode rootNode = buildTree(heapFile, pageSize, indexType);
        System.out.printf("Time to build: %dmS%n", Duration.between(startBuild, Instant.now()).toMillis());

        // Search Tree
        Instant startSearch = Instant.now();
        List<Index> matchingIndexes = searchTree(rootNode, searchText);

        // Read full records from file
        List<Record> matchingRecords = new ArrayList<>();
        for (Index index : matchingIndexes) matchingRecords.add(readRecordFromFile(heapFile, ((DataIndex)index).getPageOffset(), ((DataIndex)index).getRecordOffset(), pageSize));

        //Print matching records
        System.out.println(util.listToString(matchingRecords, 0));

        // Write to stdout
        System.out.printf("Time to search: %dmS%n", Duration.between(startSearch, Instant.now()).toMillis());
    }
}
