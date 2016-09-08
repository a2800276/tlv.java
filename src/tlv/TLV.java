package tlv;

import java.util.LinkedList;
import java.util.List;

import static tlv.TLV.State.Value;
import static tlv.Util.toByteArray;

/**
 * Created by a2800276 on 2016-09-07.
 */
public class TLV {

	private OnTag _callback;
	private State _state;

	private List<Byte> _tag;   // tag currently being collected
	private List<Byte> _value; // value currently being collected
	private List<Byte> _rawLengthBytes;

	private int _len;       // not reliable externally, decremented while collecting value
	private int _lenBytes; // number of length bytes (encoded in low bits of first len byte if hi bit is set)

	private boolean _recurse;  // recurse over constructed elements.

	public interface OnTag {
		void onTag(byte[] tag, byte[] value, byte[] rawLengthBytes);
	}

	enum State {
		FirstTagByte,
		NextTagByte,
		FirstLenByte,
		NextLenByte,
		Value,
	}

	protected TLV() {
		// required for derived classes ...
	}

	/**
	 * Construct a TLV parser with a callback. The callback is
	 * executed every time a tag has been completely parsed, i.e the
	 * tag and length have been recognized, and the indicated number of
	 * bytes has been retrieved as the value.
	 */
	public TLV(OnTag callback) {
		_callback = callback;
		_state = State.FirstTagByte;

	}

	/**
	 * run TLV parser with recursion over constructed elements. Note that this
	 * means that a callback may be exceuted multiple times: once for the
	 * parent element and then for the nested child elements.
	 */
	public TLV(OnTag callback, boolean recurse) {
		this(callback);
		_recurse = recurse;
	}


	// Is the parser in a consistant state? I.e. has a TLV value been completely
	// read or are there bytes missing?

	public boolean isConsistant() {
		return _state == State.FirstTagByte;
	}

	public void Parse(byte[] bytes) {
		Parse(bytes, 0);
	}

	public void Parse(byte[] bytes, int offset) {
		for (int i = offset; i != bytes.length; ++i) {
			switch (_state) {
				case FirstTagByte:
					_tag = new LinkedList<>();
					_tag.add(bytes[i]);
					// xxx1 | 1111 : See subsequent bytes
					if ((bytes[i] & 0x1F) == 0x1F) {
						_state = State.NextTagByte;
					} else {
						_state = State.FirstLenByte;
					}
					break;

				case NextTagByte:
					_tag.add(bytes[i]);
					// 1xxx | xxxx : Another byte follows
					if ((bytes[i] & 0x80) != 0x80) {
						_state = State.FirstLenByte;
					}
					break;

				case FirstLenByte:
					_rawLengthBytes = new LinkedList<>();
					_rawLengthBytes.add(bytes[i]);
					_len = 0;
					if ((bytes[i] & 0x80) == 0x80) {
						// 1LLL | LLLL
						_lenBytes = bytes[i] & 0x7F;
						_state = State.NextLenByte;
					} else {
						_len = bytes[i];
						initiateValue();
					}
					break;

				case NextLenByte:
					_rawLengthBytes.add(bytes[i]);
					_len <<= 8;
					_len += bytes[i];
					_lenBytes--;
					if (_lenBytes == 0) {
						_value = new LinkedList<>();
						_state = Value;
					}
					break;

				case Value:
					_value.add(bytes[i]);
					_len--;
					if (_len == 0) {
						callDelegate();
						_state = State.FirstTagByte;
					}
					break;

			} // switch
		} // for
	}// Parse

	private void initiateValue() {
		_value = new LinkedList<>();
		if (_len == 0) {
			// there is no value!
			callDelegate();
			_state = State.FirstTagByte;
		} else {
			_state = State.Value;
		}
	}

	private void callDelegate() {
		_callback.onTag(toByteArray(_tag), toByteArray(_value), toByteArray(_rawLengthBytes));
		if (_recurse) {
			handleNested();
		}
	}

	static private final Byte[] BYTEARRAY = new Byte[0];

	private void handleNested() {
		if (_value.size() != 0) {
			// xx1x | xxxx : Constructed data object
			if ((_tag.get(0) & 0x20) == 0x20) {
				TLV nested = new TLV(_callback, true);
				nested.Parse(toByteArray(_value));
			}
		}
	}
}
