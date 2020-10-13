import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;

public class SimHashBuckets {

    private static int bands = 8;

    public static void main(final String[] args) {
	File file = new File(args[0]);

	if (!(file.exists() && file.canRead())) {
	    System.exit(1);
	}

//	long time = System.currentTimeMillis();
	List<Integer> counts = new ArrayList<>();
	List<String[]> lines = new ArrayList<>();

	try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
//	try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {

	    Integer textAmount = Integer.parseInt(reader.readLine());

	    String line = null;
	    byte[] hash = null;
	    List<byte[]> simHashes = new ArrayList<>();

//	    long time = System.currentTimeMillis();
	    while (--textAmount >= 0) {
		line = reader.readLine();
		hash = textToSimHashBinary(line);
		simHashes.add(hash);
	    }

//	    Set<Map<Integer, Set<Integer>>> candidates = LSH(simHashes);
	    Map<Integer, List<Integer>> candidates = LSH(simHashes);

	    Integer similarityAmount = Integer.parseInt(reader.readLine());

	    String[] lineQ = null;
	    byte[] text = null;
	    int textIndex = 0;
	    Integer bits = null;

	    while (--similarityAmount >= 0) {

		lineQ = reader.readLine().split(" ");
		textIndex = Integer.parseInt(lineQ[0]);
		text = simHashes.get(textIndex);
		bits = Integer.parseInt(lineQ[1]);

		int count = 0;

		List<Integer> similarHashesIndexes = candidates.get(Integer.parseInt(lineQ[0]));
		similarHashesIndexes = new ArrayList<>(new HashSet<>(similarHashesIndexes));

		for (Integer index : similarHashesIndexes) {
		    if (index == textIndex) {
			continue;
		    }

		    hash = simHashes.get(index);
		    int diffBits = 0;
		    for (int i = 0; i < 128; ++i) {

			if (hash[i] != text[i]) {
			    ++diffBits;
			}

		    }
		    if ((diffBits >= 0) && (diffBits <= bits)) {
			++count;
		    }
		}

//		System.out.println(count);
		counts.add(count);
		lines.add(lineQ);

//		System.out.println(System.currentTimeMillis() - time);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

//	System.out.println(System.currentTimeMillis() - time);

	System.out.println("Start provjere");

	file = new File(args[1]);

	if (!(file.exists() && file.canRead())) {
	    System.exit(1);
	}

	try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

	    for (int i = 0; i < counts.size(); ++i) {
		Integer count = counts.get(i);
		Integer num = Integer.parseInt(reader.readLine());
		if (num != count) {
		    String[] a = lines.get(i);
		    System.out.println("broj " + i + "(unos " + a[0] + " " + a[1] + ") treba biti " + num
			    + " a ja dobio " + count);
		}
	    } //
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    private static Map<Integer, List<Integer>> LSH(final List<byte[]> simHashes) {
	Map<Integer, List<Integer>> candidates = new HashMap<>(simHashes.size());
	for (int i = 0; i < simHashes.size(); ++i) {
	    candidates.put(i, new ArrayList<Integer>());
	}
	byte[] hash = null;
	for (int i = 0; i < bands; ++i) {
	    Map<Integer, Set<Integer>> buckets = new HashMap<>();

//	    long time = 0L;
	    for (int currentId = 0; currentId < simHashes.size(); ++currentId) {
//		if ((currentId % 1000) == 0) {
//		    time = System.currentTimeMillis();
//		}
		hash = simHashes.get(currentId);
		byte[] bucket = new byte[128 / bands];
		int stop = 128 - ((i + 1) * (128 / bands));
		for (int k = 127 - (i * (128 / bands)); k >= stop; --k) {
		    bucket[k % 16] = hash[k];
		}
//		int value = ByteBuffer.wrap(bucket).getInt();
		int value = 0;
		for (int k = 0; k < bucket.length; ++k) {
		    value = (value << 1) | bucket[k];
		}
		Set<Integer> bucketIds = null;

		if (buckets.containsKey(value)) {
		    bucketIds = buckets.get(value);

		    for (Integer bucketId : bucketIds) {
			candidates.get(currentId).add(bucketId);
			candidates.get(bucketId).add(currentId);
		    }

		} else {
		    bucketIds = new HashSet<>();
		    buckets.put(value, bucketIds);
		}
		bucketIds.add(currentId);
//		if ((currentId % 1000) == 0) {
//		    System.out.println(System.currentTimeMillis() - time);
//		}
	    }

	}
	return candidates;
    }

//    private static Set<Map<Integer, Set<Integer>>> LSH(final List<byte[]> simHashes) {
//	Set<Map<Integer, Set<Integer>>> candidates = new HashSet<>();
//	byte[] hash = null;
//	for (int i = 0; i < bands; ++i) {
//	    Map<Integer, Set<Integer>> buckets = new HashMap<>();
//	    candidates.add(buckets);
//
//	    for (int currentId = 0; currentId < simHashes.size(); ++currentId) {
//		hash = simHashes.get(currentId);
//		byte[] bucket = new byte[128 / bands];
//		int stop = 128 - ((i + 1) * (128 / bands));
//		for (int k = 127 - (i * (128 / bands)); k >= stop; --k) {
//		    bucket[k % 16] = hash[k];
//		}
////		int value = ByteBuffer.wrap(bucket).getInt();
//		int value = 0;
//		for (int k = 0; k < bucket.length; ++k) {
//		    value = (value << 1) | bucket[k];
//		}
//		Set<Integer> bucketIds;
//
//		if (buckets.containsKey(value)) {
//		    bucketIds = buckets.get(value);
//
//		} else {
//		    bucketIds = new HashSet<>();
//		    buckets.put(value, bucketIds);
//		}
//		bucketIds.add(currentId);
//	    }
//	}
//	return candidates;
//    }

    public static byte[] textToSimHashBinary(final String text) {
	String[] units = text.split(" ");
	long[] sh = new long[128];
	for (int i = 0; i < sh.length; ++i) {
	    sh[i] = 0L;
	}
	for (int i = 0; i < units.length; ++i) {
	    byte[] digested = DigestUtils.md5(units[i]);
	    for (int j = 0; j < digested.length; ++j) {
		for (int k = 7; k >= 0; --k) {
		    if (((digested[j] >> k) & 1) == 1) {
			sh[((j * 8) + 7) - k] += 1;
		    } else {
			sh[((j * 8) + 7) - k] -= 1;
		    }
		}
	    }
	}
	byte[] shByte = new byte[128];

	for (int i = 0; i < sh.length; ++i) {
	    if (sh[i] >= 0L) {
		shByte[i] = 1;
	    } else {
		shByte[i] = 0;
	    }
	}

	return shByte;

    }

}
