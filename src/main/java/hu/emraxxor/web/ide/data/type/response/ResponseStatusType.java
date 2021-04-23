package hu.emraxxor.web.ide.data.type.response;

/**
 * 
 * @author Attila Barna
 */
public enum ResponseStatusType {
	ERROR(-1,"error"),
	SUCCESS(1,"success");
	
	private final String val;

	private final int code;
	
	ResponseStatusType(int code, String v) {
		this.val = v;
		this.code = code;
	}
	
	@Override
	public String toString() {
		return this.val;
	}
	
	public int getCode() {
		return this.code;
	}
}
