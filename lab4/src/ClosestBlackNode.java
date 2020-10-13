import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ClosestBlackNode {

    private enum NodeType {
	BLACK, WHITE
    }

    private static List<Node> nodes = new ArrayList<>();

    public static void main(final String[] args) {
	try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
	    String[] line = reader.readLine().split(" ");
	    Integer nodesCount = Integer.parseInt(line[0]);
	    Integer edgesCount = Integer.parseInt(line[1]);

	    int n = nodesCount;

	    String line1 = null;

	    while (--n >= 0) {
		line1 = reader.readLine();
		Integer nodeType = Integer.parseInt(line1);
		nodes.add(new Node(nodeType == 0 ? NodeType.WHITE : NodeType.BLACK, nodes.size()));
	    }

	    int e = edgesCount;

	    while (--e >= 0) {
		line = reader.readLine().split(" ");
		Integer firstNode = Integer.parseInt(line[0]);
		Integer secondNode = Integer.parseInt(line[1]);

		nodes.get(firstNode).neighbourNodesIndexes.add(secondNode);
		nodes.get(secondNode).neighbourNodesIndexes.add(firstNode);
	    }
	    for (Node node : nodes) {
		Collections.sort(node.neighbourNodesIndexes);
	    }

	    for (Node node : nodes) {
		NodeWrapper nearestBlackNode = getNearestBlackNode(node);
		if (nearestBlackNode == null) {
		    System.out.println(-1 + " " + -1);
		} else {
		    System.out.println(nearestBlackNode.node.index + " " + nearestBlackNode.distance);
		}

	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    private static NodeWrapper getNearestBlackNode(final Node node) {
	if (node.type.equals(NodeType.BLACK)) {
	    return new NodeWrapper(node, 0);
	}
	boolean[] visitedNode = new boolean[nodes.size()];
	Queue<NodeWrapper> queue = new LinkedList<>();
	queue.add(new NodeWrapper(node, 0));

	List<NodeWrapper> nearestBlackNodes = new ArrayList<>();
	int nearestBlackNodesDistance = -1;

	while (!queue.isEmpty()) {
	    NodeWrapper currentNodeW = queue.poll();
	    if (currentNodeW.distance > 10) {
		return null;
	    }
	    if ((nearestBlackNodesDistance != -1) && (nearestBlackNodesDistance < currentNodeW.distance)) {
		break;
	    }
	    if (!visitedNode[currentNodeW.node.index]) {
		if (currentNodeW.node.type.equals(NodeType.BLACK)) {
		    nearestBlackNodes.add(currentNodeW);
		    nearestBlackNodesDistance = currentNodeW.distance;

		} else {
		    for (Integer neighbour : currentNodeW.node.neighbourNodesIndexes) {
			queue.add(new NodeWrapper(nodes.get(neighbour), currentNodeW.distance + 1));
		    }
		    visitedNode[currentNodeW.node.index] = true;
		}

	    }
	}

	if (nearestBlackNodes.isEmpty()) {
	    return null;
	}
	Collections.sort(nearestBlackNodes);
	return nearestBlackNodes.get(0);
    }

    private static class Node {
	private final NodeType type;
	private final int index;
	private final List<Integer> neighbourNodesIndexes;

	public Node(final NodeType type, final int index) {
	    this.type = type;
	    this.index = index;
	    neighbourNodesIndexes = new ArrayList<>();
	}
    }

    private static class NodeWrapper implements Comparable<NodeWrapper> {
	private final Node node;
	private final int distance;

	public NodeWrapper(final Node node, final int distance) {
	    this.node = node;
	    this.distance = distance;
	}

	@Override
	public int compareTo(final NodeWrapper otherNW) {
	    return Integer.compare(node.index, otherNW.node.index);
	}

    }
}
