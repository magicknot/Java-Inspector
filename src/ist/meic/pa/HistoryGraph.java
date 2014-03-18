package ist.meic.pa;

import java.util.ArrayList;

public class HistoryGraph {

	ArrayList<Object> objects;
	int historyPointer;

	public ArrayList<Object> getObjects() {
		return objects;
	}

	public void addToHistory(ArrayList<Object> objects) {
		this.objects = objects;
	}

	public Object getNext() {
		if (historyPointer < objects.size())
			return objects.get(historyPointer++);
		else
			return null;

	}

	public Object getPrevious() {
		if (historyPointer > 0)
			return objects.get(historyPointer--);
		else
			return null;

	}

}
