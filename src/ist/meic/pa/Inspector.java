package ist.meic.pa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

public class Inspector {

	public enum TypeMatches {
		Integer, Double, Float, Long, String;

		private static HashMap<Class<?>, Method> matches = new HashMap<Class<?>, Method>();
		private static HashMap<Class<?>, Integer> matchNumber = new HashMap<Class<?>, Integer>();

		public static void init(String methodName, Class<?> wrapper,
				Class<?> primitive, Class<?> returnType) {
			try {
				matches.put(primitive,
						wrapper.getMethod(methodName, returnType));
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
				SavedObjects savedObjects) throws IllegalArgumentException,
				IllegalAccessException, InvocationTargetException,
				SecurityException, NoSuchMethodException {

			if (isChar(arg) && isPrimitive(c)) {
				return matches.get(c).invoke(arg.substring(1), 0);
			}

			if (isSaved(arg)) {
				return savedObjects.getObject(arg.substring(1));
			}

			if (isString(arg)) {
				return arg.substring(1, arg.length() - 1);
			}

			if (isPrimitive(c)) {
				return matches.get(c).invoke(c, arg);
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

			if (matchNumber.containsKey(c)) {
				return matchNumber.get(c);
			} else
				return matchNumber.size() + 1;
		}

	}

	private HistoryGraph historyGraph;
	private SavedObjects savedObjects;
	private Object object;

	public Inspector() {
		historyGraph = new HistoryGraph();
		savedObjects = new SavedObjects();
		object = null;
		TypeMatches.init("parseInt", Integer.class, int.class, String.class);
		TypeMatches.init("parseDouble", Double.class, double.class,
				String.class);
		TypeMatches.init("parseFloat", Float.class, float.class, String.class);
		TypeMatches.init("parseLong", Long.class, long.class, String.class);
		TypeMatches.init("parseBoolean", Boolean.class, boolean.class,
				String.class);
		TypeMatches.init("parseByte", Byte.class, byte.class, String.class);
		TypeMatches.init("parseShort", Short.class, short.class, String.class);
		TypeMatches.init("charAt", String.class, char.class, int.class);
	}

	public void inspect(Object object) {
		updateObject(object);
		historyGraph.addToHistory(object);
		readEvalPrint();
	}

	public void readEvalPrint() {
		BufferedReader buffer = new BufferedReader(new InputStreamReader(
				System.in));

		while (true) {
			System.err.print("> ");

			try {
				String arguments[] = buffer.readLine().split(" ");

				if (arguments[0].equals("q")) {
					buffer.close();
					return;
				} else if (arguments[0].equals("i")) {
					if (arguments.length < 3) {
						inspect(arguments[1], 0);
					} else {
						inspect(arguments[1], Integer.parseInt(arguments[2]));
					}
				} else if (arguments[0].equals("m")) {
					modify(arguments[1], arguments[2]);
				} else if (arguments[0].equals("c")) {
					call(arguments);
				} else if (arguments[0].equals("n")) {
					next();
				} else if (arguments[0].equals("p")) {
					previous();
				} else if (arguments[0].equals("s")) {
					save(arguments[1]);
				} else if (arguments[0].equals("g")) {
					get(arguments[1]);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// value tem valor por omissao zero no caso em que nao vai para superclasses
	private void inspect(String name, int value) throws SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException, InstantiationException {

		Field field;
		Class<?> actualClass = object.getClass();

		if (value != 0) {

			for (int i = 0; i < value; i++) {
				actualClass = actualClass.getClass().getSuperclass();
			}
			field = actualClass.getField(name);

		} else {
			field = getFieldByName(name);
		}

		if (field != null) {
			boolean originalAcess = field.isAccessible();
			field.setAccessible(true);
			Object fieldObj = field.get(object);
			field.setAccessible(originalAcess);

			updateObject(fieldObj, field.getType());
			historyGraph.addToHistory(fieldObj);
		} else {
			InfoPrinter.printNullInfo("inspect");
		}

	}

	private void modify(String name, String value)
			throws IllegalArgumentException, IllegalAccessException,
			SecurityException, NoSuchFieldException, InvocationTargetException,
			InstantiationException, NoSuchMethodException {

		Field field = getFieldByName(name);

		if (field != null) {

			boolean originalAccess = field.isAccessible();
			field.setAccessible(true);

			if (field.getType().isPrimitive()) {
				field.set(object, TypeMatches.parseArg(field.getType(), value,
						savedObjects));

			} else {
				field.set(object, value);
			}

			field.setAccessible(originalAccess);
			updateObject(object);

		} else {
			InfoPrinter.printNullInfo("modify");
		}

	}

	public Field getFieldByName(String name) {

		Class<?> actualClass = object.getClass();

		// procura o field na classe e se nao encontrar procura nas superclasses
		while (actualClass != Object.class) {
			for (Field f : actualClass.getDeclaredFields())
				if (f.getName().equals(name)
						&& !Modifier.isStatic(f.getModifiers())) {
					return f;
				}

			actualClass = actualClass.getSuperclass();
		}

		return null;

	}

	private void call(String args[]) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			SecurityException, NoSuchMethodException {

		Method bestMethod = null;
		Object[] methodArgs = new Object[args.length - 2];
		Class<?> actualClass = object.getClass();

		// bestMethod = filterMethods(actualClass.getDeclaredMethods(), args);

		while (bestMethod == null || actualClass == Object.class) {

			bestMethod = filterMethods(actualClass.getDeclaredMethods(), args);
			actualClass = actualClass.getSuperclass();

		}

		// ordena os metodos e escolhe o mais compativel

		// fazer a conversao dos argumentos do input conforme
		// os tipos do metodo que escolheu
		for (int i = 0; i < bestMethod.getParameterTypes().length; i++)
			methodArgs[i] = TypeMatches.parseArg(
					bestMethod.getParameterTypes()[i], args[i + 2],
					savedObjects);

		updateObject(bestMethod.invoke(object, methodArgs));
		historyGraph.addToHistory(object);

	}

	public Method filterMethods(Method[] methods, String[] args) {

		int minVal = 0;
		Method bestMethod = null;
		int tempVal = 0;
		int nProvidedArgs = args.length - 2;

		// descarta todos os metodos que sejam divergentes no
		// numero de argumentos ou nome
		for (Method m : methods) {
			if (m.getName().equals(args[1])
					&& m.getParameterTypes().length == nProvidedArgs
					&& isCompatible(args, m.getParameterTypes())) {

				tempVal = classifyMethod(m, args);

				if (tempVal < minVal || minVal == 0) {
					minVal = tempVal;
					bestMethod = m;
				}

			}

		}

		return bestMethod;

	}

	public int classifyMethod(Method method, String[] args) {

		int value = 0;
		int multiple = 1;

		for (Class<?> c : method.getParameterTypes()) {
			value = value + multiple * TypeMatches.getPriorityValue(c);
			multiple = multiple * 10;
		}

		return value;
	}

	public boolean isCompatible(String args[], Class<?> methodArgs[]) {

		try {
			for (int i = 0; i < args.length - 2; i++) {
				TypeMatches.parseArg(methodArgs[i], args[i + 2], savedObjects);
			}

			return true;

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;

	}

	private void next() {
		updateObject(historyGraph.getNext());
	}

	private void previous() {
		updateObject(historyGraph.getPrevious());
	}

	private void save(String arg) {
		savedObjects.saveObject(arg, object);
	}

	private void get(String arg) {
		updateObject(savedObjects.getObject(arg));
		historyGraph.addToHistory(object);
	}

	public void updateObject(Object obj) {
		if (obj != null) {
			object = obj;
			InfoPrinter.printObjectInfo(obj, obj.getClass().getCanonicalName());

		}

	}

	public void updateObject(Object obj, Class<?> classType) {
		if (obj != null) {
			object = obj;

			if (classType.isPrimitive()) {
				InfoPrinter.printObjectInfo(obj, classType.toString());
			} else {
				InfoPrinter.printObjectInfo(obj, obj.getClass()
						.getCanonicalName());
			}

		}
	}
}
