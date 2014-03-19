package ist.meic.pa;

import java.util.HashMap;
import java.util.Map;

public class SavedObjects {

	Map<String, Object> map;

	public SavedObjects() {
		map = new HashMap<String, Object>();
	}

	public Object getObject(String key) {
		return map.get(key);
	}

	public void saveObject(String key, Object obj) {
		map.put(key, obj);
	}
}
