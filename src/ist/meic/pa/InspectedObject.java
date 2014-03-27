package ist.meic.pa;

public class InspectedObject {

	private Object object;
	private Class<?> objectClass;

	public InspectedObject(Object object, Class<?> objectClass) {
		this.object = object;
		this.objectClass = objectClass;
	}

	public InspectedObject(Object object) {
		this.object = object;
		this.objectClass = object.getClass();
	}

	public Object getObject() {
		return object;
	}

	public void setObjectClass(Class<?> objectClass) {
		this.objectClass = objectClass;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public Class<?> getObjectClass() {
		return objectClass;
	}

	public boolean isNull() {
		return object == null;
	}

	public boolean isPrimitive() {
		return objectClass.isPrimitive();
	}

	public String getName() {
		if (object != null)
			return objectClass.getCanonicalName();
		else
			return null;
	}

}
