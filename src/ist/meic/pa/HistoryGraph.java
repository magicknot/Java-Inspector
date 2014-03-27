package ist.meic.pa;

import java.util.ArrayList;

public class HistoryGraph {

	private ArrayList<InspectedObject> objects;
	private int historyPointer;

	public HistoryGraph() {
		objects = new ArrayList<InspectedObject>();
		historyPointer = -1;
	}

	public ArrayList<InspectedObject> getObjects() {
		return objects;
	}

	public void addToHistory(InspectedObject object) {
		if (historyPointer == objects.size() -1) {
			objects.add(object);
			historyPointer++;
		}else{
			objects.remove(objects.size()-1);
			addToHistory(object);
		}
	}

	public InspectedObject getNext() {
		if (historyPointer < objects.size() - 1) {
			return objects.get(++historyPointer);
		} else {
			return objects.get(historyPointer);
		}
	}

	public InspectedObject getPrevious() {
		if (historyPointer > 0) {
			return objects.get(--historyPointer);
		} else {
			return objects.get(historyPointer);
		}
	}
}
