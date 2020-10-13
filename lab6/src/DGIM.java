import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class DGIM {

    private static final LinkedList<Bucket> buckets = new LinkedList<>();
    private static long index = -1;
    private static Integer windowSize;

    public static void main(final String[] args) {

	try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
	    String line = reader.readLine();
	    windowSize = Integer.parseInt(line);
	    line = reader.readLine();
	    while (line != null) {
		if (line.startsWith("q")) {
		    processQuery(line);
		} else {
		    processBitStream(line);
		}
		line = reader.readLine();
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private static void processQuery(final String line) {
	Integer k = Integer.parseInt(line.split(" ")[1]);

	long windowStop = (index - k) + 1;
	int sum = 0;
	int prevSize = 0;
	boolean last = false;
	for (Bucket bucket : buckets) {
	    if ((windowStop > bucket.endTimestamp) && (windowStop > bucket.beginTimestamp)) {
		last = true;
		break;
	    }
	    if (buckets.indexOf(bucket) == (buckets.size() - 1)) {
		sum += (bucket.size / 2);
		break;
	    }
	    if (((windowStop >= bucket.endTimestamp) && (windowStop <= bucket.beginTimestamp))) {
		sum += (bucket.size / 2);
		break;
	    }

	    sum += bucket.size;
	    prevSize = bucket.size;
	}

	if (last) {
	    sum -= prevSize;
	    sum += (prevSize / 2);
	}

	System.out.println(sum);

    }

    private static void processBitStream(final String line) {
	char[] chars = line.toCharArray();
	for (char character : chars) {
	    ++index;

	    if (character == '1') {
		addOneToBuckets();

	    }
	    try {
		if (index >= (windowSize - 1)) {
		    long oldestTimestamp = (index - windowSize) + 1;
		    Bucket oldestBucket = buckets.getLast();
		    while (oldestBucket.beginTimestamp <= oldestTimestamp) {
			buckets.removeLast();
			oldestBucket = buckets.getLast();
		    }
		}
	    } catch (NoSuchElementException e) {
	    }

	}
    }

    private static void addOneToBuckets() {
	Bucket bucket = new Bucket(index, 1);
	buckets.addFirst(bucket);

	int i = 0;
	try {
	    Bucket bucket1 = buckets.get(1);
	    Bucket bucket2 = buckets.get(2);

	    if (bucket1.size == bucket2.size) {
		bucket1.size += bucket2.size;
		bucket1.endTimestamp = bucket2.endTimestamp;
		buckets.remove(2);

		for (i = 2; i < (buckets.size() - 1); ++i) {
		    bucket1 = buckets.get(i);
		    bucket2 = buckets.get(i + 1);

		    if (bucket1.size == bucket2.size) {
			bucket1.size += bucket2.size;
			bucket1.endTimestamp = bucket2.endTimestamp;
			buckets.remove(i + 1);
		    } else {
			break;
		    }
		}
	    }
	} catch (IndexOutOfBoundsException e) {
	    if (!((buckets.size() <= 2) || (buckets.size() <= (i + 1)))) {
		throw e;
	    }
	}

    }

    private static class Bucket {
	private final long beginTimestamp;
	private long endTimestamp;
	private int size;

	public Bucket(final long timestamp, final int size) {
	    this.beginTimestamp = timestamp;
	    this.endTimestamp = timestamp;
	    this.size = size;
	}
    }

}
