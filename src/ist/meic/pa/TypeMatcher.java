package ist.meic.pa;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TypeMatcher {

	public TypeMatcher() {

	}

	public Object getBestMatch(String s) {

		Object result = null;

		try {

			for (Method m : this.getClass().getDeclaredMethods()) {

				if (m.getName().equals("getBestMatch"))
					continue;
				else if (result == null)
					result = m.invoke(this, s);
				else
					return result;
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

	public Object IntegerMatch(String arg) {

		try {
			return Integer.parseInt(arg);
		} catch (NumberFormatException e) {
			return null;
		}

	}

	public Object FloatMatch(String arg) {

		try {
			return Float.parseFloat(arg);
		} catch (NumberFormatException e) {
			return null;
		}

	}

	public Object DoubleMatch(String arg) {

		try {
			return Double.parseDouble(arg);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public Object LongMatch(String arg) {

		try {
			return Long.parseLong(arg);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public Object ByteMatch(String arg) {

		try {
			return Byte.parseByte(arg);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public Object ShortMatch(String arg) {

		try {
			return Short.parseShort(arg);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public Object BooleanMatch(String arg) {

		try {
			return Boolean.parseBoolean(arg);
		} catch (NumberFormatException e) {
			return null;
		}

	}

}
