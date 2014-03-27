package ist.meic.pa;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * This Enumerate stores information about primitive types
 */

public enum Types {
	Integer(int.class, Integer.class), Short(short.class, Short.class), Byte(
			byte.class, Byte.class), Float(float.class, Float.class), Double(
			double.class, Double.class), Long(long.class, Long.class), Boolean(
			boolean.class, Boolean.class), Char(char.class, Character.class);

	/**
	 * Maps primitive types with the corresponding wrapper type constructor
	 */
	private static HashMap<Class<?>, Constructor<?>> matches = new HashMap<Class<?>, Constructor<?>>();
	/**
	 * Maps primitive types with a unique numeric value
	 */
	private static HashMap<Class<?>, Integer> matchNumber = new HashMap<Class<?>, Integer>();
	/**
	 * The primitive type itself
	 */
	private Class<?> primitive;
	/**
	 * The wrapper type of the primitive type
	 */
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

	/**
	 * Saves in a map, a correspondence between a primitive type and the
	 * constructor of its wrapper type
	 * 
	 * @param wrapper
	 *            - the wrapper type class
	 * @param primitive
	 *            - the primitive type class
	 */
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

	/**
	 * Receives a class and a string value and tries to find a match, to convert
	 * the string into an object of the type of the class provided
	 * 
	 * @param c
	 *            - the class
	 * @param arg
	 *            - the string argument
	 * @param savedObjects
	 *            - the map with objects previously saved
	 * @return - the object created
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 */
	public static Object parseArg(Class<?> c, String arg,
			Map<String, InspectedObject> savedObjects)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, SecurityException,
			NoSuchMethodException, InstantiationException {

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

	/**
	 * Checks if a string contains a char
	 * 
	 * @param arg
	 *            - string argument
	 * @return - true or false
	 */
	private static boolean isChar(String arg) {
		return arg.startsWith("\'");
	}

	/**
	 * Checks if a string contains a reference to an object previously saved
	 * 
	 * @param arg
	 *            - string argument
	 * @return - true or false
	 */
	public static boolean isSaved(String arg) {
		return arg.startsWith("#");
	}

	/**
	 * Checks if class corresponds to a primitive type
	 * 
	 * @param c
	 *            - the class
	 * @return - true or false
	 */
	private static boolean isPrimitive(Class<?> c) {
		return matches.containsKey(c);
	}

	/**
	 * Checks if a string truly contains a string
	 * 
	 * @param arg
	 *            - the string
	 * @return - true or false
	 */
	private static boolean isString(String arg) {
		return arg.startsWith("\"") && arg.endsWith("\"");
	}

	/**
	 * Returns the unique numeric identifier given to the primitive type of c,
	 * or a higher value, if c doesn't represent a primitive type
	 * 
	 * @param c
	 *            - the class
	 * @return - the numeric value
	 */
	public static int getPriorityValue(Class<?> c) {
		if (isPrimitive(c)) {
			return matchNumber.get(c);
		} else
			return matchNumber.size() + 1;
	}

	/**
	 * Returns a constructor of the class, which receives one arguments of class
	 * argType
	 * 
	 * @param type
	 *            - the class
	 * @param argType
	 *            - the class of the constructor argument
	 * @return - the constructor
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	private static Constructor<?> getTypeConstructor(Class<?> type,
			Class<?> argType) throws SecurityException, NoSuchMethodException {
		return type.getConstructor(argType);
	}

}
