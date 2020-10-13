import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PCY {

    public static void main(final String[] args) {
	try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {

	    Integer basketAmount = Integer.parseInt(reader.readLine());
	    Double supportThreshold = Double.parseDouble(reader.readLine());
	    Integer compartmentAmount = Integer.parseInt(reader.readLine());

	    List<int[]> baskets = new ArrayList<>(basketAmount);
	    int largest = 0;
	    while (--basketAmount >= 0) {
		String[] itemsInBasketString = reader.readLine().split(" ");
		int[] basket = new int[itemsInBasketString.length];
		for (int i = 0; i < itemsInBasketString.length; ++i) {
		    Integer item = Integer.parseInt(itemsInBasketString[i]);
		    if (item > largest) {
			largest = item;
		    }
		    basket[i] = item;
		}
		baskets.add(basket);
	    }

	    Integer threshold = Double.valueOf(Math.floor(supportThreshold * baskets.size())).intValue();

	    int[] itemCount = new int[largest + 1];

	    for (int[] basket : baskets) {
		for (int item : basket) {
		    ++itemCount[item];
		}
	    }

	    int[] compartments = new int[compartmentAmount];

	    for (int[] basket : baskets) {
		for (int i = 0; i < (basket.length - 1); ++i) {
		    for (int j = i + 1; j < basket.length; ++j) {
			if ((itemCount[basket[i]] >= threshold) && (itemCount[basket[j]] >= threshold)) {
			    int k = ((basket[i] * itemCount.length) + basket[j]) % compartmentAmount;
			    ++compartments[k];
			}
		    }
		}
	    }

	    Map<Tuple, Integer> pairs = new HashMap<>();

	    for (int[] basket : baskets) {
		for (int i = 0; i < (basket.length - 1); ++i) {
		    for (int j = i + 1; j < basket.length; ++j) {
			if ((itemCount[basket[i]] >= threshold) && (itemCount[basket[j]] >= threshold)) {
			    int k = ((basket[i] * itemCount.length) + basket[j]) % compartmentAmount;
			    if (compartments[k] >= threshold) {
				Tuple pair = new Tuple(basket[i], basket[j]);
				Integer val = pairs.get(pair);
				pairs.put(pair, (val != null ? val : 0) + 1);
			    }
			}
		    }
		}
	    }

	    int m = 0;
	    for (int itemCountNum : itemCount) {
		if (itemCountNum >= threshold) {
		    ++m;
		}
	    }
	    System.out.println((m * (m - 1)) / 2);

	    System.out.println(pairs.size());

	    List<Integer> sorted = new ArrayList<>(pairs.values());
	    Collections.sort(sorted, Collections.reverseOrder());

	    for (Integer num : sorted) {
		System.out.println(num);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    private static class Tuple {
	int first, second;

	public Tuple(final int first, final int second) {
	    this.first = first;
	    this.second = second;
	}

	@Override
	public int hashCode() {
	    return Objects.hash(first, second);
	}

	@Override
	public boolean equals(final Object obj) {
	    if (obj == this) {
		return true;
	    }

	    if (obj == null) {
		return false;
	    }

	    if (!(obj instanceof Tuple)) {
		return false;
	    }

	    return (this.first == ((Tuple) obj).first) && (this.second == ((Tuple) obj).second);
	}
    }
}
