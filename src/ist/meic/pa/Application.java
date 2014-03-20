package ist.meic.pa;

import a.B;
import a.C;
import a.D;

public class Application {

	public static void main(String args[]) {

		B b = new B();
		C c = new C();
		D d = new D();

		//new ist.meic.pa.Inspector().inspect(b);

		// para testa uso da superclasse e/ou shadow
		//new ist.meic.pa.Inspector().inspect(c);
		new ist.meic.pa.Inspector().inspect(d);

	}

}
