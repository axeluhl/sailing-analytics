package com.sap.sailing.domain.swisstimingadapter.impl;

import com.sap.sailing.domain.swisstimingadapter.CrewMember;

public class CrewMemberImpl implements CrewMember {
    private String name;
    private String nationality;
    private String position;

    public CrewMemberImpl(String name, String nationality, String position) {
        super();
        this.name = name;
        this.nationality = nationality;
        this.position = position;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNationality() {
        return nationality;
    }

    public String getPosition() {
        return position;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((nationality == null) ? 0 : nationality.hashCode());
        result = prime * result + ((position == null) ? 0 : position.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CrewMemberImpl other = (CrewMemberImpl) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (nationality == null) {
            if (other.nationality != null)
                return false;
        } else if (!nationality.equals(other.nationality))
            return false;
        if (position == null) {
            if (other.position != null)
                return false;
        } else if (!position.equals(other.position))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CrewMemberImpl [name=" + name + ", nationality=" + nationality + ", position=" + position + "]";
    }
}
