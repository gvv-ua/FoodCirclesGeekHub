package co.foodcircles.exception;

@SuppressWarnings("serial")
public class NetException2 extends Exception {

	private String message;

	public void setMessage(String message) {
		this.message=message;
	}
	
	public String getMessage(){
		return message;
	}

}
