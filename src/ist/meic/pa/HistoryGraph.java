package ist.meic.pa;

import java.util.ArrayList;

public class HistoryGraph {

	ArrayList<Object> objects;
	int historyPointer;

	public HistoryGraph() {
		objects = new ArrayList<Object>();
		historyPointer = -1;
	}

	public ArrayList<Object> getObjects() {
		return objects;
	}

	public void addToHistory(Object object) {
		this.objects.add(object);
		historyPointer++;
		System.err.println("ADD " + historyPointer);
	}

	public Object getNext() {
		if (historyPointer < objects.size() - 1) {
			return objects.get(++historyPointer);
		} else {
			return objects.get(historyPointer);
		}
	}

	public Object getPrevious() {
		if (historyPointer > 0) {
			return objects.get(--historyPointer);
		} else {
			return objects.get(historyPointer);
		}
	}
}
