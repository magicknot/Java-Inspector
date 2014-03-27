package ist.meic.pa;

import java.util.ArrayList;

/**
 * This class saves the sequence of objects inspected. It allows the user to go
 * back and forward in the sequence acessing the different objects. When a new
 * object to be registered depends on one, which is not at the last position of
 * the list, all objects between the new object and the one it depends on, are
 * removed
 */

public class HistoryGraph {

	/**
	 * The list of objects registed along time
	 */
	private ArrayList<InspectedObject> objects;
	/**
	 * The position of the current object on the list
	 */
	private int historyPointer;

	public HistoryGraph() {
		objects = new ArrayList<InspectedObject>();
		historyPointer = -1;
	}

	/**
	 * Registers a new object on the sequence
	 * 
	 * @param object
	 *            -object to register
	 */
	public void addToHistory(InspectedObject object) {
		if (historyPointer == objects.size() - 1) {
			objects.add(object);
			historyPointer++;
		} else {
			objects.remove(objects.size() - 1);
			addToHistory(object);
		}
	}

	/**
	 * Returns the object on the position next to the one pointer is pointing at
	 * 
	 * @return - the object
	 */
	public InspectedObject getNext() {
		if (historyPointer < objects.size() - 1) {
			return objects.get(++historyPointer);
		} else {
			return objects.get(historyPointer);
		}
	}

	/**
	 * Returns the object on the position before the one pointer is pointing at
	 * 
	 * @return - the object
	 */
	public InspectedObject getPrevious() {
		if (historyPointer > 0) {
			return objects.get(--historyPointer);
		} else {
			return objects.get(historyPointer);
		}
	}
}
