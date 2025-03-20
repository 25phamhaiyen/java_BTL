package entity;

import Enum.RoleType;

public class Role {
    private int roleID;
    private RoleType roleName;

    public Role() {
        super();
    }

    public Role(int roleID, RoleType roleName) {
        super();
        this.roleID = roleID;
        this.roleName = roleName;
    }

    public int getRoleID() {
        return roleID;
    }

    public void setRoleID(int roleID) {
        this.roleID = roleID;
    }

    public RoleType getRoleName() {
        return roleName;
    }

    public void setRoleName(RoleType roleName) {
        this.roleName = roleName;
    }

    @Override
    public String toString() {
        return "Role: ID: " + roleID + "\t Name: " + roleName;
    }
}
