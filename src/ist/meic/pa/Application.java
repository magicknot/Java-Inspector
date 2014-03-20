package ist.meic.pa;

import a.B;
import a.C;

public class Application {

	public static void main(String args[]) {

		B b = new B();
		C c = new C();

		//new ist.meic.pa.Inspector().inspect(b);

		// para testa uso da superclasse e/ou shadow
		new ist.meic.pa.Inspector().inspect(c);

	}

}
