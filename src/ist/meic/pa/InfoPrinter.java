package ist.meic.pa;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class InfoPrinter {

	public static void printInspectionInfo(Object object) {
		if (object == null) {
			return;
		}

		System.err.println(object + " is an instance of " + object.getClass());
		System.err.println("----------");

		printFieldsInfo(object.getClass().getDeclaredFields(), object);
		System.err.println("----------");

		printAnnotationsInfo(object.getClass().getAnnotations());
		printConstructorsInfo(object.getClass().getConstructors());
		printInterfacesInfo(object.getClass().getInterfaces());
		printMethodsInfo(object.getClass().getDeclaredMethods());
		printSuperClassesInfo(object);
	}

	private static void printFieldsInfo(Field[] fields, Object object) {
		for (Field field : fields) {
			if (Modifier.isPrivate(field.getModifiers())
					|| Modifier.isProtected(field.getModifiers())
					|| Modifier.isStatic(field.getModifiers()))
				field.setAccessible(true);
			try {
				Object fieldObj = field.get(object);

				if (fieldObj != null && fieldObj.getClass().isArray()) {

					System.err.print(field.toString() + " = [ ");

					for (int i = 0; i < Array.getLength(fieldObj); i++) {
						System.err.print(Array.get(fieldObj, i) + " ");
					}
					System.err.print("]");
					// print a new line
					System.err.println("");

				} else {
					System.err.println(field.toString() + " = "
							+ field.get(object));
				}
			} catch (IllegalArgumentException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private static void printAnnotationsInfo(Annotation[] annotations) {
		System.err.print("Annotations: ");

		if (annotations.length < 1) {
			System.err.print("there are no annotations.");
		}

		for (Annotation annotation : annotations) {
			System.err.print(annotation.toString() + "; ");
		}

		System.err.println();
	}

	private static void printConstructorsInfo(Constructor<?>[] constructors) {
		System.err.print("Constructors: ");

		for (Constructor<?> constructor : constructors) {
			System.err.print(constructor.toString() + "; ");
		}

		System.err.println();
	}

	private static void printInterfacesInfo(Class<?>[] interfaces) {
		System.err.print("Interfaces: ");

		if (interfaces.length < 1) {
			System.err.print("there are no interfaces.");
		}

		for (Class<?> interf : interfaces) {
			System.err.print(interf.toString() + "; ");
		}

		System.err.println();
	}

	private static void printMethodsInfo(Method[] methods) {
		System.err.print("Methods: ");

		for (Method m : methods) {
			System.err.print(m.toString() + "; ");
		}

		System.err.println();
	}

	private static void printSuperClassesInfo(Object object) {
		if (object.getClass().getSuperclass() != null) {
			System.err.println("Superclasse: "
					+ object.getClass().getSuperclass().getName());
		}
	}
}
