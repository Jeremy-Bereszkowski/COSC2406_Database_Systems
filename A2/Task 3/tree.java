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

        return String.format("%s/%s/%s %s:00:00 %s", year, month, day, hour, twelveHour);
    }

    private static List<DataNode> readHeapFile(String fileName, int pageSize, String indexType) {
        List<DataNode> dataNodes = new ArrayList<>();
        int records = 0;

        try {
            DataInputStream inputStream = new DataInputStream(new FileInputStream(fileName));
            int pageCounter = 0;

            // Read whole file
            while (inputStream.available() > 0) {
                int bytesRead = 0;
                int recordOffset = 0;
                DataNode node = new DataNode();

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
                    if ("date".equals(indexType)) index = dateString;
                    else index = recordId.concat(dateString);

                    node.addIndex(new DataIndex(index, pageCounter, recordOffset++));
                    records++;

                } while ((bytesRead += RECORD_LENGTH) < pageSize);

                //If page contained records
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

        int records2 = 0;
        for (DataNode node : dataNodes) {
            records2 += node.getIndexes().size();
        }

        System.out.println(records);
        System.out.println(records2);
        System.out.println(dataNodes.size());

        return dataNodes;
    }

    private static List<TreeNode> buildLayer(List<TreeNode> nodes) {
        List<TreeNode> layer = new ArrayList<>();

        for (int i = 0; i < nodes.size(); ) {
            TreeNode node = new TreeNode();

            node.addChild(nodes.get(i++));

            for (int k = 1; k < MAX_INDEX_PER_NODE + 1 && i < nodes.size(); i++, k++) {
                // Add child to new node, add index to new node
                node.addChild(nodes.get(i));
                node.addIndex(nodes.get(i).getIndexes().get(0));
            }

            layer.add(node);
        }

        return layer;
    }

    private static List<TreeNode> buildBase(List<DataNode> nodes) {
        List<TreeNode> layer = new ArrayList<>();

        for (int i = 0; i < nodes.size(); ) {
            TreeNode node = new TreeNode();

            node.addChild(nodes.get(i++));

            for (int k = 1; k < MAX_INDEX_PER_NODE + 1 && i < nodes.size(); i++, k++) {
                // Add child to new node, add index to new node
                node.addChild(nodes.get(i));
                node.addIndex(new Index(nodes.get(i).getIndexes().get(0).getIndex()));
            }

            layer.add(node);
        }

        return layer;
    }

    private static List<DataNode> chainDataNodes(List<DataNode> nodes) {
        for (int i = 0; i < nodes.size()-1; i++) {
            nodes.get(i).setNext(nodes.get(i+1));
        }

        return nodes;
    }

    private static TreeNode buildTree(String fileName, int pageSize, String indexType) {
        // Read in heap file to data nodes
        List<DataNode> nodes = readHeapFile(fileName, pageSize, indexType);

        // Chain data nodes
        nodes = chainDataNodes(nodes);

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
                    updated = true;
                    break;
                }
            }

            //Set node to last child
            if (!updated) node = node.getChildren().get(node.getChildren().size()-1);
        }

//        Node node2 = node;
//        while (node2 != null) {
//            System.out.println(util.listToString(node2.getIndexes()));
//            node2 = node2.getNext();
//        }

        // Get index of first matching record
        int recordIndex = 0;
        for (Index index : node.getIndexes()) {
            String [] searchTerms = searchText.split(" ");
            boolean match = true;

            // Search for each spaced separated string in the search string
            for (String term : searchTerms) {
                if (!index.getIndex().contains(term)) {
                    match = false;
                    break;
                }
            }

            // If a match => set recordIndex
            if (match) {
                recordIndex = node.getIndexes().indexOf(index);
                break;
            }
        }

        boolean match = true;
        // Iterate over records beginning from the first matching index looking for more matches
        while (match) {
            for (int i = recordIndex; i < node.getIndexes().size(); i++) {
                String [] searchTerms = searchText.split(" ");

                // Search for each spaced separated string in the search string
                for (String term : searchTerms) {
                    if (!node.getIndexes().get(i).getIndex().contains(term)) {
                        match = false;
                        break;
                    }
                }

                // If a match => add to matching index list
                if (match) {
                    matchingIndexes.add(node.getIndexes().get(i));
                }
            }

            node = node.getNext();
            recordIndex = 0;
        }

//        System.out.println(util.listToString(matchingIndexes));
//        System.out.println(matchingIndexes.size());
    }

    private static void readRecordFromFile(int pageOffset, int recordOffset) {

    }

    public static void main(String[] args) {
        // Fetch input args
        String searchText = args[0];
        int pageSize = Integer.parseInt(args[1]);
        String indexType = args[2];
        String heapFile = String.format("heap.%d", pageSize);

        // Start timer
        Instant start = Instant.now();

        // Build Tree
        TreeNode rootNode = buildTree(heapFile, pageSize, indexType);

        //Search Tree
        searchTree(rootNode, searchText);

        // Stop timer
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();

        // Write to stdout
        System.out.printf("Time to search: %dmS%n", timeElapsed);
    }
}
