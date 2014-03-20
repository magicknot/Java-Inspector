package ist.meic.pa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;

public class Inspector {

	public enum TypeMatches {
		Integer, Double, Float, Long, String;

		private static HashMap<Class<?>, Method> matches = new HashMap<Class<?>, Method>();

		public static void init(String methodName, Class<?> wrapper,
				Class<?> primitive) {
			try {
				matches.put(primitive,
						wrapper.getMethod(methodName, java.lang.String.class));
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public static Object parseObject(Class<?> c, Object obj)
				throws IllegalArgumentException, IllegalAccessException,
				InvocationTargetException {
			return matches.get(c).invoke(c, obj);
		}

	}

	private HistoryGraph historyGraph;
	private SavedObjects savedObjects;
	private Object object;

	public Inspector() {
		historyGraph = new HistoryGraph();
		savedObjects = new SavedObjects();
		object = null;
		TypeMatches.init("parseInt", Integer.class, int.class);
		TypeMatches.init("parseDouble", Double.class, double.class);
		TypeMatches.init("parseFloat", Float.class, float.class);
		TypeMatches.init("parseLong", Long.class, long.class);
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
			}
		}
	}

	private void inspect(String name, int value) throws SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException, InstantiationException {

		Object tempObject = object;

		for (int i = 0; i < value; i++)
			tempObject = object.getClass().getSuperclass().newInstance();

		Field field = tempObject.getClass().getDeclaredField(name);

		if (!Modifier.isStatic(field.getModifiers())) {
			boolean originalAcess = field.isAccessible();
			field.setAccessible(true);
			updateObject(field.get(tempObject), field.getType());
			historyGraph.addToHistory(object);
			field.setAccessible(originalAcess);

		}

	}

	private void modify(String name, String value)
			throws IllegalArgumentException, IllegalAccessException,
			SecurityException, NoSuchFieldException, InvocationTargetException {

		Field field = object.getClass().getDeclaredField(name);

		boolean originalAcess = field.isAccessible();
		field.setAccessible(true);

		if (!Modifier.isStatic(field.getModifiers())) {
			if (field.getType().isPrimitive())
				field.set(object,
						TypeMatches.parseObject(field.getType(), value));
			else
				field.set(object, value);

			updateObject(object,field.getType());
			field.setAccessible(originalAcess);
		}

	}

	private void call(String args[]) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {

		Object[] methodArgs = new Object[args.length - 2];
		Class<?> myClass;

		if (object != null) {

			for (int i = 0; i < args.length - 2; i++) {
				if (args[i + 2].startsWith("#")) {
					methodArgs[i] = savedObjects.getObject(args[i + 2]
							.substring(1));
				} else {
					methodArgs[i] = getBestMatch(args[i + 2]);
				}
			}

			// verifica partindo da classe actual, passando depois `as
			// superclasses
			// se ha algum metodo com o mesmo nome
			myClass = object.getClass();

			while (!myClass.isInstance(Object.class)) {

				for (Method m : myClass.getMethods()) {
					if (m.getName().equals(args[1])
							&& hasCompatibleArgs(m, methodArgs)) {
						object = m.invoke(object, methodArgs);
						historyGraph.addToHistory(object);
						InfoPrinter.printObjectInfo(object);
						return;
					}
				}
				myClass = myClass.getSuperclass();
			}
		} else {
			InfoPrinter
					.printObjectInfo("cCommand: the object invocated does not exist");
		}
	}

	public boolean hasCompatibleArgs(Method m, Object args[]) {
		for (int i = 0; i < args.length; i++) {
			if (!m.getParameterTypes()[i].getName().equals(
					args[i].getClass().getName())) {
				return false;
			}
		}

		return m.getParameterTypes().length == args.length;
	}

	public static Object getBestMatch(String s) {
		try {
			for (Method m : TypeMatcher.class.getDeclaredMethods()) {
				try {
					return m.invoke(TypeMatcher.class, s);
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
			InfoPrinter.printObjectInfo(object);

		}

	}

	public void updateObject(Object obj, Class<?> c) {
		if (obj != null) {
			object = obj;
			InfoPrinter.printObjectInfo(object, c);
		}
	}
}
