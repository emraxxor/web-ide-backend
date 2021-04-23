package hu.emraxxor.web.ide.data.type.response;

/**
 * 
 * @author Attila Barna
 */
public class StatusResponse {

	public int code;
	
	public String statusType;
	
	public String message;
	
	private Object object;
	
	public StatusResponse(int code, String statusType, String message) {
		this.code = code;
		this.message = message;
		this.statusType = statusType;
	}
	
	public StatusResponse(int code, String statusType) {
		this(code,statusType,"");
	}
	
	public static StatusResponse create(int code, String statusType) {
		return new StatusResponse(code, statusType);
	}

	public static StatusResponse success() {
		return StatusResponse.create(ResponseStatusType.SUCCESS);
	}
	
	public static StatusResponse success(Object o) {
		StatusResponse r = StatusResponse.create(ResponseStatusType.SUCCESS);
		r.setObject(o);
		return r;
	}
	
	public static StatusResponse error() {
		return StatusResponse.create(ResponseStatusType.ERROR);
	}

	
	public static StatusResponse success(String message) {
		return StatusResponse.create(ResponseStatusType.SUCCESS,message);
	}

	public static StatusResponse error(String msg) {
		return StatusResponse.create(ResponseStatusType.ERROR,msg);
	}

	public static StatusResponse error(Object o) {
		StatusResponse r = StatusResponse.create(ResponseStatusType.ERROR);
		r.setObject(o);
		return r;
	}
	
	public static StatusResponse error(Exception e) {
		return StatusResponse.create(ResponseStatusType.ERROR,e);
	}

	
	public static StatusResponse create(int code, String statusType, String message) {
		return new StatusResponse(code, statusType, message);
	}
	
	public static StatusResponse create(ResponseStatusType status) {
		return new StatusResponse(status.getCode(), status.toString() );
	}

	
	public static StatusResponse create(ResponseStatusType status,String message) {
		return new StatusResponse(status.getCode(), status.toString() , message);
	}

	
	public static StatusResponse create(ResponseStatusType status,Exception e) {
		return new StatusResponse(status.getCode(), status.toString() , e.getMessage());
	}
	
	public static StatusResponse create(Exception e) {
		return new StatusResponse(1, e.getClass().getName() , e.getMessage());
	}


	
	public String getStatusType() {
		return statusType;
	}
	
	public int getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setObject(Object object) {
		this.object = object;
	}
	
	public Object getObject() {
		return object;
	}
}
