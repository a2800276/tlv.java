package tlv;

import java.util.List;

/**
 * Created by a2800276 on 2016-09-07.
 */
public class Util {
	public static byte[] toByteArray(List<Byte> byteList) {
		byte [] bs = new byte[byteList.size()];
		Object [] os = byteList.toArray();
		//System.arraycopy(os, 0, bs, 0, bs.length);
		for (int i = 0; i!= os.length; ++i){
			bs[i] = (byte)os[i];
		}
		return bs;
	}
	public static void addArraryToList(List<Byte> list, byte[] bs) {
		for (byte b : bs) {
			list.add(b);
		}
	}

	final static byte[] hex = "0123456789abcdef".getBytes();

	public static String b2h(byte[] bytes) {
		if (null == bytes) return "";
		return b2h(bytes, 0, bytes.length);
	}

	public static String b2h(byte[] bytes, int offset, int count) {
		byte[] hexBytes = new byte[count * 2];
		for (int j = 0; j < count; j++) {
			int v = bytes[j + offset] & 0xFF;
			hexBytes[j * 2] = hex[v >>> 4];
			hexBytes[j * 2 + 1] = hex[v & 0x0F];
		}
		return new String(hexBytes);
	}
}
