package ist.meic.pa;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class InfoPrinter {

	public static void printObjectInfo(Object obj, String objectClassName) {
		// System.out.println(classType);

			System.err.println(obj + " is an instance of " + objectClassName);
			System.err.println("----------");
			printStructureInfo(obj);

	}

	private static void printStructureInfo(Object object) {

		try {

			System.err.println("Attributes:");

			printFieldsInfo(object);

			System.err.println("----------");
			printAnnotationsInfo(object.getClass().getAnnotations());
			printConstructorsInfo(object.getClass().getConstructors());
			printInterfacesInfo(object.getClass().getInterfaces());
			printMethodsInfo(object.getClass().getDeclaredMethods());
			printSuperClassesInfo(object);

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void printFieldsInfo(Object object)
			throws IllegalArgumentException, IllegalAccessException,
			SecurityException, NoSuchMethodException, InvocationTargetException {

		Class<?> actualClass = object.getClass();

		while (actualClass != Object.class) {

			for (Field field : actualClass.getDeclaredFields()) {

				// don't print static variables
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				}

				Boolean fieldAccess = field.isAccessible();
				field.setAccessible(true);
				Object fieldObj = field.get(object);
				field.setAccessible(fieldAccess);
				
				
				System.err.print(field.toString());

				if (fieldObj == null){
					System.err.println(";");
					continue;
				}

				if (fieldObj.getClass().isArray()) {

					System.err.print(" = [ ");

					for (int i = 0; i < Array.getLength(fieldObj); i++) {
						System.err.print(Array.get(fieldObj, i) + " ");
					}
					System.err.println("];");

				} else {
					System.err.println(" = " + fieldObj+";");
				}

			}

			actualClass = actualClass.getSuperclass();

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

		if (constructors.length == 0) {
			System.err.print("there are no constructors.");
		}

		for (Constructor<?> constructor : constructors) {
			System.err.print(constructor.toString() + "; ");
		}

		System.err.println();
	}

	private static void printInterfacesInfo(Class<?>[] interfaces) {
		System.err.print("Interfaces: ");

		if (interfaces.length == 0) {
			System.err.print("there are no interfaces.");
		}

		for (Class<?> interf : interfaces) {
			System.err.print(interf.toString() + "; ");
		}

		System.err.println();
	}

	private static void printMethodsInfo(Method[] methods) {
		System.err.print("Methods: ");

		if (methods.length == 0) {
			System.err.print("there are no interfaces.");
		}

		for (Method m : methods) {
			System.err.print(m.toString() + "; ");
		}

		System.err.println();
	}

	private static void printSuperClassesInfo(Object object) {
		Class<?> actualClass = object.getClass().getSuperclass();
		
		System.err.print("Superclasses: ");

		while (actualClass != Object.class) {
			System.err.print(actualClass.getName() + " ");
			actualClass = actualClass.getSuperclass();
		}

		System.err.println(actualClass);
	}

	public static void printNullInfo(String s) {
		System.err.println(s + ": the object invocated does not exist");
	}

}
