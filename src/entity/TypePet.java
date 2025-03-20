package entity;

public class TypePet {
    private int typePetID;
    private String typeName;

    public TypePet() {}

    public TypePet(int typePetID, String typeName) {
        this.typePetID = typePetID;
        this.typeName = typeName;
    }

    public int getTypePetID() {
        return typePetID;
    }

    public void setTypePetID(int typePetID) {
        this.typePetID = typePetID;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return "TypePet: ID: " + typePetID + "\n\tType Name: " + typeName;
    }
}
