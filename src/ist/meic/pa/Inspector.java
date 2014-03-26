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
 * This inspector can be started from any point of a Java program. To do so all
 * that have to be done is call the method {@link #inspect(Object)} passing the
 * {@code Object} you want to inspect. After that it is possible to inspect and
 * modify the object fields, call methods, save inspected objects for further
 * calls and navigate through the history of inspected objects.
 */
public class Inspector {
	/** The timeline of used objects. */
	private HistoryGraph historyGraph;

	/** The objects saved with the method {@link #save(String)}. */
	private Map<String, Object> savedObjects;

	/** The object which is currently in use. */
	private Object object;

	/** The input reader */
	private BufferedReader buffer;

	/**
	 * Instantiates a new inspector and initializes the {@link Types}.
	 */
	public Inspector() {
		this.historyGraph = new HistoryGraph();
		this.savedObjects = new HashMap<String, Object>();
		this.object = null;
		this.buffer = null;
	}

	/**
	 * Gets the object class.
	 * 
	 * @param object
	 *            the object of the class to be retrieved.
	 */
	private Class<?> getObjectClass(Object object) {
		for (Types type : Types.values()) {
			if (object.getClass() == type.getWrapper())
				return type.getPrimitive();
		}
		return object.getClass();
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
		for (Types type : Types.values()) {
			Types.init(type.getWrapper(), type.getPrimitive());
		}

		updateObject(object);
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
	private void readEvalPrint() {
		buffer = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			System.err.print("> ");
			try {
				String arguments[] = buffer.readLine().split("(\\s+\")|(\"\\s+)|\"");

				if (arguments[0].equals("q")) {
					buffer.close();
					return;
				} else {
					this.getClass()
							.getDeclaredMethod(arguments[0], String[].class)
							.invoke(this, new Object[] { arguments });
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
	@SuppressWarnings("unused")
	private void i(String input[]) throws SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException, InstantiationException {
		Field field;
		Class<?> actualClass = getObjectClass(object);
		String name = input[1];

		if (object == null || getObjectClass(object).isPrimitive())
			return;

		if (input.length == 3) {
			int level = Integer.parseInt(input[2]);
			for (int i = 0; i < level; i++) {
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
	@SuppressWarnings("unused")
	private void m(String input[]) throws IllegalArgumentException,
			IllegalAccessException, SecurityException, NoSuchFieldException,
			InvocationTargetException, InstantiationException,
			NoSuchMethodException {
		String name = input[1];
		String value = input[2];

		if (object == null || getObjectClass(object).isPrimitive())
			return;

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
	@SuppressWarnings("unused")
	private void c(String input[]) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			SecurityException, NoSuchMethodException {

		String name = input[1];
		String[] inputArgs = new String[input.length - 2];
		Object[] methodArgs = new Object[inputArgs.length];
		Method bestMethod = null;
		System.arraycopy(input, 2, inputArgs, 0, inputArgs.length);

		if (object == null || getObjectClass(object).isPrimitive())
			return;

		Class<?> actualClass = getObjectClass(object);
		while (bestMethod == null && actualClass != Object.class) {
			bestMethod = filterMethods(actualClass.getDeclaredMethods(),
					inputArgs, name);
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

	/**
	 * Tries to {@link #parse(Class, String) parse} the <i>argument</i> with
	 * each primitive type.
	 * 
	 * @param argument
	 *            the value to instantiate the class
	 * @return the object or null if the parse fails
	 */
	private Object parseObjectType(String argument) {
		Object obj;

		for (Types type : Types.values()) {
			obj = parse(type.getPrimitive(), argument);
			if (obj != null)
				return obj;
		}
		return null;
	}

	/**
	 * Obtains the object that is a instance of the class <i>type</i> with the
	 * value <i>argument</i>. This is done invoking
	 * {@link Types#parseArg(Class, String, Map)}.
	 * 
	 * @param type
	 *            the type of the class
	 * @param argument
	 *            the value to instantiate the class
	 * @return the object or null if the parse fails
	 */
	private Object parse(Class<?> type, String argument) {
		try {
			return Types.parseArg(type, argument, savedObjects);
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
	@SuppressWarnings("unused")
	private void n(String input[]) {
		updateObject(historyGraph.getNext());
	}

	/**
	 * Calls the {@link #setObject(Object)} with the previous one in the
	 * {@link #historyGraph}.
	 */
	@SuppressWarnings("unused")
	private void p(String input[]) {
		updateObject(historyGraph.getPrevious());
	}

	/**
	 * Saves the current object with the name <i>name</i>.
	 * 
	 * @param name
	 *            the name that will be associated to the saved object
	 */
	@SuppressWarnings("unused")
	private void s(String input[]) {
		String name = input[1];
		savedObjects.put(name, object);
	}

	/**
	 * Retrieves the current {@link #object}.
	 * 
	 * @param name
	 *            the name that will be associated to the saved object
	 */
	@SuppressWarnings("unused")
	private void g(String input[]) {
		String name = input[1];
		updateObject(savedObjects.get(name));
		historyGraph.addToHistory(object);
	}

	/**
	 * Updates the {@link #object}. It checks whether the class type is a Java
	 * primitive or not in order to print the proper information.
	 * 
	 * @param object
	 *            the object to be set
	 */
	private void updateObject(Object object) {
		this.object = object;

		if (object == null) {
			InfoPrinter.printObjectInfo(object, null);
			return;
		}

		InfoPrinter.printObjectInfo(object, getObjectClass(object)
				.getCanonicalName());

		if (!getObjectClass(object).isPrimitive()) {
			InfoPrinter.printStructureInfo(object);
		}
	}
}
