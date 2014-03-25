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
	private Object object;

	public Inspector() {
		historyGraph = new HistoryGraph();
		savedObjects = new SavedObjects();
		object = null;

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

	private void inspect(String name, int value) throws SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException, InstantiationException {

		Field field;
		Class<?> actualClass = object.getClass();

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

		Field field = getFieldInAnyClass(name);

		if (field != null) {
			boolean originalAccess = field.isAccessible();
			field.setAccessible(true);

			if (field.getType().isPrimitive()) {
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
		Class<?> actualClass = object.getClass();
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

	private void call(String input[]) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			SecurityException, NoSuchMethodException {

		Object[] methodArgs = new Object[input.length - 2];
		String[] inputArgs = new String[input.length - 2];
		String methodName = input[1];
		Class<?> actualClass = object.getClass();
		Method bestMethod = null;

		System.arraycopy(input, 2, inputArgs, 0, inputArgs.length);

		while (bestMethod == null && actualClass != Object.class) {
			bestMethod = filterMethods(actualClass.getDeclaredMethods(),
					inputArgs, methodName);
			actualClass = actualClass.getSuperclass();
		}

		if (bestMethod == null) {
			InfoPrinter.printNullInfo("call");
			return;
		}

		// ordena os metodos e escolhe o mais compativel

		// fazer a conversao dos argumentos do input conforme
		// os tipos do metodo que escolheu
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

		// descarta todos os metodos que sejam divergentes no
		// numero de argumentos ou nome
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

	private void updateObject(Object obj) {
		if (obj != null) {
			object = obj;
			InfoPrinter.printObjectInfo(obj, obj.getClass().getCanonicalName());
		}
	}

	private void updateObject(Object obj, Class<?> classType) {
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
