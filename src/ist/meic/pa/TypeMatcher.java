package ist.meic.pa;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TypeMatcher {

	public TypeMatcher() {
		// Nothing to do here...
	}

	public Object getBestMatch(String s) {
		Object result = null;

		try {
			for (Method m : this.getClass().getDeclaredMethods()) {
				if (m.getName().equals("getBestMatch")) {
					continue;
				}

				try {
					result = m.invoke(this, s);
					return result;
				} catch (NumberFormatException e) {
					continue;
				}
			}

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return s;
	}

	public Object IntegerMatch(String arg) throws NumberFormatException {
		return Integer.parseInt(arg);
	}

	public Object FloatMatch(String arg) throws NumberFormatException {
		return Float.parseFloat(arg);
	}

	public Object DoubleMatch(String arg) throws NumberFormatException {
		return Double.parseDouble(arg);
	}

	public Object LongMatch(String arg) throws NumberFormatException {
		return Long.parseLong(arg);
	}

	public Object ByteMatch(String arg) throws NumberFormatException {
		return Byte.parseByte(arg);
	}

	public Object ShortMatch(String arg) throws NumberFormatException {
		return Short.parseShort(arg);
	}

	public Object BooleanMatch(String arg) throws NumberFormatException {
		return Boolean.parseBoolean(arg);
	}
}
