package ist.meic.pa;

/**
 * This class wrappers the object which is being inspected, together with its
 * class
 */

public class InspectedObject {

	/**
	 * The object itself
	 */
	private Object object;
	/**
	 * The class of the object
	 */
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

	/**
	 * Checks if the object is null
	 * 
	 * @return true or false depending if it is null or not
	 */
	public boolean isNull() {
		return object == null;
	}

	/**
	 * Checks if the object class corresponds to a primitive type
	 * 
	 * @return
	 */

	public boolean isPrimitive() {
		return objectClass.isPrimitive();
	}

	/**
	 * Returns the name of the class of the object
	 * 
	 * @return - the name
	 */
	public String getName() {
		if (object != null)
			return objectClass.getCanonicalName();
		else
			return null;
	}

}
