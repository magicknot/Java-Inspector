package ist.meic.pa;

import java.util.ArrayList;

public class HistoryGraph {

	private ArrayList<Object> objects;
	private ArrayList<Class<?>> classes;
	private int historyPointer;

	public HistoryGraph() {
		objects = new ArrayList<Object>();
		classes = new ArrayList<Class<?>>();
		historyPointer = -1;
	}

	public void addToHistory(Object object, Class<?> objClass) {
		if (historyPointer == objects.size() - 1) {
			objects.add(object);
			classes.add(objClass);
			historyPointer++;
		} else {
			objects.remove(objects.size() - 1);
			addToHistory(object, objClass);
		}
	}

	public void setNext() {
		if (historyPointer < objects.size() - 1) {
			++historyPointer;
		}
	}

	public void setPrevious() {
		if (historyPointer > 0) {
			--historyPointer;
		}
	}

	public Class<?> getActualClass() {
		return classes.get(historyPointer);
	}

	public Object getActualObject() {
		return objects.get(historyPointer);
	}

}
