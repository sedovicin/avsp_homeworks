import java.io.BufferedReader;
import java.io.InputStreamReader;
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

public class CF {

    private static List<List<Integer>> matrix = new ArrayList<>();

    private static Map<Integer, Double> userAverage = new HashMap<>();
    private static List<Map<Integer, Double>> userSimilarities;

    private static Map<Integer, Double> itemAverage = new HashMap<>();
    private static List<Map<Integer, Double>> itemSimilarities;

    public static void main(final String[] args) {
	try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {

	    String[] line = reader.readLine().split(" ");
	    Integer itemsCount = Integer.parseInt(line[0]);
	    Integer usersCount = Integer.parseInt(line[1]);

	    int n = itemsCount;

	    while (--n >= 0) {
		line = reader.readLine().split(" ");
		List<Integer> users = new ArrayList<>();
		Integer num;
		for (String user : line) {
		    if (user.equals("X")) {
			num = -1;
		    } else {
			num = Integer.parseInt(user);
		    }
		    users.add(num);
		}
		matrix.add(users);
	    }

	    userSimilarities = calculateUserSimilarities(usersCount);

	    itemSimilarities = calculateItemSimilarities(itemsCount);

	    String line1 = reader.readLine();

	    Integer queryAmount = Integer.parseInt(line1);

	    int q = queryAmount;

	    while (--q >= 0) {
		line = reader.readLine().split(" ");
		Integer i = Integer.parseInt(line[0]) - 1;
		Integer j = Integer.parseInt(line[1]) - 1;
		Integer type = Integer.parseInt(line[2]);
		Integer maxCardinal = Integer.parseInt(line[3]);

		if (type == 0) {
		    Map<Integer, Double> similarities = itemSimilarities.get(i);

		    List<Map.Entry<Integer, Double>> entries = new ArrayList<>(similarities.entrySet());
		    entries.sort(Map.Entry.comparingByValue(Collections.reverseOrder()));

		    int kCount = 0;
		    double numerator = 0d, denominator = 0d;
		    for (Map.Entry<Integer, Double> entry : entries) {
			if (entry.getValue().compareTo(0d) > 0) {
			    Integer value = matrix.get(entry.getKey()).get(j);
			    if (value != -1) {
				numerator += (value.doubleValue() * entry.getValue());
				denominator += entry.getValue();
			    } else {
				continue;
			    }
			} else {
			    break;
			}

			if (++kCount >= maxCardinal) {
			    break;
			}
		    }

		    double prediction = numerator / denominator;
		    println(prediction);

		} else if (type == 1) {
		    Map<Integer, Double> similarities = userSimilarities.get(j);

		    Set<Map.Entry<Integer, Double>> entries = new TreeSet<>(
			    Map.Entry.comparingByValue(Collections.reverseOrder()));
		    entries.addAll(similarities.entrySet());

		    int kCount = 0;
		    double numerator = 0d, denominator = 0d;
		    for (Map.Entry<Integer, Double> entry : entries) {
			if (entry.getValue().compareTo(0d) > 0) {
			    Integer value = matrix.get(i).get(entry.getKey());
			    if (value != -1) {
				numerator += value.doubleValue() * entry.getValue();
				denominator += entry.getValue();
			    } else {
				continue;
			    }
			} else {
			    break;
			}

			if (++kCount >= maxCardinal) {
			    break;
			}
		    }

		    double prediction = numerator / denominator;
		    println(prediction);
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    private static List<Map<Integer, Double>> calculateUserSimilarities(final Integer usersCount) {
	double[][] similarities = new double[usersCount][usersCount];
	for (int i = 0; i < (usersCount - 1); ++i) {
	    for (int j = i; j < usersCount; ++j) {
		if (i != j) {
		    Double similarity = calculateUserSimilarity(i, j);
		    similarities[i][j] = similarity;
		    similarities[j][i] = similarity;
		} else {
		    similarities[i][j] = Double.NEGATIVE_INFINITY;
		}
	    }
	}

	List<Map<Integer, Double>> similaritiesList = new ArrayList<>(usersCount);
	for (int i = 0; i < usersCount; ++i) {
	    Map<Integer, Double> map = new HashMap<>();
	    for (int j = 0; j < usersCount; ++j) {
		map.put(j, similarities[i][j]);
	    }
	    similaritiesList.add(map);
	}

	return similaritiesList;
    }

    private static Double calculateUserSimilarity(final Integer person1, final Integer person2) {
	return userPearsonSimilarity(person1, person2);
    }

    private static Double userPearsonSimilarity(final Integer user1, final Integer user2) {
	Double user1Avg = userAverage.get(user1);
	if (user1Avg == null) {
	    user1Avg = calculateUserAverage(user1);
	}
	Double user2Avg = userAverage.get(user2);
	if (user2Avg == null) {
	    user2Avg = calculateUserAverage(user2);
	}

	double numerator = 0d, denominator1 = 0d, denominator2 = 0d;

	Integer user1Value = 0, user2Value = 0;

	for (List<Integer> item : matrix) {
	    user1Value = item.get(user1);
	    user2Value = item.get(user2);
	    if ((!user1Value.equals(-1)) && (!user2Value.equals(-1))) {
		numerator += ((user1Value.doubleValue() - user1Avg) * (user2Value.doubleValue() - user2Avg));

	    }
	    if (!user1Value.equals(-1)) {
		denominator1 += Math.pow((user1Value.doubleValue() - user1Avg), 2d);

	    }
	    if (!user2Value.equals(-1)) {
		denominator2 += Math.pow((user2Value.doubleValue() - user2Avg), 2d);
	    }
	}

	return numerator / (Math.sqrt(denominator1 * denominator2));
    }

    private static Double calculateUserAverage(final Integer user) {
	Integer sum = 0;
	int count = 0;
	int value = 0;
	for (List<Integer> item : matrix) {
	    if ((value = item.get(user)) > 0) {
		sum += value;
		++count;
	    }
	}

	double avg = (double) sum / (double) count;
	userAverage.put(user, Double.valueOf(avg));
	return avg;
    }

    private static void println(final double result) {
	DecimalFormat df = new DecimalFormat("#.000");
	BigDecimal bd = new BigDecimal(result);
	BigDecimal res = bd.setScale(3, RoundingMode.HALF_UP);
	System.out.println(df.format(res));
    }

    private static List<Map<Integer, Double>> calculateItemSimilarities(final Integer itemsCount) {
	double[][] similarities = new double[itemsCount][itemsCount];
	for (int i = 0; i < (itemsCount); ++i) {
	    for (int j = i; j < itemsCount; ++j) {
		if (i != j) {
		    Double similarity = calculateItemSimilarity(i, j);
		    similarities[i][j] = similarity;
		    similarities[j][i] = similarity;
		} else {
		    similarities[i][j] = Double.NEGATIVE_INFINITY;
		}
	    }
	}

	List<Map<Integer, Double>> similaritiesList = new ArrayList<>(itemsCount);
	for (int i = 0; i < itemsCount; ++i) {
	    Map<Integer, Double> map = new HashMap<>();
	    for (int j = 0; j < itemsCount; ++j) {
		map.put(j, similarities[i][j]);
	    }
	    similaritiesList.add(map);
	}

	return similaritiesList;
    }

    private static Double calculateItemSimilarity(final Integer item1, final Integer item2) {
	return itemPearsonSimilarity(item1, item2);
    }

    private static Double itemPearsonSimilarity(final Integer item1, final Integer item2) {
	Double item1Avg = itemAverage.get(item1);
	if (item1Avg == null) {
	    item1Avg = calculateItemAverage(item1);
	}
	Double item2Avg = itemAverage.get(item2);
	if (item2Avg == null) {
	    item2Avg = calculateItemAverage(item2);
	}

	double numerator = 0d, denominator1 = 0d, denominator2 = 0d;

	Integer item1Value = 0, item2Value = 0;

	Integer userCount = matrix.get(0).size();
	for (int j = 0; j < userCount; ++j) {
	    item1Value = matrix.get(item1).get(j);
	    item2Value = matrix.get(item2).get(j);
	    if ((!item1Value.equals(-1)) && (!item2Value.equals(-1))) {
		numerator += ((item1Value.doubleValue() - item1Avg) * (item2Value.doubleValue() - item2Avg));

	    }
	    if (!item1Value.equals(-1)) {
		denominator1 += Math.pow((item1Value.doubleValue() - item1Avg), 2);

	    }
	    if (!item2Value.equals(-1)) {
		denominator2 += Math.pow((item2Value.doubleValue() - item2Avg), 2);
	    }
	}

	return numerator / (Math.sqrt(denominator1 * denominator2));
    }

    private static Double calculateItemAverage(final Integer item) {
	Integer sum = 0;
	int count = 0;
	for (Integer value : matrix.get(item)) {
	    if (value > 0) {
		sum += value;
		++count;
	    }
	}

	double avg = (double) sum / (double) count;
	itemAverage.put(item, Double.valueOf(avg));
	return avg;
    }

}
