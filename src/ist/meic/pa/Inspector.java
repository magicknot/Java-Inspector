package ist.meic.pa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class Inspector.
 */
public class Inspector {

	/** The timeline of used objects. */
	private HistoryGraph historyGraph;

	/** The objects saved with the method {@link #save(String)}. */
	private Map<String, Object> savedObjects;

	/** The object which is currently in use. */
	private Object object;

	/**
	 * Instantiates a new inspector and initializes the {@link Types}.
	 */
	public Inspector() {
		this.historyGraph = new HistoryGraph();
		this.savedObjects = new HashMap<String, Object>();
		this.object = null;

		for (Types type : Types.values()) {
			Types.init(type.getWrapper(), type.getPrimitive());
		}
	}

	/**
	 * Sets the object.
	 * 
	 * @param object
	 *            the new object to be set.
	 */
	private void setObject(Object object) {
		if (object != null) {
			System.out.println(object.getClass().isPrimitive());
			this.object = object;
			InfoPrinter.printObjectInfo(object, object.getClass()
					.getCanonicalName());
		}
	}

	/**
	 * Sets the object. It checks whether the class type is a Java primitive or
	 * not in order to print the proper information.
	 * 
	 * @param object
	 *            the object to be set
	 * @param classType
	 *            the class type of the object
	 */
	private void setObject(Object object, Class<?> classType) {
		if (object != null) {
			this.object = object;
			if (classType.isPrimitive()) {
				InfoPrinter.printObjectInfo(object, classType.toString());
			} else {
				InfoPrinter.printObjectInfo(object, object.getClass()
						.getCanonicalName());
			}
		}
	}

	/**
	 * Gets the named object.
	 * 
	 * @param name
	 *            the name of the object to be retrieved.
	 */
	private void get(String name) {
		setObject(savedObjects.get(name));
		historyGraph.addToHistory(object);
	}

	/**
	 * Inspects a given {@code object}. It is printed its information and the
	 * {@code object} is added to the {@link #historyGraph}. Finally, it is
	 * started a REPL.
	 * 
	 * @param object
	 *            the object to be inspected
	 */
	public void inspect(Object object) {
		setObject(object);
		historyGraph.addToHistory(object);
		readEvalPrint();
	}

	/**
	 * The REPL. It supports various commands: <br>
	 * <ul>
	 * <li><b>{@code i name <value>}</b> - inspects the value of the field named
	 * <i>name</i>. The <i>value</i> represents, if present, the hierarchical
	 * position of the superclass where the field is located. If the
	 * <i>value</i> is not present or if it's 0, then the field is inspected in
	 * the current object. See {@link #inspectField(String, int)}.
	 * <li><b>{@code m name value}</b> - modifies the value of the field named
	 * <i>name</i> of the current object so that if becomes <i>value</i>. See
	 * {@link #modify(String, String)}.
	 * <li><b>c name value0 value1 ... valueN </b> - calls the method named
	 * <i>name</i> using the current object as a receiver and the provided
	 * values as arguments and inspects the returned value, if there is one. See
	 * {@link #call(String[])}.
	 * <li><b>n</b> - gets the next object in the {@link #historyGraph}. See
	 * {@link #next()}.
	 * <li><b>p</b> - gets the previous object in the {@link #historyGraph}. See
	 * {@link #previous()}.
	 * <li><b>s name</b> - saves the current into the {@link #savedObjects}
	 * object with the name <i>name</i>. See {@link #save(String)}.
	 * <li><b>g name</b> - saves the object named <i>name</i> from
	 * {@link #savedObjects}. See {@link #get(String)}.
	 * <li><b>q: terminate the execution.</b>
	 * </ul>
	 */
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
						inspectField(arguments[1], 0);
					} else {
						inspectField(arguments[1],
								Integer.parseInt(arguments[2]));
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

	/**
	 * Inspects the field named <i>name</i>. This field is looked up according
	 * to the <i>value</i>. If this parameter is 0 then it's looked up in the
	 * current object, otherwise it's looked up in the respective superclass.
	 * 
	 * @param name
	 *            the name of the field to be inspected
	 * @param value
	 *            the number of times you go to the parent level in the class
	 *            hierarchy.
	 */
	private void inspectField(String name, int value) throws SecurityException,
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
			setObject(fieldObj, field.getType());
			historyGraph.addToHistory(fieldObj);
		} else {
			InfoPrinter.printNullInfo("inspect");
		}
	}

	/**
	 * Modifies the value of the field named <i>name</i> so that its value
	 * becomes <i>value</i>. It tries to obtain the field by its name (see
	 * {@link #getFieldByName(String)}). If this operation is successful then
	 * the new value is set.
	 * 
	 * @param name
	 *            the name of the field
	 * @param value
	 *            the new value
	 */
	private void modify(String name, String value)
			throws IllegalArgumentException, IllegalAccessException,
			SecurityException, NoSuchFieldException, InvocationTargetException,
			InstantiationException, NoSuchMethodException {

		Field field = getFieldByName(name);

		if (field != null) {
			boolean originalAccess = field.isAccessible();
			field.setAccessible(true);

			if (field.getType().isPrimitive()) {
				field.set(object, parse(field.getType(), value));
			} else {
				field.set(object, value);
			}
			field.setAccessible(originalAccess);
			setObject(object);
		} else {
			InfoPrinter.printNullInfo("modify");
		}
	}

	/**
	 * Gets the field by its <i>name</i>. The field is searched in the current
	 * class or upwards in the class hierarchy, until the class
	 * <code>Object</code>, until the field is found.
	 * 
	 * @param name
	 *            the name of the field to be retrieved
	 * @return the field named <i>name</i> or <code>null</code> if it doesn't
	 *         exists.
	 */
	private Field getFieldByName(String name) {
		Class<?> actualClass = object.getClass();

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

	/**
	 * Calls the method named <i>name</i> in the current object passing as
	 * arguments the possible <i>values</i>. The input is splitted according to
	 * this so that the best method match can be found. To select the best match
	 * all the methods from the current object are tested using
	 * {@link #filterMethods(Method[], String[], String)}. If none matches, the
	 * test is conducted upwards in the class hierarchy until reaching the class
	 * <code>Object</code>. If there is a match, the <i>values</i> are converted
	 * to the arguments' types and then the call is executed.
	 * 
	 * @param input
	 *            the name of the method and the arguments
	 */
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
		setObject(bestMethod.invoke(object, methodArgs));
		historyGraph.addToHistory(object);
	}

	/**
	 * Filter methods by rejecting all of those who have a different number of
	 * arguments and/or a different name. The methods must also receive the same
	 * type of arguments (see {@link #isCompatible(String[], Class[])}). In the
	 * end, is returned the method with the best classification ( see
	 * {@link #classifyMethod(Method)}).
	 * 
	 * @param methods
	 *            the methods to be tested
	 * @param arguments
	 *            the arguments that the method must receive
	 * @param name
	 *            the name of the method
	 * @return the method best classified
	 */
	private Method filterMethods(Method[] methods, String[] arguments,
			String name) {
		int minVal = 0;
		Method bestMethod = null;
		int tempVal = 0;

		// descarta todos os metodos que sejam divergentes no
		// numero de argumentos ou nome
		for (Method m : methods) {
			if (m.getName().equals(name)
					&& m.getParameterTypes().length == arguments.length
					&& isCompatible(arguments, m.getParameterTypes())) {

				tempVal = classifyMethod(m);

				if (tempVal < minVal || minVal == 0) {
					minVal = tempVal;
					bestMethod = m;
				}
			}
		}
		return bestMethod;
	}

	/**
	 * Checks if is compatible. //TODO
	 * 
	 * @param arguments
	 *            the list of argument
	 * @param methodArgs
	 *            the method arguments to be tested
	 * @return true, if is compatible
	 */
	private boolean isCompatible(String arguments[], Class<?> methodArgs[]) {

		boolean result = true;

		for (int i = 0; i < arguments.length; i++) {
			result = parse(methodArgs[i], arguments[i]) != null && result;
		}
		return result;
	}

	/**
	 * The method is classified according to its arguments priority value. To
	 * calculate this value it is used {@link Types#getPriorityValue(Class)}.
	 * 
	 * @param method
	 *            the method to be classified
	 * @return the classification
	 */
	private int classifyMethod(Method method) {
		int value = 0;
		int multiple = 1;

		for (Class<?> c : method.getParameterTypes()) {
			value = value + multiple * Types.getPriorityValue(c);
			multiple = multiple * 10;
		}
		return value;
	}

	// TODO JAVADOC
	private Object parseObjectType(String arg) {
		Object obj;

		for (Types type : Types.values()) {
			obj = parse(type.getPrimitive(), arg);
			if (obj != null)
				return obj;
		}
		return null;
	}

	// TODO JAVADOC
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

	/**
	 * Calls the {@link #setObject(Object)} with the next one in the
	 * {@link #historyGraph}.
	 */
	private void next() {
		setObject(historyGraph.getNext());
	}

	/**
	 * Calls the {@link #setObject(Object)} with the previous one in the
	 * {@link #historyGraph}.
	 */
	private void previous() {
		setObject(historyGraph.getPrevious());
	}

	/**
	 * Saves the current object with the name <i>name</i>.
	 * 
	 * @param name
	 *            the name that will be associated to the saved object
	 */
	private void save(String name) {
		savedObjects.put(name, object);
	}
}
