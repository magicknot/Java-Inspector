package ist.meic.pa;

public class TypeMatcher {
	
	private Object IntegerMatch(String arg) {

		try {
			return Integer.parseInt(arg);
		} catch (NumberFormatException e) {
			return null;
		}

	}
	
	private Object FloatMatch(String arg){
		
		try {
			return Float.parseFloat(arg);
		} catch (NumberFormatException e) {
			return null;
		}
		
	}
	
	private Object DoubleMatch(String arg){

		try {
			return Double.parseDouble(arg);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	private Object LongMatch(String arg){
		try {
			return Long.parseLong(arg);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	private Object ByteMatch(String arg){
		try {
			return Byte.parseByte(arg);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	private Object ShortMatch(String arg){
		try {
			return Short.parseShort(arg);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	private Object Boolean(String arg){
		try {
			return Boolean.parseBoolean(arg);
		} catch (NumberFormatException e) {
			return null;
		}
		
	}

}
