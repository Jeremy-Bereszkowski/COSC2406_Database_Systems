import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class tree {
    private static final int MAX_INDEX_PER_NODE = 18;
    private static final int RECORD_LENGTH = 56;

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

                    if (record_id == 0) {
                        bytesRead += RECORD_LENGTH;
                        break;
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

                    String index = recordId.concat(dateString);
                    int recordOffset = bytesRead / RECORD_LENGTH;

                    node.addIndex(new DataIndex(index, pageCounter/5, recordOffset));
                    bytesRead += RECORD_LENGTH;
                }

                //If page contained nodes
                if (node.getIndexes().size() > 0) {
                    node.sortIndexes();
                    dataNodes.add(node);
                }

                //Sort indexes and add node to list
                pageCounter++;
            }

            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataNodes;
    }

    private static List<TreeNode> buildLayer(List<TreeNode> nodes) {
        List<TreeNode> layer = new ArrayList<>();

        for (int i = 0; i < nodes.size(); ) {
            for (int j = 0; j < MAX_INDEX_PER_NODE + 1 && i < nodes.size(); ) {
                TreeNode node = new TreeNode();

                node.addChild(nodes.get(i++));

                for (int k = 1; k < MAX_INDEX_PER_NODE + 1 && i < nodes.size(); j++, i++, k++) {
                    // Add child to new node, add index to new node
                    node.addChild(nodes.get(i));
                    node.addIndex(nodes.get(i).getIndexes().get(0));
                }

                layer.add(node);
            }
        }

        return layer;
    }

    private static List<TreeNode> buildBase(List<DataNode> nodes) {
        List<TreeNode> layer = new ArrayList<>();

        for (int i = 0; i < nodes.size(); ) {
            for (int j = 0; j < MAX_INDEX_PER_NODE + 1; ) {
                TreeNode node = new TreeNode();

                node.addChild(nodes.get(i++));

                for (int k = 1; k < MAX_INDEX_PER_NODE + 1 && i < nodes.size(); j++, i++, k++) {
                    // Add child to new node, add index to new node
                    node.addChild(nodes.get(i));
                    node.addIndex(new Index(nodes.get(i).getIndexes().get(0).getIndex()));
                }

                layer.add(node);
            }
        }

        return layer;
    }

    private static void chainDataNodes(List<DataNode> nodes) {
        for (int i = 0; i < nodes.size()-1; i++) {
            nodes.get(i).setNext(nodes.get(i+1));
        }
    }

    private static TreeNode buildTree(String fileName, int pageSize) {
        // Read in heap file to data nodes
        List<DataNode> nodes = readHeapFile(fileName, pageSize);

        // Chain data nodes
        chainDataNodes(nodes);

        // Build base layer of tree
        List<TreeNode> baseLayer = buildBase(nodes);
        if (baseLayer.size() == 1) return baseLayer.get(0);

        // Build tree layers until root node is found
        List<TreeNode> next = buildLayer(baseLayer);
        while (next.size() > 1) next = buildLayer(baseLayer);

        // Return root node
        return next.get(0);
    }

    private static void searchTree(TreeNode rootNode, String searchText) {
        List<Index> matchingIndexes = new ArrayList<>();

        Node node = rootNode;

        while (node.getChildren().size() != 0) {
            Boolean updated = false;
            //Search through current nodes indexes
            for (int i = 0; i < node.getIndexes().size(); i++) {
                // If less, than set node to current child node
                if (searchText.compareTo(node.getIndexes().get(i).getIndex()) < 0) {
                    node = node.getChildren().get(i);
                    System.out.println("HELLLO");
                    updated = true;
                    break;
                }
            }

            //Set node to last child
            if (!updated) {
                System.out.println("HELLLO1111");
                node = node.getChildren().get(node.getChildren().size()-1);
            }
        }

        // Print all indexes in relevant leaf
        for (Index index : node.getIndexes()) {
            if (index.getIndex().equals(searchText)) {
                matchingIndexes.add(index);
            }
        }

        System.out.println(matchingIndexes);
    }

    private static void readRecordFromFile(int pageOffset, int recordOffset) {

    }

    public static void main(String[] args) {
        // Fetch input args
        String searchText = args[0];
        int pageSize = Integer.parseInt(args[1]);
        String heapFile = String.format("heap.%d", pageSize);

        // Start timer
        Instant start = Instant.now();

        // Build Tree
        TreeNode rootNode = buildTree(heapFile, pageSize);

        //Search Tree
        searchTree(rootNode, searchText);

        // Stop timer
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();

        // Write to stdout
        System.out.printf("Time to search: %dmS%n", timeElapsed);
    }
}
