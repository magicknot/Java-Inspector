package ist.meic.pa;

import java.util.ArrayList;

public class HistoryGraph {

	private ArrayList<Object> objects;
	private int historyPointer;

	public HistoryGraph() {
		objects = new ArrayList<Object>();
		historyPointer = -1;
	}

	public ArrayList<Object> getObjects() {
		return objects;
	}

	public void addToHistory(Object object) {
		if (historyPointer == objects.size() -1) {
			objects.add(object);
			historyPointer++;
		}else{
			objects.remove(objects.size()-1);
			addToHistory(object);
		}
	}

	public Object getNext() {
		//System.out.println("pointer " + historyPointer + " objects.size " + objects.size());
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
