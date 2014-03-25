package ist.meic.pa;

import java.util.HashMap;
import java.util.Map;

public class SavedObjects {

	Map<String, Object> objectMap;
	Map<String, Class<?>> classMap;

	public SavedObjects() {
		objectMap = new HashMap<String, Object>();
		classMap = new HashMap<String, Class<?>>();
	}

	public Object getObject(String key) {
		return objectMap.get(key);
	}

	public Class<?> getObjClass(String key) {
		return classMap.get(key);
	}

	public void saveObject(String key, Object obj, Class<?> objectClass) {
		objectMap.put(key, obj);
		classMap.put(key, objectClass);
	}
}
