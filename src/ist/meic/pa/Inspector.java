package ist.meic.pa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Inspector {

	private HistoryGraph historyGraph;
	private SavedObjects savedObjects;
	private Command command;
	private Object object;

	public Inspector() {
		historyGraph = new HistoryGraph();
		savedObjects = new SavedObjects();
		object = null;
		command = new Command(this);

		for (Types type : Types.values()) {
			Types.init(type.getWrapper(), type.getPrimitive());
		}
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
				} else {
					command.getClass()
							.getDeclaredMethod(arguments[0], String[].class)
							.invoke(command, new Object[] { arguments });
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}

	void inspect(String name, int value) throws SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException, InstantiationException {

		if (object == null || getObjectClass(object).isPrimitive())
			return;

		Field field;
		Class<?> actualClass = getObjectClass(object);

		if (value != 0) {
			for (int i = 0; i < value; i++) {
				actualClass = actualClass.getSuperclass();
			}
			field = getFieldOnClass(name, actualClass);
		} else {
			field = getFieldInAnyClass(name);
		}

		if (field != null) {
			boolean originalAcess = field.isAccessible();
			field.setAccessible(true);
			Object fieldObj = field.get(object);
			field.setAccessible(originalAcess);
			updateObject(fieldObj);
			historyGraph.addToHistory(fieldObj);
		} else {
			InfoPrinter.printNullInfo("inspect");
		}
	}

	void modify(String name, String value) throws IllegalArgumentException,
			IllegalAccessException, SecurityException, NoSuchFieldException,
			InvocationTargetException, InstantiationException,
			NoSuchMethodException {

		if (object == null || getObjectClass(object).isPrimitive())
			return;

		Field field = getFieldInAnyClass(name);

		if (field != null) {
			boolean originalAccess = field.isAccessible();
			field.setAccessible(true);

			if (Types.isParsable(field.getType())) {
				field.set(object, parse(field.getType(), value));
			} else {
				field.set(object, value);
			}
			field.setAccessible(originalAccess);
			updateObject(object);
		} else {
			InfoPrinter.printNullInfo("modify");
		}
	}

	private Field getFieldInAnyClass(String name) {
		Class<?> actualClass = getObjectClass(object);
		Field field = null;

		while (actualClass != Object.class && field == null) {
			field = getFieldOnClass(name, actualClass);
			actualClass = actualClass.getSuperclass();
		}
		return field;
	}

	private Field getFieldOnClass(String name, Class<?> classWithField) {
		for (Field f : classWithField.getDeclaredFields()) {
			if (f.getName().equals(name)
					&& !Modifier.isStatic(f.getModifiers())) {
				return f;
			}
		}
		return null;
	}

	void call(String inputArgs[], String methodName)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, SecurityException, NoSuchMethodException {

		Object[] methodArgs = new Object[inputArgs.length];
		Class<?> actualClass = getObjectClass(object);
		Method bestMethod = null;

		if (object == null || getObjectClass(object).isPrimitive())
			return;

		while (bestMethod == null && actualClass != Object.class) {
			bestMethod = filterMethods(actualClass.getDeclaredMethods(),
					inputArgs, methodName);
			actualClass = actualClass.getSuperclass();
		}

		if (bestMethod == null) {
			InfoPrinter.printNullInfo("call");
			return;
		}

		for (int i = 0; i < bestMethod.getParameterTypes().length; i++) {
			if (bestMethod.getParameterTypes()[i] == Object.class) {
				methodArgs[i] = parseObjectType(inputArgs[i]);
			} else {
				methodArgs[i] = parse(bestMethod.getParameterTypes()[i],
						inputArgs[i]);
			}
		}

		updateObject(bestMethod.invoke(object, methodArgs));
		historyGraph.addToHistory(object);
	}

	private Method filterMethods(Method[] methods, String[] args, String name) {
		int minVal = 0;
		Method bestMethod = null;
		int tempVal = 0;

		for (Method m : methods) {
			if (m.getName().equals(name)
					&& m.getParameterTypes().length == args.length
					&& isCompatible(args, m.getParameterTypes())) {

				tempVal = classifyMethod(m);

				if (tempVal < minVal || minVal == 0) {
					minVal = tempVal;
					bestMethod = m;
				}
			}
		}
		return bestMethod;
	}

	private int classifyMethod(Method method) {
		int value = 0;
		int multiple = 1;

		for (Class<?> c : method.getParameterTypes()) {
			value = value + multiple * Types.getPriorityValue(c);
			multiple = multiple * 10;
		}
		return value;
	}

	private Object parseObjectType(String arg) {
		Object obj;

		for (Types type : Types.values()) {
			obj = parse(type.getPrimitive(), arg);
			if (obj != null)
				return obj;
		}
		return null;
	}

	private Object parse(Class<?> type, String arg) {
		try {
			return Types.parseArg(type, arg, savedObjects);
		} catch (IllegalArgumentException e) {
		} catch (SecurityException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		} catch (NoSuchMethodException e) {
		} catch (InstantiationException e) {
		}
		return null;
	}

	private boolean isCompatible(String args[], Class<?> methodArgs[]) {

		boolean result = true;

		for (int i = 0; i < args.length; i++) {
			result = parse(methodArgs[i], args[i]) != null && result;
		}
		return result;
	}

	void next() {
		updateObject(historyGraph.getNext());
	}

	void previous() {
		updateObject(historyGraph.getPrevious());
	}

	void save(String arg) {
		savedObjects.saveObject(arg, object);
	}

	void get(String arg) {
		updateObject(savedObjects.getObject(arg));
		historyGraph.addToHistory(object);
	}

	private void updateObject(Object obj) {
		object = obj;

		InfoPrinter
				.printObjectInfo(obj, getObjectClass(obj).getCanonicalName());

		if (!getObjectClass(obj).isPrimitive() && obj != null) {
			InfoPrinter.printStructureInfo(obj);
		}
	}

	private Class<?> getObjectClass(Object obj) {
		for (Types type : Types.values()) {
			if (obj.getClass() == type.getWrapper())
				return type.getPrimitive();
		}
		return obj.getClass();
	}
}
