package tlv;

import java.util.LinkedList;
import java.util.List;

import static tlv.Util.b2h;

/**
 * Created by a2800276 on 2016-09-07.
 */
public class TLVObject {

	private List<TLVObject> _children;
	private byte[] _tag;
	private byte[] _value;

	public TLVObject(byte[] bs)
	{
		this (null,bs);
	}

	protected TLVObject(byte[] tag, byte[] value) {


		_children = new LinkedList<>();
		_tag = tag;
		_value = value;

		if (isConstructed()) {
			// parse the children.
			TLV tlv = new TLV ((_t,_v, _l) -> {
				TLVObject obj = new TLVObject(_t, _v);
				_children.add(obj);
			});
			tlv.Parse(value);
		}
	}


	public byte[] tag()
	{
		return _tag;
	}
	public byte[] value()
	{
		return _value;
	}
	public boolean isConstructed()
	{
		// xx1x | xxxx : Constructed data object
		return _tag == null || isConstructed(_tag);
	}

	static boolean isConstructed(byte[] tag) {

		return (tag[0] & 0x20) == 0x20;
	}

	public List<TLVObject> getChildren()
	{
		return _children;
	}

	public String toString()
	{
		String ret = "";
		for (TLVObject child : _children) {
			ret += child.toString(0);
		}
		return ret;
	}

	public String toString(int indent)
	{
		String indentation = "";
		for (int i = 0; i != indent; ++i)
		{
			indentation += " ";
		}
		String ret = String.format("%sTag: %s Len: %s Value: %s\n", indentation, b2h(tag()), value().length, b2h(value()));
		for (TLVObject child : _children) {
			ret += child.toString(indent + 1);
		}
		return ret;
	}
}
