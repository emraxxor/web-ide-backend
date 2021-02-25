package hu.emraxxor.web.ide.data.type.response;

/**
 * 
 * @author Attila Barna
 * @category infovip.web.validation
 *
 */
public enum ValidationType {

	EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", ResponseStatusType.ERROR),
	
	EMAIL_VALIDATION_ERROR("EMAIL_VALIDATION_ERROR", ResponseStatusType.ERROR),
	
	VALIDATION_SUCCESSFUL("VALIDATION_SUCCESSFUL", ResponseStatusType.SUCCESS),

	EMPTY_FIELD("VALIDATION_EMPTY_FIELD", ResponseStatusType.ERROR),
	
	VALIDATION_NUMERIC_ERROR("VALIDATION_NUMERIC_ERROR", ResponseStatusType.ERROR),
	
	VALIDATION_MAX_LENGTH("VALIDATION_MAX_LENGTH", ResponseStatusType.ERROR),

	VALIDATION_COMPARE_ERROR("VALIDATION_COMPARE_ERROR", ResponseStatusType.ERROR),
	
	VALIDATION_NO_PERMISSION_TO_UPDATE("VALIDATION_NO_PERMISSION_TO_UPDATE", ResponseStatusType.ERROR),
	
	UNEXPECTED_ERROR("UNEXPECTED_ERROR", ResponseStatusType.ERROR),

	INVALID_DATA("INVALID_DATA", ResponseStatusType.ERROR),
	
	DATA_ALREADY_EXISTS("DATA_ALREADY_EXISTS", ResponseStatusType.ERROR),
	
	INVALID_CAPTCHA("INVALID_CAPTCHA", ResponseStatusType.ERROR);
	
	private String value;
	
	private ResponseStatusType code;
	
	private ValidationType(String v,ResponseStatusType code) {
		this.value = v;
		this.code = code;
	}
	
	@Override
	public String toString() {
		return this.value;
	}
	
	public String value() {
		return this.value;
	}
	
	public int code() {
		return code.getCode();
	}

}
