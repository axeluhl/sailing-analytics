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

}
