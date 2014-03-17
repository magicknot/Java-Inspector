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

public class Inspector {

	public Inspector() {

	}

	public void inspect(Object object) {

		printInspectionInfo(object);

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

					Field field = myObject.getClass().getDeclaredField(
							arguments[1]);

					// for (Field f : myObject.getClass().getDeclaredFields()) {

					if (Modifier.isPrivate(field.getModifiers())
							|| Modifier.isProtected(field.getModifiers()))
						field.setAccessible(true);

					myObject = field.get(object);

					if (myObject != null)
						printInspectionInfo(myObject);

				} else if (arguments[0].equals("m")) {

					Field field = myObject.getClass().getDeclaredField(
							arguments[1]);

					if (Modifier.isPrivate(field.getModifiers())
							|| Modifier.isProtected(field.getModifiers()))
						field.setAccessible(true);

					System.out.println(field.getType());
					
					if (field.getType().equals("int"))
						field.set(object, Integer.parseInt(arguments[2]));
					else if (field.getType().equals("float"))
						field.set(object, Float.parseFloat(arguments[2]));
					else if (field.getType().equals("double"))
						field.set(object, Double.parseDouble(arguments[2]));
					else if (field.getType().equals("long"))
						field.set(object, Long.parseLong(arguments[2]));
					else if (field.getType().equals("byte"))
						field.set(object, Byte.parseByte(arguments[2]));
					else if (field.getType().equals("short"))
						field.set(object, Short.parseShort(arguments[2]));
					else if (field.getType().equals("boolean"))
						field.set(object, Boolean.parseBoolean(arguments[2]));
					else
						field.set(object, arguments[2]);

					printInspectionInfo(myObject);

				} else if (arguments[0].equals("c")) {

					for (Method method : object.getClass().getMethods()) {
						if (method.getName().equals(arguments[1])) {

							Object result;

							if (arguments.length - 2 == 0)
								result = method.invoke(object, null);

							else {
								Object[] methodArgs = new Object[arguments.length - 2];
								for (int i = 0; i < arguments.length; i++)
									methodArgs[i] = arguments[i + 2];

								result = method.invoke(object, methodArgs);

							}

							if (result != null)
								myObject = result;

							break;
						}

					}

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

	private void printInspectionInfo(Object object) {

		System.out.println(object + " is an instance of " + object.getClass());
		System.out.println("----------");

		printFieldsInfo(object.getClass().getDeclaredFields(), object);
		System.out.println("----------");

		printAnnotationsInfo(object.getClass().getAnnotations());
		printConstructorsInfo(object.getClass().getConstructors());
		printInterfacesInfo(object.getClass().getInterfaces());
		printMethodsInfo(object.getClass().getDeclaredMethods());
		printSuperclassesInfo(object);

	}

	private void printFieldsInfo(Field[] fields, Object object) {

		for (Field field : fields) {
			if (Modifier.isPrivate(field.getModifiers())
					|| Modifier.isProtected(field.getModifiers())
					|| Modifier.isStatic(field.getModifiers()))
				field.setAccessible(true);
			try {
				System.out
						.println(field.toString() + " = " + field.get(object));
			} catch (IllegalArgumentException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}

	}

	private void printAnnotationsInfo(Annotation[] annotations) {

		System.out.print("Annotations: ");

		for (Annotation anot : annotations)
			System.out.print(anot.toString() + "; ");

		System.out.println();

	}

	private void printConstructorsInfo(Constructor<?>[] constructors) {

		System.out.print("Constructors: ");

		for (Constructor<?> constructor : constructors)
			System.out.print(constructor.toString() + "; ");

		System.out.println();

	}

	private void printInterfacesInfo(Class<?>[] interfaces) {

		System.out.print("Interfaces: ");

		for (Class<?> interf : interfaces)
			System.out.print(interf.toString() + "; ");

		System.out.println();
	}

	private void printMethodsInfo(Method[] methods) {

		System.out.print("Methods: ");

		for (Method m : methods)
			System.out.print(m.toString() + "; ");

		System.out.println();

	}

	private void printSuperclassesInfo(Object object) {
		if (object.getClass().getSuperclass() != null)
			System.out.println("Superclasse: "
					+ object.getClass().getSuperclass().getName());
	}

}
