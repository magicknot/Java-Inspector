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

public class Inspector {

	private InfoPrinter infoPrinter;
	private TypeMatcher matcher;
	private HistoryGraph historyGraph;
	private SavedObjects savedObjects;

	public Inspector() {

		infoPrinter = new InfoPrinter();
		matcher = new TypeMatcher();
		historyGraph = new HistoryGraph();
		savedObjects = new SavedObjects();

	}

	public void inspect(Object object) {

		infoPrinter.printInspectionInfo(object);
		historyGraph.addToHistory(object);
		readEvalPrint(object);
	}

	public void readEvalPrint(Object object) {

		BufferedReader buffer = new BufferedReader(new InputStreamReader(
				System.in));
		Object myObject = object;

		while (true) {

			System.out.print("> ");

			try {
				String arguments[] = buffer.readLine().split(" ");

				if (arguments[0].equals("q")) {
					return;
				} else if (arguments[0].equals("i")) {
					System.out.println("I");

					Field field = myObject.getClass().getDeclaredField(
							arguments[1]);

					if (Modifier.isPrivate(field.getModifiers())
							|| Modifier.isProtected(field.getModifiers()))
						field.setAccessible(true);

					myObject = field.get(object);
					historyGraph.addToHistory(myObject);

					if (myObject != null)
						infoPrinter.printInspectionInfo(myObject);

				} else if (arguments[0].equals("m")) {
					System.out.println("M");

					Field field = myObject.getClass().getDeclaredField(
							arguments[1]);

					if (Modifier.isPrivate(field.getModifiers())
							|| Modifier.isProtected(field.getModifiers()))
						field.setAccessible(true);

					String fieldType = field.getType().toString();

					if (fieldType.equals("int"))
						field.set(object, matcher.IntegerMatch(arguments[2]));
					else if (fieldType.equals("float"))
						field.set(object, matcher.FloatMatch(arguments[2]));
					else if (fieldType.equals("double"))
						field.set(object, matcher.DoubleMatch(arguments[2]));
					else if (fieldType.equals("long"))
						field.set(object, matcher.LongMatch(arguments[2]));
					else if (fieldType.equals("byte"))
						field.set(object, matcher.ByteMatch(arguments[2]));
					else if (fieldType.equals("short"))
						field.set(object, matcher.ShortMatch(arguments[2]));
					else if (fieldType.equals("boolean"))
						field.set(object, matcher.BooleanMatch(arguments[2]));
					else
						field.set(object, arguments[2]);

					infoPrinter.printInspectionInfo(myObject);

				} else if (arguments[0].equals("c")) {
					System.out.println("C");

					for (Method method : object.getClass().getMethods()) {
						if (method.getName().equals(arguments[1])) {

							Object result;

							if (arguments.length - 2 == 0) {

								result = method.invoke(myObject, null);
							}

							else {

								int numGets = 0;
								for (int i = 0; i < arguments.length - 2; i++) {
									if (arguments[i + 2].equals("#get")) {
										numGets++;
									}
								}

								Object[] methodArgs;
								if (numGets > 0) {
									methodArgs = new Object[arguments.length
											- numGets - 2];

								} else {
									methodArgs = new Object[arguments.length - 2];
								}

								for (int i = 0; i < arguments.length - 2; i++) {

									if (arguments[i + 2].equals("#get")) {

										methodArgs[i] = savedObjects
												.getObject(arguments[i + 3]);
										// incrementa o i para saltar para a
										// posicao a seguir ao nome da variavel
										i++;

									} else {
										methodArgs[i] = matcher
												.getBestMatch(arguments[i + 2]);
									}
								}

								result = method.invoke(myObject, methodArgs);
							}

							if (result != null) {
								myObject = result;
								historyGraph.addToHistory(myObject);
								infoPrinter.printInspectionInfo(myObject);
							}

							break;
						}

					}

				} else if (arguments[0].equals("n")) {
					System.out.println("N");
					myObject = historyGraph.getNext();
					infoPrinter.printInspectionInfo(myObject);

				} else if (arguments[0].equals("p")) {
					System.out.println("P");
					myObject = historyGraph.getPrevious();
					infoPrinter.printInspectionInfo(myObject);
				} else if (arguments[0].equals("s")) {
					System.out.println("S");
					savedObjects.saveObject(arguments[1], myObject);
				} else if (arguments[0].equals("g")) {
					System.out.println("G");
					myObject = savedObjects.getObject(arguments[1]);
					if (myObject != null)
						infoPrinter.printInspectionInfo(myObject);
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
}
