import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class GNAlgorithm {

    private static Map<Integer, Node> nodes = new HashMap<>();
    private static List<Edge> edges = new ArrayList<>();
    private static Map<Integer, boolean[]> userProperties = new HashMap<>();
    private static List<List<Node>> shortestPathsNodes;
    private static List<List<Edge>> shortestPathsEdges;
    private static int shortestPathLength;

    public static void main(final String[] args) {
	// try (BufferedReader reader = new BufferedReader(new
	// InputStreamReader(System.in))) {
	try (BufferedReader reader = new BufferedReader(new FileReader(new File(args[0])))) {
	    String line = reader.readLine();
	    String[] lineSplitted;

	    while (!line.equals("")) {
		lineSplitted = line.split(" ");
		Integer node1Index = Integer.parseInt(lineSplitted[0]);
		Integer node2Index = Integer.parseInt(lineSplitted[1]);

		Node node1 = nodes.get(node1Index);
		if (node1 == null) {
		    node1 = new Node(node1Index);
		    nodes.put(node1Index, node1);
		}
		Node node2 = nodes.get(node2Index);
		if (node2 == null) {
		    node2 = new Node(node2Index);
		    nodes.put(node2Index, node2);
		}

		Edge edge = new Edge(node1Index, node2Index);
		node1.edges.add(edge);
		node2.edges.add(edge);
		edges.add(edge);

		line = reader.readLine();
	    }

	    line = reader.readLine();

	    while (!((line == null) || (line.equals("")))) {
		lineSplitted = line.split(" ");

		boolean[] properties = new boolean[lineSplitted.length - 1];

		for (int i = 1; i < lineSplitted.length; ++i) {
		    if (lineSplitted[i].equals("1")) {
			properties[i - 1] = true;
		    } else if (lineSplitted[i].equals("0")) {
			properties[i - 1] = false;
		    } else {
			throw new Exception("Illegal properties value at current line: " + line);
		    }
		}

		userProperties.put(Integer.parseInt(lineSplitted[0]), properties);

		line = reader.readLine();
	    }

	    for (Integer userPropertyKey : userProperties.keySet()) {
		if (!nodes.containsKey(userPropertyKey)) {
		    nodes.put(userPropertyKey, new Node(userPropertyKey.intValue()));
		}
	    }

	    calculateAndSetWeights();

	    double currModularity = 0d, bestModularity = -1d;

	    Set<TreeSetComparable<Node>> bestCommunities = new TreeSetComparable<>(),
		    currCommunities = new TreeSetComparable<>();
	    do {
		calculateEdgeCentralities();

		currModularity = calculateModularity();

		currCommunities = new TreeSetComparable<>();
		for (Node node : nodes.values()) {
		    currCommunities.add(node.nodesInCommunity);
		}

		if (currModularity > bestModularity) {
		    bestModularity = currModularity;
		    bestCommunities = currCommunities;
		}

		List<Edge> edgesToRemove = getHighestCentralityEdges();

		System.out.println(printEdges(edgesToRemove));

		edges.removeAll(edgesToRemove);

		for (Node node : nodes.values()) {
		    node.edges.removeAll(edgesToRemove);
		}
	    } while (edges.size() > 0);

	    StringBuilder sb = new StringBuilder();
	    for (Set<Node> community : bestCommunities) {
		for (Node node : community) {
		    sb.append(node.index);
		    sb.append("-");
		}
		sb.delete(sb.lastIndexOf("-"), sb.length());
		sb.append(" ");
	    }
	    sb.delete(sb.lastIndexOf(" "), sb.length());

	    System.out.println(sb.toString());

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private static void calculateAndSetWeights() {
	for (Edge edge : edges) {
	    boolean[] propertiesNode1 = userProperties.get(edge.node1);
	    boolean[] propertiesNode2 = userProperties.get(edge.node2);

	    int counter = 0;

	    for (int i = 0; i < propertiesNode1.length; ++i) {
		if (propertiesNode1[i] == propertiesNode2[i]) {
		    ++counter;
		}
	    }
	    edge.weight = propertiesNode1.length - (counter - 1);
	}
    }

    private static void calculateEdgeCentralities() throws Exception {
	resetCentralities();
	resetNodeCommunities();

	for (Node node1 : nodes.values()) {
	    for (Node node2 : nodes.values()) {
		if (node1 != node2) {
		    shortestPathsNodes = new ArrayList<>();
		    shortestPathsEdges = new ArrayList<>();
		    shortestPathLength = 0;
		    List<Node> visitedNodes = new ArrayList<>();
		    List<Edge> visitedEdges = new ArrayList<>();
		    visitNextNode(node1, visitedNodes, visitedEdges, shortestPathLength, node2);

		    updateCentralities();
		}
	    }
	}
	for (Edge edge : edges) {
	    edge.edgeCentrality /= 2;
	}
    }

    private static double calculateModularity() {
	double sum = 0d;
	int edgeWeightSum = 0;
	for (Edge edge : edges) {
	    edgeWeightSum += edge.weight;
	}
	if (edgeWeightSum == 0) {
	    return 0.0;
	}
	for (Node node1 : nodes.values()) {
	    for (Node node2 : nodes.values()) {
		if (node1.nodesInCommunity.contains(node2)) {
		    int edgeWeight = 0;
		    int node1EdgesSum = 0, node2EdgesSum = 0;
		    for (Edge edge : node1.edges) {
			if (((edge.node1 == node1.index) && (edge.node2 == node2.index))
				|| ((edge.node1 == node2.index) && (edge.node2 == node1.index))) {
			    edgeWeight = edge.weight;

			}
			node1EdgesSum += edge.weight;
		    }
		    for (Edge edge : node2.edges) {
			node2EdgesSum += edge.weight;
		    }
		    sum += (edgeWeight - ((double) (node1EdgesSum * node2EdgesSum) / (2 * edgeWeightSum)));
		}
	    }
	}
	sum /= (2 * edgeWeightSum);
	if (Math.abs(sum) < 10e-5) {
	    sum = 0d;
	}
	DecimalFormat df = new DecimalFormat("0.0000");
	BigDecimal bd = new BigDecimal(sum);
	BigDecimal res = bd.setScale(4, RoundingMode.HALF_UP);
	return Double.parseDouble(df.format(res.doubleValue()).replaceAll(",", "."));
    }

    private static void visitNextNode(final Node node, final List<Node> visitedNodes, final List<Edge> visitedEdges,
	    final int pathLength, final Node endingNode) throws Exception {
	if (visitedNodes.contains(node)) {
	    return;
	}

	visitedNodes.add(node);

	visitedNodes.get(0).nodesInCommunity.add(node);

	if (node == endingNode) {
	    if (pathLength < shortestPathLength) {
		shortestPathsNodes = new ArrayList<>();
		shortestPathsEdges = new ArrayList<>();
	    }
	    shortestPathsNodes.add(new ArrayList<>(visitedNodes));
	    shortestPathsEdges.add(new ArrayList<>(visitedEdges));
	    shortestPathLength = pathLength;
	} else {
	    for (Edge edge : node.edges) {
		if (visitedEdges.contains(edge)) {
		    continue;
		}
		visitedEdges.add(edge);

		int nextPathLength = pathLength + edge.weight;
		if ((shortestPathLength == 0) || (nextPathLength <= shortestPathLength)) {
		    Node nextNode = getNextNode(node, edge);

		    visitNextNode(nextNode, visitedNodes, visitedEdges, nextPathLength, endingNode);
		}
		visitedEdges.remove(visitedEdges.size() - 1);
	    }
	}
	visitedNodes.remove(visitedNodes.size() - 1);
	return;
    }

    private static Node getNextNode(final Node node, final Edge edge) throws Exception {
	if (edge.node1 == node.index) {
	    return nodes.get(edge.node2);
	} else if (edge.node2 == node.index) {
	    return nodes.get(edge.node1);
	} else {
	    throw new Exception("This edge (" + edge.node1 + " " + edge.node2 + ") does not belong to this node ("
		    + node.index + ")!");
	}
    }

    private static void updateCentralities() {
	for (List<Edge> shortestPathEdges : shortestPathsEdges) {
	    for (Edge edge : shortestPathEdges) {
		double sum = edge.edgeCentrality + (1d / (shortestPathsEdges.size()));
		DecimalFormat df = new DecimalFormat("0.0000");
		BigDecimal bd = new BigDecimal(sum);
		BigDecimal res = bd.setScale(4, RoundingMode.HALF_UP);
		edge.edgeCentrality = Double.parseDouble(df.format(res.doubleValue()).replaceAll(",", "."));
	    }
	}
    }

    private static void resetCentralities() {
	for (Edge edge : edges) {
	    edge.edgeCentrality = 0d;
	}
    }

    private static void resetNodeCommunities() {
	for (Node node : nodes.values()) {
	    node.nodesInCommunity = new TreeSetComparable<>();
	}
    }

    private static List<Edge> getHighestCentralityEdges() {
	List<Edge> edgesWithMaxCentrality = new ArrayList<>();
	double maxCentralityValue = 0d;
	for (Edge edge : edges) {
	    if (edge.edgeCentrality < maxCentralityValue) {
		continue;
	    }
	    if (edge.edgeCentrality > maxCentralityValue) {
		maxCentralityValue = edge.edgeCentrality;
		edgesWithMaxCentrality = new ArrayList<>();
	    }
	    edgesWithMaxCentrality.add(edge);
	}
	return edgesWithMaxCentrality;
    }

    private static String printEdges(final List<Edge> edges) {
	Collections.sort(edges);
	StringBuilder sb = new StringBuilder();
	for (Edge edge : edges) {
	    sb.append(edge);
	    sb.append(System.lineSeparator());
	}
	sb.delete(sb.lastIndexOf(System.lineSeparator()), sb.length());
	return sb.toString();
    }

    private static class Node implements Comparable<Node> {
	private final int index;
	private final List<Edge> edges;
	private TreeSetComparable<Node> nodesInCommunity;

	public Node(final int index) {
	    this.index = index;
	    edges = new ArrayList<>();
	    nodesInCommunity = new TreeSetComparable<>();
	}

	@Override
	public String toString() {
	    return String.valueOf(index);
	}

	@Override
	public int compareTo(final Node otherNode) {
	    return Integer.compare(this.index, otherNode.index);
	}
    }

    private static class Edge implements Comparable<Edge> {
	private final int node1, node2;
	private int weight;
	private double edgeCentrality;

	public Edge(final int node1, final int node2) {
	    if (node1 < node2) {
		this.node1 = node1;
		this.node2 = node2;
	    } else {
		this.node1 = node2;
		this.node2 = node1;
	    }
	    weight = 1;
	    edgeCentrality = 0d;
	}

	@Override
	public String toString() {
	    return node1 + " " + node2;
	}

	@Override
	public int compareTo(final Edge otherEdge) {
	    if (this.node1 == otherEdge.node1) {
		return Integer.compare(this.node2, otherEdge.node2);
	    }
	    return Integer.compare(this.node1, otherEdge.node1);
	}
    }

    private static class TreeSetComparable<E extends Comparable<E>> extends TreeSet<E>
	    implements Comparable<TreeSetComparable<E>> {
	private static final long serialVersionUID = 1L;

	@Override
	public int compareTo(final TreeSetComparable<E> other) {
	    if (this.size() == other.size()) {
		List<E> thisList = new ArrayList<>(this);
		List<E> otherList = new ArrayList<>(other);

		for (int i = 0; i < thisList.size(); ++i) {
		    int res = thisList.get(i).compareTo(otherList.get(i));
		    if (res == 0) {
			continue;
		    } else {
			return res;
		    }
		}
	    }
	    return Integer.compare(this.size(), other.size());
	}
    }
}