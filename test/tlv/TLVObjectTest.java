package tlv;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Created by a2800276 on 2016-09-07.
 */
public class TLVObjectTest {
	@Test
	public void basicTest () {
		byte[] pseRecord = { 0x70, 0x0e, 0x61, 0x0c, 0x4f, 0x07, (byte)0xa0, 0x00, 0x00, 0x00, 0x03, 0x10, 0x10, (byte)0x87, 0x01, 0x01};
		TLVObject tlv = new TLVObject(pseRecord);

		assertEquals(tlv.getChildren().size(), 1);

		String expected =
		"Tag: 70 Len: 14 Value: 610c4f07a0000000031010870101\n"+
		" Tag: 61 Len: 12 Value: 4f07a0000000031010870101\n"+
		"  Tag: 4f Len: 7 Value: a0000000031010\n"+
		"  Tag: 87 Len: 1 Value: 01\n";

		assertEquals(tlv.toString(), expected);
	}

}