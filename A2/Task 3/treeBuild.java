import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

public class treeBuild {
    private static int NODE_ID_COUNT = 0;
    private static byte[] page;
    private static int pageByteCount = 0;
    private static boolean append = false;

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
                    String dateString = util.dateStringBuilder(String.format("%d", inputStream.readInt()));
                    String sensorId = String.format("%d", inputStream.readInt());

                    byte[] sensor_name = new byte[40];
                    for (int i = 0; i < 40; i++) {
                        sensor_name[i] = inputStream.readByte();
                    }

                    String sensorName = new String(sensor_name, StandardCharsets.UTF_8);
                    String hourlyCounts = String.format("%d", inputStream.readInt());

                    String date = dateString.substring(0, 10);
                    String time = dateString.substring(11);

                    // Build index
                    String index;
                    if ("date".equals(indexType)) index = date;
                    else if ("time".equals(indexType)) index = time;
                    else if ("datetime".equals(indexType)) index = date.concat(util.INDEX_DELIMITER).concat(time);
                    else index = recordId.concat(util.INDEX_DELIMITER).concat(date).concat(util.INDEX_DELIMITER).concat(time);

                    indexes.add(new DataIndex(index, pageCount, bytesRead/util.RECORD_LENGTH));

                } while ((bytesRead += util.RECORD_LENGTH) < pageSize && inputStream.available() > 0);

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
            DataNode node = new DataNode(NODE_ID_COUNT++);

            for (int j = 0; j < pageSize/util.RECORD_LENGTH && i < indexes.size(); i++, j++) {
                node.addIndex(indexes.get(i));
            }

            dataNodes.add(node);
        }

        return dataNodes;
    }

    private static List<TreeNode> buildLayer(List<TreeNode> nodes, int pageSize) {
        List<TreeNode> layer = new ArrayList<>();

        int maxIndexPerNode = pageSize / util.RECORD_LENGTH;

        for (int i = 0; i < nodes.size(); ) {
            TreeNode node = new TreeNode(NODE_ID_COUNT++);

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

        int maxIndexPerNode = pageSize / util.RECORD_LENGTH;

        for (int i = 0; i < nodes.size(); ) {
            TreeNode node = new TreeNode(NODE_ID_COUNT++);

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
//        chainDataNodes(nodes);

        // Build base layer of tree
        List<TreeNode> baseLayer = buildBase(nodes, pageSize);
        if (baseLayer.size() == 1) return baseLayer.get(0);

        // Build tree layers until root node is found
        List<TreeNode> next = buildLayer(baseLayer, pageSize);
        while (next.size() > 1) next = buildLayer(baseLayer, pageSize);

        // Return root node
        return next.get(0);
    }

    public static void traverse(Node root, String fileName, int pageSize) {
        // Convert node to bytes
        byte[] nodeInBytes = util.RECORD_DELIMITER.concat(root.toFlatString()).concat(util.RECORD_DELIMITER).getBytes();

        // If page doesn't have enough remaining space, write to file
        if (nodeInBytes.length >= pageSize - pageByteCount) writePage(fileName, pageSize);

        // Write node to page buffer
        for (byte by : nodeInBytes) page[pageByteCount++] = by;
        pageByteCount += nodeInBytes.length;

        // Traverse node children
        for (Node n : root.getChildren()) traverse(n, fileName, pageSize);
    }

    public static void writePage(String fileName, int pageSize) {
        try {
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(fileName, append));

            // Write page buffer
            for (byte b : page) outputStream.writeByte(b);

            // Reset buffer variables
            page = new byte[pageSize];
            pageByteCount = 0;
            append = true;

            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Fetch input args
        int pageSize = Integer.parseInt(args[0]);
        String indexType = args[1];

        // Construct file names
        String heapFile = String.format("heap.%d", pageSize);
        String treeFile = String.format("tree.%s.%d", indexType, pageSize);

        // Start timer
        Instant startBuild = Instant.now();

        // Build Tree
        TreeNode rootNode = buildTree(heapFile, pageSize, indexType);
        System.out.printf("Time to build: %dmS%n", Duration.between(startBuild, Instant.now()).toMillis());

        // Init page buffer
        page = new byte[pageSize];

        // Write tree to file
        traverse(rootNode, treeFile, pageSize);

        // Write buffer if not empty
        if (pageByteCount > 0) writePage(treeFile, pageSize);
    }
}
