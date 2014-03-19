package ist.meic.pa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.NumberFormat;
import java.util.ArrayList;

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

	private TypeMatcher matcher;
	private HistoryGraph historyGraph;
	private SavedObjects savedObjects;
	private Object object;

	public Inspector() {
		matcher = new TypeMatcher();
		historyGraph = new HistoryGraph();
		savedObjects = new SavedObjects();
		object = null;
	}

	public void inspect(Object object) {
		this.object = object;
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
					buffer.close();
					return;
				} else if (arguments[0].equals("i")) {
					inspect(arguments[1]);
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
					// FIXME
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

	private void inspect(String arg) throws SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException {

		Field field = object.getClass().getDeclaredField(arg);

		if (Modifier.isPrivate(field.getModifiers())
				|| Modifier.isProtected(field.getModifiers())) {
			field.setAccessible(true);
		}

		object = field.get(object);
		historyGraph.addToHistory(object);
		InfoPrinter.printInspectionInfo(object);
	}

	private void modify(String name, String value)
			throws IllegalArgumentException, IllegalAccessException,
			SecurityException, NoSuchFieldException {

		Field field = object.getClass().getDeclaredField(name);

		if (Modifier.isPrivate(field.getModifiers())
				|| Modifier.isProtected(field.getModifiers())) {
			field.setAccessible(true);
		}

		String type = field.getType().toString();

		if (type.equals("int")) {
			field.set(object, matcher.IntegerMatch(value));
		} else if (type.equals("float")) {
			field.set(object, matcher.FloatMatch(value));
		} else if (type.equals("double")) {
			field.set(object, matcher.DoubleMatch(value));
		} else if (type.equals("long")) {
			field.set(object, matcher.LongMatch(value));
		} else if (type.equals("byte")) {
			field.set(object, matcher.ByteMatch(value));
		} else if (type.equals("short")) {
			field.set(object, matcher.ShortMatch(value));
		} else if (type.equals("boolean")) {
			field.set(object, matcher.BooleanMatch(value));
		} else {
			field.set(object, value);
		}

		InfoPrinter.printInspectionInfo(object);
	}

	private void call(String args[]) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {

		ArrayList<Method> methods = new ArrayList<Method>();
		Method selectedMethod = null;
		Object result;

		for (Method m : object.getClass().getMethods()) {
			if (m.getName().equals(args[1])) {
				methods.add(m);
			}

			if (methods.size() > 1) {
				// to be continued...
				// pensar se n�o vale a pena ter s� uma vari�vel metodo
				// comparar e perceber se vale a pena substituir
			} else {
				selectedMethod = methods.get(0);
			}
			
			if (args.length - 2 == 0) {
				result = selectedMethod.invoke(object, null);
			} else {
				Object[] methodArgs = new Object[args.length - 2];

				for (int i = 0; i < args.length - 2; i++) {
					if (args[i + 2].startsWith("#")) {
						methodArgs[i] = savedObjects.getObject(args[i + 2]
								.substring(1));
					} else {
						methodArgs[i] = matcher.getBestMatch(args[i + 2]);
					}
				}

				result = selectedMethod.invoke(object, methodArgs);
			}

			object = result;
			historyGraph.addToHistory(object);
			InfoPrinter.printInspectionInfo(object);
		}
	}

	private void next() {
		object = historyGraph.getNext();
		InfoPrinter.printInspectionInfo(object);
	}

	private void previous() {
		object = historyGraph.getPrevious();
		InfoPrinter.printInspectionInfo(object);
	}

	private void save(String arg) {
		savedObjects.saveObject(arg, object);
	}

	private void get(String arg) {
		object = savedObjects.getObject(arg);
		InfoPrinter.printInspectionInfo(object);
	}
}
