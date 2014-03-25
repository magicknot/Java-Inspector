package ist.meic.pa;

import java.lang.reflect.InvocationTargetException;

public class Command {

	private Inspector inspector;

	public Command(Inspector inspector) {
		this.inspector = inspector;
	}

	public void i(String input[]) throws NumberFormatException,
			SecurityException, IllegalArgumentException, NoSuchFieldException,
			IllegalAccessException, InstantiationException {
		if (input.length < 3) {
			inspector.inspect(input[1], 0);
		} else {
			inspector.inspect(input[1], Integer.parseInt(input[2]));
		}
	}

	public void m(String input[]) throws IllegalArgumentException,
			SecurityException, IllegalAccessException, NoSuchFieldException,
			InvocationTargetException, InstantiationException,
			NoSuchMethodException {
		inspector.modify(input[1], input[2]);
	}

	public void c(String input[]) throws IllegalArgumentException,
			SecurityException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		String[] inputArgs = new String[input.length - 2];
		System.arraycopy(input, 2, inputArgs, 0, inputArgs.length);
		inspector.call(inputArgs, input[1]);
	}

	public void n(String input[]) {
		inspector.next();
	}

	public void p(String input[]) {
		inspector.previous();
	}

	public void s(String input[]) {
		inspector.save(input[1]);
	}

	public void g(String input[]) {
		inspector.get(input[1]);
	}

}
