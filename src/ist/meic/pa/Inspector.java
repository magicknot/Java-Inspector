package ist.meic.pa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/*TODO getdeclaredmethod ou getmethod?
 * Adaptar o codigo para reconhecer objectos do tipo array
 * Adaptar o codigo para imprimir objectos do tipo array
 * Terminar a classe graph para eliminar caminhos antigos
 * Acabar command c, falta o caso em que existem varios
 * matches para o mesmo metodo
 * Verificar a sequencia dos metodos no typematcher
 * Ultimo ponto extra
 * Perceber o porque de os modificadores necessarios nao
 * serem sempre no mesmo numero
 */

public class Inspector {

	private HistoryGraph historyGraph;
	private SavedObjects savedObjects;
	private Object myObject;

	public Inspector() {
		historyGraph = new HistoryGraph();
		savedObjects = new SavedObjects();
		myObject = null;
	}

	public void inspect(Object object) {
		myObject = object;
		InfoPrinter.printInspectionInfo(object);
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
					return;
				} else if (arguments[0].equals("i")) {
					iCommand(arguments[1]);
				} else if (arguments[0].equals("m")) {
					mCommand(arguments[1], arguments[2]);
				} else if (arguments[0].equals("c")) {
					cCommand(arguments);
				} else if (arguments[0].equals("n")) {
					nCommand();
				} else if (arguments[0].equals("p")) {
					pCommand();
				} else if (arguments[0].equals("s")) {
					sCommand(arguments[1]);
				} else if (arguments[0].equals("g")) {
					gCommand(arguments[1]);
				} else if (arguments[0].equals("x")) {
					xCommand(arguments[1], arguments[2]);
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
			}
		}
	}

	public void iCommand(String arg) throws SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException {

		Field field = myObject.getClass().getDeclaredField(arg);

		if (Modifier.isPrivate(field.getModifiers())
				|| Modifier.isProtected(field.getModifiers()))
			field.setAccessible(true);

		myObject = field.get(myObject);
		historyGraph.addToHistory(myObject);
		InfoPrinter.printInspectionInfo(myObject);
	}

	public void xCommand(String arg1, String arg2) throws SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException {

		Class<?> myClass = myObject.getClass();
		Integer numSuper = Integer.parseInt(arg2);

		// percorre as superclasses ate´ chegar `a desejada
		for (int i = 0; i < numSuper && !myClass.isInstance(Object.class); i++) {
			myClass = myClass.getSuperclass();
		}

		Field field = myClass.getDeclaredField(arg1);

		if (Modifier.isPrivate(field.getModifiers())
				|| Modifier.isProtected(field.getModifiers()))
			field.setAccessible(true);

		Object o;
		try {
			o = (Object) myClass.newInstance();
			myObject = field.get(o);
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		historyGraph.addToHistory(myObject);
		InfoPrinter.printInspectionInfo(myObject);
	}

	public void mCommand(String arg1, String arg2)
			throws IllegalArgumentException, IllegalAccessException,
			SecurityException, NoSuchFieldException {

		Field field = myObject.getClass().getDeclaredField(arg1);

		if (Modifier.isPrivate(field.getModifiers())
				|| Modifier.isProtected(field.getModifiers()))
			field.setAccessible(true);

		String fieldType = field.getType().toString();

		if (fieldType.equals("int"))
			field.set(myObject, TypeMatcher.IntegerMatch(arg2));
		else if (fieldType.equals("float"))
			field.set(myObject, TypeMatcher.FloatMatch(arg2));
		else if (fieldType.equals("double"))
			field.set(myObject, TypeMatcher.DoubleMatch(arg2));
		else if (fieldType.equals("long"))
			field.set(myObject, TypeMatcher.LongMatch(arg2));
		else if (fieldType.equals("byte"))
			field.set(myObject, TypeMatcher.ByteMatch(arg2));
		else if (fieldType.equals("short"))
			field.set(myObject, TypeMatcher.ShortMatch(arg2));
		else if (fieldType.equals("boolean"))
			field.set(myObject, TypeMatcher.BooleanMatch(arg2));
		else
			field.set(myObject, arg2);

		InfoPrinter.printInspectionInfo(myObject);
	}

	public void cCommand(String args[]) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {

		Object[] methodArgs = new Object[args.length - 2];
		Class<?> myClass;

		if (myObject != null) {

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
			// se ha´ algum metodo com o mesmo nome
			myClass = myObject.getClass();

			while (!myClass.isInstance(Object.class)) {

				for (Method m : myClass.getMethods()) {
					if (m.getName().equals(args[1])
							&& hasCompatibleArgs(m, methodArgs)) {
						myObject = m.invoke(myObject, methodArgs);
						historyGraph.addToHistory(myObject);
						InfoPrinter.printInspectionInfo(myObject);
						return;
					}
				}
				myClass = myClass.getSuperclass();
			}
		} else {
			// TODO Confirmar se nao sera melhor chamar algum metodo da classe
			// InfoPrinter
			System.err.println("the object is null");
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

	public void nCommand() {
		myObject = historyGraph.getNext();
		InfoPrinter.printInspectionInfo(myObject);
	}

	public void pCommand() {
		myObject = historyGraph.getPrevious();
		InfoPrinter.printInspectionInfo(myObject);
	}

	public void sCommand(String arg) {
		savedObjects.saveObject(arg, myObject);
	}

	public void gCommand(String arg) {
		myObject = savedObjects.getObject(arg);
		if (myObject != null) {
			InfoPrinter.printInspectionInfo(myObject);
		}
	}

}
