import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

public class SimHash {

    public static void main(final String[] args) {
	try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {

	    Integer textAmount = Integer.parseInt(reader.readLine());

	    String line = null;
	    byte[] hash = null;
	    List<byte[]> simHashes = new ArrayList<>();

	    while (--textAmount >= 0) {
		line = reader.readLine();
		hash = textToSimHashBinary(line);
		simHashes.add(hash);
	    }

	    Integer similarityAmount = Integer.parseInt(reader.readLine());

	    String[] lineQ = null;
	    int textIndex = 0;
	    byte[] text = null;
	    Integer bits = null;
	    while (--similarityAmount >= 0) {
		lineQ = reader.readLine().split(" ");
		textIndex = Integer.parseInt(lineQ[0]);
		text = simHashes.get(textIndex);
		bits = Integer.parseInt(lineQ[1]);

		int count = 0;

		for (int index = 0; (index < simHashes.size()); ++index) {
		    if (index == textIndex) {
			continue;
		    }

		    byte[] simHash = simHashes.get(index);
		    int diffBits = 0;
		    for (int i = 0; i < 128; ++i) {

			if (simHash[i] != text[i]) {
			    ++diffBits;
			}
		    }
		    if ((diffBits >= 0) && (diffBits <= bits)) {
			++count;
		    }
		}

		System.out.println(count);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    public static byte[] textToSimHashBinary(final String text) {
	String[] units = text.split(" ");
	long[] sh = new long[128];
	for (int i = 0; i < sh.length; ++i) {
	    sh[i] = 0;
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
