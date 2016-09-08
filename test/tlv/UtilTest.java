package tlv;

import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * Created by a2800276 on 2016-09-07.
 */
public class UtilTest {
	@Test
	public void testAddArraryToList() throws Exception {
		List<Byte> bytes = new LinkedList<>();
		byte[] arr = {0x00, 0x01, 0x02};
		Util.addArraryToList(bytes, arr);
		assertEquals(bytes.size(), 3);
		assertEquals((byte)bytes.get(2), (byte)0x02);
	}

	@Test
	public void testToByteArray() throws Exception {
		List<Byte> bytes = new LinkedList<>();
		byte[] bs = Util.toByteArray(bytes);
		byte[] empty = new byte[0];
		assertEquals(bs, empty);
	}

}