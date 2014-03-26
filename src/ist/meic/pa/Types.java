package ist.meic.pa;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public enum Types {
	Integer(int.class, Integer.class), Short(short.class, Short.class), Byte(
			byte.class, Byte.class), Float(float.class, Float.class), Double(
			double.class, Double.class), Long(long.class, Long.class), Boolean(
			boolean.class, Boolean.class), Char(char.class, Character.class);

	private static HashMap<Class<?>, Constructor<?>> matches = new HashMap<Class<?>, Constructor<?>>();
	private static HashMap<Class<?>, Integer> matchNumber = new HashMap<Class<?>, Integer>();
	private Class<?> primitive;
	private Class<?> wrapper;

	Types(Class<?> primType, Class<?> wrapperType) {
		this.primitive = primType;
		this.wrapper = wrapperType;
	}

	public Class<?> getPrimitive() {
		return primitive;
	}

	public Class<?> getWrapper() {
		return wrapper;
	}

	public static void init(Class<?> wrapper, Class<?> primitive) {
		try {
			if (primitive == char.class) {
				matches.put(primitive, getTypeConstructor(wrapper, char.class));
			} else {
				matches.put(primitive,
						getTypeConstructor(wrapper, String.class));
			}
			matchNumber.put(primitive, matchNumber.size() + 1);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Object parseArg(Class<?> c, String arg,
			Map<String, Object> savedObjects) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			SecurityException, NoSuchMethodException, InstantiationException {

		if (isChar(arg) && isPrimitive(c)) {
			return matches.get(c).newInstance(arg.charAt(1));
		}

		if (isSaved(arg)) {
			return savedObjects.get(arg.substring(1));
		}

		if (isString(arg)) {
			return arg.substring(1, arg.length() - 1);
		}

		if (isPrimitive(c)) {
			return matches.get(c).newInstance(arg);
		}

		return arg;
	}

	private static boolean isString(String arg) {
		return arg.startsWith("\"") && arg.endsWith("\"");
	}

	private static boolean isChar(String arg) {
		return arg.startsWith("\'");
	}

	private static boolean isSaved(String arg) {
		return arg.startsWith("#");
	}

	private static boolean isPrimitive(Class<?> c) {
		return matches.containsKey(c);
	}

	public static int getPriorityValue(Class<?> c) {
		if (isPrimitive(c)) {
			return matchNumber.get(c);
		} else
			return matchNumber.size() + 1;
	}

	private static Constructor<?> getTypeConstructor(Class<?> type,
			Class<?> argType) throws SecurityException, NoSuchMethodException {
		return type.getConstructor(argType);
	}

}