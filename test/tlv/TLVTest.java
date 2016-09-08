package tlv;

import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static tlv.Util.toByteArray;

/**
 * Created by a2800276 on 2016-09-07.
 */
public class TLVTest {
	@Test
	public void testParse() throws Exception {

	}

	@Test
	public void testSingleByteTag() {
		for (short t = 0; ; ++t) {
			if ((t & 0x1F) == 0x1f) { // first multibyte, skip.
				break;
			}
			for (short v = 0; ; ++v) {
				testSingleTag(new byte[]{(byte)t, 0x01, (byte)v}, new byte[]{(byte)t}, new byte[]{(byte)v});
				if (v == 0xff) {
					break;
				}
			}
			if (t == 0xff) {
				break;
			}
		}
	}
	@Test
	public void testExtractAIDs()
	{
		byte[] pseRecord = { 0x70, 0x0e, 0x61, 0x0c, 0x4f, 0x07, (byte)0xa0, 0x00, 0x00, 0x00, 0x03, 0x10, 0x10, (byte)0x87, 0x01, 0x01};
		byte [] aid = { (byte)0xa0, 0x00, 0x00, 0x00, 0x03, 0x10, 0x10};
		// 0x47 is tag for AID => EMV Book3, A1 Data Dictionary
		//boolean called = false;
		TLV tlv = new TLV((tag, value, raw) -> {
			if (tag[0] == 0x4F) {
				//called = true;
				assertEquals(value, aid);
			}
		}, true);

		tlv.Parse(pseRecord);
		//assertTrue(called);
		assertTrue(tlv.isConsistant());

	}

	@Test
	public void testMultiByteTag ()
	{
		byte[] bytes = { (byte)0x9F, 0x00, 0x01, 0x02};
		byte[] tag_expected = {(byte)0x9F, 0x00};
		byte[] val_expected = {0x02};

		testSingleTag(bytes, tag_expected, val_expected);

		bytes = new byte[] { (byte)0x9F, (byte)0x9F, 0x00, 0x01, 0x02};
		tag_expected = new byte[] {(byte)0x9F, (byte)0x9F, 0x00};

		testSingleTag(bytes, tag_expected, val_expected);

		bytes = new byte[]{ (byte)0x9F, (byte)0x9F, 0x00, 0x03, 0x00, 0x01, 0x02};
		val_expected = new byte[]{0x00, 0x01, 0x02};

		testSingleTag(bytes, tag_expected, val_expected);
	}

	public void TestMultiByteLen ()
	{
		byte[] bytes = { (byte)0x9F, 0x00, (byte)0x81, 0x01, 0x02 };
		byte[] tag_expected = { (byte)0x9F, 0x00 };
		byte[] val_expected = { 0x02 };
		testSingleTag (bytes, tag_expected, val_expected);

		bytes = new byte[] { (byte)0x9f, 0x00, (byte)0x82, 0x00, 0x01, 0x02 };
		testSingleTag (bytes, tag_expected, val_expected);

		List<Byte> blist = new LinkedList<>();
		Util.addArraryToList(blist, tag_expected);

		// 256 bytes len encoded in 1 byte: 0x8*1* 0xff, ...
		blist.add ((byte)0x81);
		blist.add ((byte)0xff);
		for (int i = 1; i != 256; ++i) // start counting at 1 so % won't divide by zero.
		{
			blist.add((byte)(256 % i));
		}

		List<Byte> vlist = new LinkedList<>();
		for (int i = 1; i != 256; ++i)
		{
			vlist.add((byte)(256 % i));
		}
		testSingleTag (toByteArray(blist), tag_expected, toByteArray(vlist));

		// 257 bytes len encoded in 2 bytes: 0x8*2* 0x01, 0x00 ,...
		bytes = new byte[] { (byte)0x9f, 0x00, (byte)0x82, 0x01, 0x00};
		blist = new LinkedList<>();
		Util.addArraryToList(blist, bytes);
		for (int i = 1; i != 257; ++i)
		{
			blist.add((byte)(256 % i));
		}
		vlist = new LinkedList<>();
		for (int i = 1; i != 257; ++i)
		{
			vlist.add((byte)(256 % i));
		}
		testSingleTag (toByteArray(blist), tag_expected, toByteArray(vlist));

	}

	private void testSingleTag(byte[] tlvBytes, byte[] tag_expected, byte[] val_expected) {
		//boolean called = false;
		TLV tlv = new TLV((byte[] tag, byte[] val, byte[] raw) -> {
			//called = true;
			assertEquals(tag, tag_expected, "Tag incorrect");
			assertEquals(val, val_expected, "value incorrect");
		});
		tlv.Parse(tlvBytes);
		//Assert.True(called);
		assertTrue(tlv.isConsistant());
	}

}