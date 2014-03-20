package ist.meic.pa;

public class TypeMatcher {

	public TypeMatcher() {
		// nothing to do here...
	}

	public static Object IntegerMatch(String arg) throws NumberFormatException {
		return Integer.parseInt(arg);
	}

	public static Object FloatMatch(String arg) throws NumberFormatException {
		return Float.parseFloat(arg);
	}

	public static Object DoubleMatch(String arg) throws NumberFormatException {
		return Double.parseDouble(arg);
	}

	public static Object LongMatch(String arg) throws NumberFormatException {
		return Long.parseLong(arg);
	}

	public static Object ByteMatch(String arg) throws NumberFormatException {
		return Byte.parseByte(arg);
	}

	public static Object ShortMatch(String arg) throws NumberFormatException {
		return Short.parseShort(arg);
	}

	public static Object BooleanMatch(String arg) throws NumberFormatException {
		return Boolean.parseBoolean(arg);
	}
}
