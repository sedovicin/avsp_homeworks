import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class NodeRank {

    private static List<List<Integer>> matrix = new ArrayList<>();
    private static List<double[]> rIterations = new ArrayList<>();

    public static void main(final String[] args) {
	try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
	    String[] line = reader.readLine().split(" ");

	    Integer nodesCount = Integer.parseInt(line[0]);
	    Double beta = Double.parseDouble(line[1]);

	    int n = nodesCount;

	    while (--n >= 0) {
		line = reader.readLine().split(" ");
		List<Integer> nodes = new ArrayList<>();
		for (String destNode : line) {
		    nodes.add(Integer.parseInt(destNode));
		}
		matrix.add(nodes);
	    }

	    String line1 = reader.readLine();
	    Integer queries = Integer.parseInt(line1);

	    int q = queries;

	    double[] r0 = new double[nodesCount];
	    for (int i = 0; i < nodesCount; ++i) {
		r0[i] = 1d / nodesCount;
	    }
	    rIterations.add(r0);
	    double[] rPrevious = r0;

	    int size = matrix.size();

	    for (int i = 1; i <= 100; ++i) {
		double[] rCurrent = new double[nodesCount];
		for (int j = 0; j < nodesCount; ++j) {
		    rCurrent[j] = (1d - beta) / ((double) nodesCount);
		}

		for (int j = 0; j < size; ++j) {
		    List<Integer> destNodes = matrix.get(j);
		    double destNodesSize = destNodes.size();
		    for (Integer destNode : destNodes) {
			rCurrent[destNode] += (beta * rPrevious[j]) / destNodesSize;
		    }
		}
		rIterations.add(rCurrent);
		rPrevious = rCurrent;
	    }
	    while (--q >= 0) {
		line = reader.readLine().split(" ");
		Integer nodeIndex = Integer.parseInt(line[0]);
		Integer iterationIndex = Integer.parseInt(line[1]);

		println(rIterations.get(iterationIndex)[nodeIndex]);

	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    private static void println(final double result) {
	DecimalFormat df = new DecimalFormat("0.0000000000");
	BigDecimal bd = new BigDecimal(result);
	BigDecimal res = bd.setScale(10, RoundingMode.HALF_UP);
	System.out.println(df.format(res));
    }

}
