package com.sap.sailing.android.buoy.positioning.app.valueobjects;

public class Mark {
	
	private int id;
	private String name;
	private String type;
	private String className;
	
	public Mark(int id, String name, String type, String className){
		setId(id);
		setName(name);
		setType(type);
		setClassName(className);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
	
}
