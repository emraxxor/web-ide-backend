package hu.emraxxor.web.ide.data.type.response;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @author Attila Barna
 * @category infovip.web.response
 */
public class ValidationResponse  extends StatusResponse {

	protected ValidationType type;

	protected String field;
	
	public ValidationResponse(ValidationType t) {
		super(t.code(), t.value(), t.value());
		this.type = t;
	}
	
	public ValidationResponse(ValidationType t, String message) {
		super(t.code(), t.value(), message);
		this.type = t;
	}
	
	public ValidationResponse(ValidationType t, String message, String field) {
		super(t.code(), t.value(), message);
		this.type = t;
		this.field = field;
	}
	
	public static List<ValidationResponse> create(ValidationType t, String message) {
		List<ValidationResponse> resp = new ArrayList<>();
		resp.add(new ValidationResponse(t, message));
		return resp;
	}
	
	public static List<ValidationResponse> errors(ValidationType t, String message) {
		List<ValidationResponse> resp = new ArrayList<>();
		resp.add(new ValidationResponse(t, message));
		return resp;
	}
	
	public static List<ValidationResponse> errors(String message) {
		return errors(ValidationType.INVALID_DATA, message);
	}
	
	public static List<ValidationResponse> errors(Exception e) {
		return errors(ValidationType.UNEXPECTED_ERROR, e.getMessage() );
	}

	
	public static List<ValidationResponse> errors() {
		return errors(ValidationType.INVALID_DATA, "invalid.data" );
	}
	
	public static List<ValidationResponse> create(ValidationType t) {
		return create(t, null);
	}

	
	public static List<ValidationResponse> successful() {
		return create(ValidationType.VALIDATION_SUCCESSFUL, "success" );
	}
	
	
	public static List<ValidationResponse> successful(String msg) {
		return create(ValidationType.VALIDATION_SUCCESSFUL, msg );
	}


	public ValidationType getValidationType() {
		return type;
	}
	
	public String getField() {
		return field;
	}
	
	public void setField(String field) {
		this.field = field;
	}
	
}
