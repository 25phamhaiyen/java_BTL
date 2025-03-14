package entity;

public class TypePet {
	private int typePetID;
	private String UN_TypeName;
	public TypePet() {
		super();
	}
	public TypePet(int typePetID, String uN_TypeName) {
		super();
		this.typePetID = typePetID;
		UN_TypeName = uN_TypeName;
	}
	public int getTypePetID() {
		return typePetID;
	}
	public void setTypePetID(int typePetID) {
		this.typePetID = typePetID;
	}
	public String getUN_TypeName() {
		return UN_TypeName;
	}
	public void setUN_TypeName(String uN_TypeName) {
		UN_TypeName = uN_TypeName;
	}
	@Override
	public String toString() {
		return "Type Pet: ID: " + typePetID + "\n\\t\\tName: " + UN_TypeName;
	}
	
	
}	
