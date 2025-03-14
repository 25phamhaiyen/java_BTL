package entity;

public class TypeService {
	private int typeServiceID;
	private String UN_TypeService;
	public TypeService() {
		super();
	}
	public TypeService(int typeServiceID, String uN_TypeService) {
		super();
		this.typeServiceID = typeServiceID;
		UN_TypeService = uN_TypeService;
	}
	public int getTypeServiceID() {
		return typeServiceID;
	}
	public void setTypeServiceID(int typeServiceID) {
		this.typeServiceID = typeServiceID;
	}
	public String getUN_TypeService() {
		return UN_TypeService;
	}
	public void setUN_TypeService(String uN_TypeService) {
		UN_TypeService = uN_TypeService;
	}
	@Override
	public String toString() {
		return "Type Service: ID: " + typeServiceID + "\n\t\t\tService: " + UN_TypeService;
	}
	
}
