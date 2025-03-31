package entity;

import Enum.TypeServiceEnum;

public class TypeService {
	private int typeServiceID;
	private TypeServiceEnum typeServiceName;

	public TypeService() {
		super();
	}

	public TypeService(int typeServiceID, TypeServiceEnum typeServiceName) {
		super();
		this.typeServiceID = typeServiceID;
		this.typeServiceName = typeServiceName;
	}

	public int getTypeServiceID() {
		return typeServiceID;
	}

	public void setTypeServiceID(int typeServiceID) {
		this.typeServiceID = typeServiceID;
	}

	public TypeServiceEnum getTypeServiceName() {
		return typeServiceName;
	}

	public void setTypeServiceName(TypeServiceEnum typeServiceName) {
		this.typeServiceName = typeServiceName;
	}

	@Override
	public String toString() {
		return "Type Service: ID: " + typeServiceID + "\nService: " + typeServiceName.getDescription();
	}
}
