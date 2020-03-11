package ui;

public enum Constraint {
	
	check_phone("CHK_PHONE", "phone number must be 9 digits");
	
	public String name;
	public String message;
	
	Constraint(String name, String message) {
		this.name = name;
		this.message = message;
	}
}
