package ist.meic.pa;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class InfoPrinter {
	
	
	public void printInspectionInfo(Object object) {

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
