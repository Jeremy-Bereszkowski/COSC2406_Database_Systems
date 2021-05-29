import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class treeSearch {

    private static List<Index> traverseTree(TreeNode rootNode, String searchText) {
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
            inputStream.skipBytes(pageOffset*pageSize+recordOffset*util.RECORD_LENGTH);

            int record_id = inputStream.readInt();

            String recordId = String.format("%d", record_id);
            String dateString = util.dateStringBuilder(String.format("%d", inputStream.readInt()));
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

    public static List<Node> readNodes(String fileName, int pageSize) {
        List<Node> nodes = new ArrayList<>();

        try (DataInputStream inputStream = new DataInputStream(new FileInputStream(fileName))) {

            while (inputStream.available() > 0) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                baos.write(inputStream.readByte());
                char lastChar = baos.toString().charAt(baos.toString().length() - 1);

                if (lastChar == '$') {
                    while (inputStream.available() > 0) {
                        baos.write(inputStream.readByte());
                        lastChar = baos.toString().charAt(baos.toString().length() - 1);

                        if (lastChar == '$') break;
                    }
                } else {
                    continue;
                }

                String nodeString = baos.toString();
                if (nodeString.length() == pageSize) break;

                String[] strings = nodeString.substring(1, nodeString.length() - 2).split("#");

                if (strings[1].equals("0")) {
                    TreeNode node = new TreeNode(Integer.parseInt(strings[0]));

                    String[] indexes = strings[2].split(" ");
                    for (String index : indexes) {
                        node.addIndex(new Index(index));
                    }

                    String[] childrenIds = strings[3].split(" ");
                    for (String cId : childrenIds) {
                        node.addChildId(Integer.parseInt(cId));
                    }

                    nodes.add(node);
                } else {
                    DataNode node = new DataNode(Integer.parseInt(strings[0]));

                    String[] indexes = strings[2].split(" ");
                    for (String index : indexes) {
                        String[] i = index.split("%");
                        node.addIndex(new DataIndex(i[0], Integer.parseInt(i[1]), Integer.parseInt(i[2])));
                    }

                    nodes.add(node);
                }
            }

            return nodes;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void chainNodes(List<Node> nodes, TreeNode root) {
        //Iterate through root nodes child ids - looking to find matching node in nodes list
        for (Integer childId : root.getChildrenIds()) {
            int nodeIndex = -1;

            // Search nodes list for matching node
            for (Node node : nodes) {
                //If node matches
                if (node.getId() == childId) {
                    nodeIndex = nodes.indexOf(node);
                    break;
                }
            }

            // Add matching node as child of root node
            if (nodeIndex!= -1) root.addChild(nodes.remove(nodeIndex));
        }

        // If there are children nodes remaining - recursively chain nodes
        if (nodes.size() > 0) {
            for (Node child : root.getChildren()) {
                // Early terminate if all nodes chained
                if (nodes.size() == 0) break;

                // Only TreeNodes have children
                if (child instanceof TreeNode) chainNodes(nodes, (TreeNode)child);
            }
        }
    }

    private static void traverseForDataNodes(Node root, List<DataNode> nodes) {
        // Traverse all child nodes
        for (Node child : root.getChildren()) traverseForDataNodes(child, nodes);

        // If node is a Data node => add to list
        if (root instanceof DataNode) nodes.add((DataNode)root);
    }

    private static void chainLeafNodes(TreeNode root) {
        List<DataNode> nodes = new ArrayList<>();

        // Collect list of all Data Nodes
        // Due to nature of search, nodes will be ordered
        traverseForDataNodes(root, nodes);

        // Chain ordered list of data nodes
        chainDataNodes(nodes);
    }

    private static void chainDataNodes(List<DataNode> nodes) {
        for (int i = 0; i < nodes.size()-1; i++) {
            nodes.get(i).setNext(nodes.get(i+1));
        }
    }

    public static void main(String[] args) {
        // Fetch input args
        int pageSize = Integer.parseInt(args[0]);
        String indexType = args[1];
        String searchText = args[2];

        // Construct file names
        String heapFile = String.format("heap.%d", pageSize);
        String treeFile = String.format("tree.%s.%d", indexType, pageSize);

        // Start build timer
        Instant startBuild = Instant.now();

        // Read tree from file
        List<Node> nodes = readNodes(treeFile, pageSize);
        assert nodes != null;
        TreeNode root = (TreeNode)nodes.remove(0);

        // Rebuild tree
        chainNodes(nodes, root);
        chainLeafNodes(root);

        // End build/Start search timer
        Instant endBuildStartSearch = Instant.now();

        // Search Tree
        List<Index> matchingIndexes = traverseTree(root, searchText);

        // Read full records from file
        List<Record> matchingRecords = new ArrayList<>();
        for (Index index : matchingIndexes) matchingRecords.add(readRecordFromFile(heapFile, ((DataIndex)index).getPageOffset(), ((DataIndex)index).getRecordOffset(), pageSize));

        // End search timer
        Instant endSearch = Instant.now();

        // Write to stdout
        System.out.println(util.listToString(matchingRecords));
        System.out.printf("Time to build: %dmS%n", Duration.between(startBuild, endBuildStartSearch).toMillis());
        System.out.printf("Time to search: %dmS%n", Duration.between(endBuildStartSearch, endSearch).toMillis());
    }
}
