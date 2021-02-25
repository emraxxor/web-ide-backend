package hu.emraxxor.web.ide.data.type.response;

/**
 * 
 * @author Attila Barna
 * @category infovip.web.response
 */
public enum ResponseStatusType {
	ERROR(-1,"error"),
	SUCCESS(1,"success");
	
	private String val;

	private int code;
	
	private ResponseStatusType(int code, String v) {
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
