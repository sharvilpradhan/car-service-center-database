package ui;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class CARS {

	private static final String URL = "jdbc:oracle:thin:@orca.csc.ncsu.edu:1521:orcl01";

	Connection con;
	Scanner in;

	public CARS(Connection con) {
		this.con = con;
		in = new Scanner(System.in);
	}

	public static void main(String[] args) {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			String user = args[0];
			String password = args[1];
			CARS cars = new CARS(DriverManager.getConnection(URL, user, password));
			cars.home();
		} catch (ClassNotFoundException | SQLException e) {
			System.out.println("Unable to connect to database!");
		}
	}

	private int menu(String... opts) {
		for (int i = 0; i < opts.length; i++) {
			System.out.println((i + 1) + ". " + opts[i]);
		}
		int choice = 0;
		while (true) {
			try {
				choice = in.nextInt();
				if (choice < 1 || choice > opts.length) {
					System.out.println("Invalid choice: must be (1-" + opts.length + ")");
				} else {
					break;
				}
			} catch (InputMismatchException e) {
				System.out.println("Input must be an integer");
				in.nextLine();
			}
		}
		in.nextLine();
		return choice;
	}

	private String input(String name) {
		System.out.println(name + ":");
		return in.nextLine();
	}

	private void checkError(Exception e) {
		boolean handled = false;
		String m = e.getMessage().replaceAll("\n", " ");
		if (m.contains("value too large")) {
			String[] a = m.split(" ");
			System.out.println(a[6].split("\"")[5].toLowerCase() + " must have fewer than or equal to "
					+ a[10].replace(")", "") + " characters");
			handled = true;
		}
		for (Constraint c : Constraint.values()) {
			if (m.contains(c.name)) {
				System.out.println(c.message);
				handled = true;
			}
		}
		if (!handled) {
			e.printStackTrace();
		}
	}

	// Start pages

	private void home() {
		while (true) {
			int c = menu("Login", "Sign Up", "Exit");
			switch (c) {
			case 1:
				login();
				break;
			case 2:
				signUp();
				break;
			case 3:
				try {
					con.close();
				} catch (SQLException e) {
					checkError(e);
				}
				return;
			}
		}
	}

	private void login() {
		while (true) {
			String username = input("User ID");
			String password = input("Password");
			int c = menu("Sign-In", "Go Back");
			switch (c) {
			case 1:
				try {
					PreparedStatement ps = con.prepareStatement(Query.authenticate.stmt);
					ps.setString(1, username);
					ps.setString(2, password);
					ResultSet rs = ps.executeQuery();
					if (rs.next()) {
						String role = rs.getString(1);
						if (role.equals("customer")) {
							ps = con.prepareStatement(Query.getCustomerId.stmt);
							ps.setString(1, username);
							rs = ps.executeQuery();
							rs.next();
							cstLandingPage(rs.getString(1));
							ps.close();
							rs.close();
							return;
						} else if (role.equals("receptionist")) {
							recLandingPage(username);
							return;
						} else if (role.equals("manager")) {
							mngLandingPage(username);
							return;
						}
					} else {
						System.out.println("Login Incorrect");
						break;
					}
				} catch (SQLException e) {
					checkError(e);
					break;
				}

			case 2:
				return;
			}
		}
	}

	private void signUp() {
		while (true) {
			String email = input("Email Address");
			String password = input("Password");
			String name = input("Name");
			String address = input("Address");
			String phone = input("Phone number");
			int c = menu("Sign Up", "Go Back");
			switch (c) {
			case 1:
				try {
					CallableStatement cs = con.prepareCall("{CALL CREATECUSTOMER(?,?,?,?,?,?)}");
					cs.setString(1, email);
					cs.setString(2, password);
					cs.setString(3, name);
					cs.setString(4, address);
					cs.setString(5, phone);
					cs.setString(6, "S0001");
					cs.executeUpdate();
					cs.close();
				} catch (SQLException e) {
					checkError(e);
					break;
				}
				return;
			case 2:
				return;
			}
		}
	}

	// Customer pages

	private void cstLandingPage(String cusID) {
		while (true) {
			int c = menu("Profile", "Register Car", "Service", "Invoices", "Logout");
			switch (c) {
			case 1:
				cstProfile(cusID);
				break;
			case 2:
				cstRegisterCar(cusID);
				break;
			case 3:
				cstService(cusID);
				break;
			case 4:
				cstInvoice(cusID);
				break;
			case 5:
				return;
			}
		}
	}

	private void cstProfile(String cusID) {
		while (true) {
			int c = menu("View Profile", "Update Profile", "Go Back");
			switch (c) {
			case 1:
				cstViewProfile(cusID);
				break;
			case 2:
				cstUpdateProfile(cusID);
				break;
			case 3:
				return;
			}
		}
	}

	private void cstViewProfile(String cusID) {
		try {
			PreparedStatement ps = con.prepareStatement(Query.customerProfile.stmt);
			ps.setString(1, cusID);
			ResultSet rs = ps.executeQuery();
			rs.next();
			System.out.println("Customer ID: " + rs.getString(1));
			System.out.println("Name: " + rs.getString(2));
			System.out.println("Address: " + rs.getString(3));
			System.out.println("Email: " + rs.getString(4));
			System.out.println("Phone: " + rs.getLong(5));
			System.out.println("Cars:");
			ps = con.prepareStatement(Query.customerVehicles.stmt);
			ps.setString(1, cusID);
			rs = ps.executeQuery();
			boolean hasVal = rs.next();
			if (!hasVal) {
				System.out.println("none");
			}
			while (hasVal) {
				System.out.println(rs.getString(1) + " " + rs.getString(2) + " " + rs.getString(3) + " "
						+ rs.getString(4) + " " + rs.getString(5));
				hasVal = rs.next();
			}
			ps.close();
			rs.close();
		} catch (SQLException e) {
			checkError(e);
		}
		menu("Go Back");
	}

	private void cstUpdateProfile(String cusID) {
		while (true) {
			int c = menu("Name", "Address", "Phone Number", "Password", "Go Back");
			PreparedStatement ps = null;
			switch (c) {
			case 1:
				String name = input("Name");
				try {
					ps = con.prepareStatement(Query.customerUpdateName.stmt);
					ps.setString(1, name);
					ps.setString(2, cusID);
					ps.executeQuery();
					ps.close();
				} catch (SQLException e) {
					checkError(e);
				}
				break;
			case 2:
				String address = input("Address");
				try {
					ps = con.prepareStatement(Query.customerUpdateAddress.stmt);
					ps.setString(1, address);
					ps.setString(2, cusID);
					ps.executeQuery();
					ps.close();
				} catch (SQLException e) {
					checkError(e);
				}
				break;
			case 3:
				String phone = input("Phone Number");
				try {
					ps = con.prepareStatement(Query.customerUpdatePhone.stmt);
					ps.setString(1, phone);
					ps.setString(2, cusID);
					ps.executeQuery();
					ps.close();
				} catch (SQLException e) {
					checkError(e);
				}
				break;
			case 4:
				String password = input("Password");
				try {
					ps = con.prepareStatement(Query.customerUpdatePassword.stmt);
					ps.setString(1, password);
					ps.setString(2, cusID);
					ps.executeQuery();
					ps.close();
				} catch (SQLException e) {
					checkError(e);
				}
				break;
			case 5:
				return;
			}
		}
	}

	private void cstRegisterCar(String cusID) {
		while (true) {
			String plate = input("Liscense plate");
			String purchaseDate = input("Purchase date");
			String make = input("Make");
			String model = input("Model");
			String year = input("Year");
			String mileage = input("Current mileage");
			String lastServiceDate = input("Last Service Date");
			int c = menu("Register", "Cancel");
			switch (c) {
			case 1:
				PreparedStatement ps;
				try {
					ps = con.prepareStatement(Query.registerCar.stmt);
					ps.setString(1, plate);
					ps.setString(2, make);
					ps.setString(3, model);
					ps.setString(4, year);
					ps.setString(5, purchaseDate);
					ps.setString(6, mileage);
					ps.setString(7, lastServiceDate);
					ps.setString(8, cusID);
					ResultSet rs = ps.executeQuery();
					ps.close();
					rs.close();
				} catch (SQLException e) {
					checkError(e);
					break;
				}
				return;
			case 2:
				return;
			}
		}
	}

	private void cstService(String cusID) {
		while (true) {
			int c = menu("View Service History", "Schedule Service", "Reschedule Service", "Go Back");
			switch (c) {
			case 1:
				cstViewServiceHistory(cusID);
				break;
			case 2:
				cstScheduleService(cusID);
				break;
			case 3:
				recRescheduleService1();
				break;
			case 4:
				return;
			}
		}
	}

	private void cstViewServiceHistory(String cusID) {
		try {
			PreparedStatement ps = con.prepareStatement(Query.customerServiceHistory.stmt);
			ps.setString(1, cusID);
			ps.setString(2, cusID);
			ResultSet rs = ps.executeQuery();
			boolean hasRow = rs.next();
			while (hasRow) {
				System.out.println("Service ID: " + rs.getString(1));
				System.out.println("    License Plate: " + rs.getString(2));
				System.out.println("    Service Type: " + rs.getString(3));
				System.out.println("    Mechanic Name: " + rs.getString(4));
				System.out.println("    Service Start: " + rs.getString(5));
				System.out.println("    Service End: " + rs.getString(6));
				hasRow = rs.next();
			}
		} catch (SQLException e) {
			checkError(e);
		}
		menu("Go Back");
		return;
	}

	private void cstScheduleService(String custID) {
		while (true) {
			String plate = input("Liscense Plate");
			String mileage = input("Current Mileage");
			String mechanic = input("Mechanic Name (Optional)");
			int c = menu("Schedule Maintenance", "Schedule Repair", "Go Back");
			switch (c) {
			case 1:
				cstScheduleMaintenance1(custID, plate, mileage, mechanic);
				break;
			case 2:
				cstScheduleRepair1(custID, plate, mileage, mechanic);
				break;
			case 3:
				return;
			}
		}
	}

	private void cstScheduleMaintenance1(String custID, String plate, String mileage, String mechanic) {
		while (true) {
			int c = menu("Find Service Date", "Go Back");
			switch (c) {
			case 1:
				// TODO find two earliest service dates
				boolean found = true;
				if (found) {
					cstScheduleMaintenance2();
					break;
				} else {
					// TODO calculate date to try again
					return;
				}
			case 2:
				return;
			}
		}
	}

	private void cstScheduleMaintenance2() {
		// TODO display two dates
		int c = menu("Schedule on Date", "Go Back");
		switch (c) {
		case 1:
			// TODO choose a date and schedule
			return;
		case 2:
			return;
		}
	}

	private void cstScheduleRepair1(String custID, String plate, String mileage, String mechanic) {
		while (true) {
			int c = menu("Engine knock", "Car drifts in a particular direction", "Battery does not hold charge",
					"Black/unclean exhaust", "A/C-Heater not working", "Headlamps/Tail lamps not working",
					"Check engine light", "Go Back");
			String issue = null;
			switch (c) {
			case 1:
				issue = "Engine knock";
				break;
			case 2:
				issue = "Car drifts in a particular direction";
				break;
			case 3:
				issue = "Battery does not hold charge";
				break;
			case 4:
				issue = "Black/unclean exhaust";
				break;
			case 5:
				issue = "A/C-Heater not working";
				break;
			case 6:
				issue = "Headlamps/Tail lamps not working";
				break;
			case 7:
				issue = "Check engine light";
				break;
			case 8:
				return;
			}
			try {
				PreparedStatement ps = con.prepareStatement(Query.customerScheduleRepairInfo.stmt);
				ps.setString(1, issue);
				ps.setString(2, custID);
				ps.setString(3, plate);
				ResultSet rs = ps.executeQuery();
				System.out.println("Diagnostic Report:");
				boolean hasRow = rs.next();
				while (hasRow) {
					System.out.println("    " + rs.getString(1));
					System.out.println("    " + rs.getString(2));
					System.out.println("    Fee: " + rs.getString(3));
					System.out.println("    Part: " + rs.getString(4));
					System.out.println("    Count: " + rs.getString(5));
					System.out.println("    Basic Service: " + rs.getString(6));
					hasRow = rs.next();
				}
				ps.close();
				rs.close();
			} catch (SQLException e) {
				checkError(e);
			}

			// TODO find two earliest service dates
			boolean found = true;
			if (found) {
				cstScheduleRepair2();
				break;
			} else {
				// TODO calculate date to try again
				return;
			}
		}
	}

	private void cstScheduleRepair2() {
		// TODO display two dates
		int c = menu("Repair on Date", "Go Back");
		switch (c) {
		case 1:
			// TODO choose a date and schedule
			return;
		case 2:
			return;
		}
	}

	private void cstRescheduleService1() {
		while (true) {
			// TODO Display all details for upcoming services
			int c = menu("Pick a service", "Go Back");
			switch (c) {
			case 1:
				String serviceID = input("Serice ID");
				// TODO find two earliest available dates
				cstRescheduleService2();
				break;
			case 2:
				return;
			}
		}
	}

	private void cstRescheduleService2() {
		// TODO display the dates and mechanic
		int c = menu("Reschedule Date", "Go Back");
		switch (c) {
		case 1:
			// TODO pick one and schedule it
			return;
		case 2:
			return;
		}
	}

	private void cstInvoice(String cusID) {
		while (true) {
			// TODO display details
			int c = menu("View Invoice Details", "Go Back");
			switch (c) {
			case 1:
				cstViewInvoiceDetails();
				break;
			case 2:
				return;
			}
		}
	}

	private void cstViewInvoiceDetails() {
		String serviceID = input("Service ID");
		// TODO show service details
		menu("Go Back");
	}

	// Employee (receptionist, manager) pages

	private void recLandingPage(String empID) {
		while (true) {
			int c = menu("Profile", "View Customer Profile", "Register Car", "Service History", "Schedule Service",
					"Reschedule Service", "Invoices", "Daily Task-Update Inventory", "Daily Task-Record Deliveries",
					"Logout");
			switch (c) {
			case 1:
				empProfile(empID);
				break;
			case 2:
				empViewCstProfile();
				break;
			case 3:
				recRegisterCar();
				break;
			case 4:
				recServiceHistory();
				break;
			case 5:
				recScheduleService();
				break;
			case 6:
				recRescheduleService1();
				break;
			case 7:
				recInvoices();
				break;
			case 8:
				recDTUpdateInventory();
				break;
			case 9:
				recDTRecordDeliveries();
				break;
			case 10:
				return;
			}
		}
	}

	private void mngLandingPage(String empID) {
		while (true) {
			int c = menu("Profile", "View Customer Profile", "Add New Employees", "Payroll", "Inventory", "Orders",
					"Notifications", "New Car Model", "Car Service Details", "Service History", "Invoices", "Logout");
			switch (c) {
			case 1:
				empProfile(empID);
				break;
			case 2:
				empViewCstProfile();
				break;
			case 3:
				mngAddNewEmployees(empID);
				break;
			case 4:
				mngPayroll();
				break;
			case 5:
				mngInventory(empID);
				break;
			case 6:
				mngOrders(empID);
				break;
			case 7:
				mngNotifications();
				break;
			case 8:
				mngNewCarModel();
				break;
			case 9:
				mngCarServiceDetails();
				break;
			case 10:
				mngServiceHistory();
				break;
			case 11:
				mngInvoices();
				break;
			case 12:
				return;
			}
		}
	}

	private void empProfile(String empID) {
		while (true) {
			int c = menu("View Profile", "Update Profile", "Go Back");
			switch (c) {
			case 1:
				empViewProfile(empID);
				break;
			case 2:
				empUpdateProfile(empID);
				break;
			case 3:
				return;
			}
		}
	}

	private void empViewProfile(String empID) {
		try {
			PreparedStatement ps = con.prepareStatement(Query.employeeProfile.stmt);
			ps.setString(1, empID);
			ResultSet rs = ps.executeQuery();
			rs.next();
			System.out.println("Employee ID: " + rs.getString(1));
			System.out.println("Name: " + rs.getString(2));
			System.out.println("Address: " + rs.getString(3));
			System.out.println("Email: " + rs.getString(4));
			System.out.println("Phone: " + rs.getString(5));
			System.out.println("Service Center: " + rs.getString(6));
			System.out.println("Role: " + rs.getString(7));
			System.out.println("Start Date: " + rs.getString(8));
			System.out.println("Compensation: " + rs.getString(9) + " per month");
			ps.close();
			rs.close();
		} catch (SQLException e) {
			checkError(e);
		}
		menu("Go Back");
		return;
	}

	private void empUpdateProfile(String empID) {
		while (true) {
			int c = menu("Name", "Address", "Email Address", "Phone Number", "Password", "Go Back");
			PreparedStatement ps = null;
			switch (c) {
			case 1:
				String name = input("Name");
				try {
					ps = con.prepareStatement(Query.employeeUpdateName.stmt);
					ps.setString(1, name);
					ps.setString(2, empID);
					ps.executeQuery();
					ps.close();
				} catch (SQLException e) {
					checkError(e);
				}
				break;
			case 2:
				String address = input("Address");
				try {
					ps = con.prepareStatement(Query.employeeUpdateAddress.stmt);
					ps.setString(1, address);
					ps.setString(2, empID);
					ps.executeQuery();
					ps.close();
				} catch (SQLException e) {
					checkError(e);
				}
				break;
			case 3:
				String email = input("Email");
				try {
					ps = con.prepareStatement(Query.employeeUpdateEmail.stmt);
					ps.setString(1, email);
					ps.setString(2, empID);
					ps.executeQuery();
					ps.close();
				} catch (SQLException e) {
					checkError(e);
				}
				break;
			case 4:
				String phone = input("Phone");
				try {
					ps = con.prepareStatement(Query.employeeUpdatePhone.stmt);
					ps.setString(1, phone);
					ps.setString(2, empID);
					ps.executeQuery();
					ps.close();
				} catch (SQLException e) {
					checkError(e);
				}
				break;
			case 5:
				String password = input("Password");
				try {
					ps = con.prepareStatement(Query.employeeUpdatePassword.stmt);
					ps.setString(1, password);
					ps.setString(2, empID);
					ps.executeQuery();
					ps.close();
				} catch (SQLException e) {
					checkError(e);
				}
				break;
			case 6:
				return;
			}
		}
	}

	private void empViewCstProfile() {
		String email = input("Customer Email Address");
		try {
			PreparedStatement ps = con.prepareStatement(Query.lookupCustomerProfile.stmt);
			ps.setString(1, email);
			ResultSet rs = ps.executeQuery();
			rs.next();
			System.out.println("Customer ID: " + rs.getString(1));
			System.out.println("Name: " + rs.getString(2));
			System.out.println("Address: " + rs.getString(3));
			System.out.println("Email: " + rs.getString(4));
			System.out.println("Phone: " + rs.getLong(5));
			System.out.println("Cars:");
			ps = con.prepareStatement(Query.lookupCustomerVehicles.stmt);
			ps.setString(1, email);
			rs = ps.executeQuery();
			boolean hasVal = rs.next();
			if (!hasVal) {
				System.out.println("none");
			}
			while (hasVal) {
				System.out.println(rs.getString(1) + " " + rs.getString(2) + " " + rs.getString(3) + " "
						+ rs.getString(4) + " " + rs.getString(5));
				hasVal = rs.next();
			}
			ps.close();
			rs.close();
		} catch (SQLException e) {
			checkError(e);
		}
		menu("Go Back");
	}

	private void recRegisterCar() {
		String email = input("Customer Email Address");
		String liscensePlate = input("Liscense Plate");
		String purchaseDate = input("Purchase Date");
		String make = input("Make");
		String model = input("Model");
		String year = input("Year");
		String mileage = input("Current Mileage");
		String lastServiceDate = input("Last Service Date");
		int c = menu("Register", "Cancel");
		switch (c) {
		case 1:
			// TODO register car info
			return;
		case 2:
			return;
		}
	}

	private void recServiceHistory() {
		String email = input("Customer Email Address");
		try {
			PreparedStatement ps = con.prepareStatement(Query.lookupCustomerServiceHistory.stmt);
			ps.setString(1, email);
			ps.setString(2, email);
			ResultSet rs = ps.executeQuery();
			boolean hasRow = rs.next();
			while (hasRow) {
				System.out.println("Service ID: " + rs.getString(1));
				System.out.println("    License Plate: " + rs.getString(2));
				System.out.println("    Service Type: " + rs.getString(3));
				System.out.println("    Mechanic Name: " + rs.getString(4));
				System.out.println("    Service Start: " + rs.getString(5));
				System.out.println("    Service End: " + rs.getString(6));
				hasRow = rs.next();
			}
		} catch (SQLException e) {
			checkError(e);
		}
		menu("Go Back");
	}

	private void recScheduleService() {
		while (true) {
			String email = input("Customer Email Address");
			String plate = input("Liscense Plate");
			String mileage = input("Current Mileage");
			String mechanic = input("Mechanic Name");
			int c = menu("Schedule Maintenance", "Schedule Repair", "Go Back");
			switch (c) {
			case 1:
				recScheduleMaintenance1();
				break;
			case 2:
				recScheduleRepair1();
				break;
			case 3:
				return;
			}
		}
	}

	private void recScheduleMaintenance1() {
		while (true) {
			int c = menu("Find Service Date", "Go Back");
			switch (c) {
			case 1:
				// TODO find two earliest service dates
				boolean found = true;
				if (found) {
					recScheduleMaintenance2();
					break;
				} else {
					// TODO calculate date to try again
					return;
				}
			case 2:
				return;
			}
		}
	}

	private void recScheduleMaintenance2() {
		// TODO display two dates
		int c = menu("Schedule on Date", "Go Back");
		switch (c) {
		case 1:
			// TODO choose a date and schedule
			return;
		case 2:
			return;
		}
	}

	private void recScheduleRepair1() {
		while (true) {
			int c = menu("Engine knock", "Car drifts in a particular direction", "Battery does not hold charge",
					"Black/unclean exhaust", "A/C-Heater not working", "Headlamps/Tail lamps not working",
					"Check engine light", "Go Back");
			switch (c) {
			case 1:
				// TODO Create report
				break;
			case 2:
				// TODO Create report
				break;
			case 3:
				// TODO Create report
				break;
			case 4:
				// TODO Create report
				break;
			case 5:
				// TODO Create report
				break;
			case 6:
				// TODO Create report
				break;
			case 7:
				// TODO Create report
				break;
			case 8:
				return;
			}
			// TODO find two earliest service dates
			boolean found = true;
			if (found) {
				recScheduleRepair2();
				break;
			} else {
				// TODO calculate date to try again
				return;
			}
		}
	}

	private void recScheduleRepair2() {
		// TODO display two dates
		int c = menu("Schedule on Date", "Go Back");
		switch (c) {
		case 1:
			// TODO choose a date and schedule
			return;
		case 2:
			return;
		}
	}

	private void recRescheduleService1() {
		while (true) {
			String email = input("Customer Email Address");
			// TODO Display all details for upcoming services
			int c = menu("Pick a service", "Go Back");
			switch (c) {
			case 1:
				String serviceID = input("Serice ID");
				// TODO find two earliest available dates
				recRescheduleService2();
				break;
			case 2:
				return;
			}
		}
	}

	private void recRescheduleService2() {
		// TODO display the dates and mechanic
		int c = menu("Reschedule Date", "Go Back");
		switch (c) {
		case 1:
			// TODO pick one and schedule it
			return;
		case 2:
			return;
		}
	}

	private void recInvoices() {
		String email = input("Customer Email Address");
		// TODO display all invoices
		menu("Go Back");
	}

	private void recDTUpdateInventory() {
		// TODO update part counts and display message
		menu("Go Back");
	}

	private void recDTRecordDeliveries() {
		int c = menu("Enter Order ID (CSV)", "Go Back");
		switch (c) {
		case 1:
			String orderIDs = input("Order IDs");
			// TODO set orders to complete and display message
			// TODO generate notification for delayed orders
			return;
		case 2:
			return;
		}
	}

	private void mngAddNewEmployees(String empID) {
		while (true) {
			String name = input("Name");
			String address = input("Address");
			String email = input("Email Address");
			String phone = input("Phone Number");
			String role = input("Role");
			String startDate = input("Start Date");
			String compensation = input("Compensation");
			int c = menu("Add", "Go Back");
			switch (c) {
			case 1:
				try {
					String newID = Integer.toString((int) (Math.random() * 1000000000));
					PreparedStatement ps = null;
					if (role.equals("receptionist")) {
						ps = con.prepareStatement(Query.addReceptionist.stmt);
					} else if (role.equals("mechanic")) {
						ps = con.prepareStatement(Query.addMechanic.stmt);
					} else {
						System.out.println("Role must be receptionist or mechanic");
						break;
					}
					ps.setString(1, newID);
					ps.setString(2, name);
					ps.setString(3, address);
					ps.setString(4, email);
					ps.setString(5, phone);
					ps.setString(6, role);
					ps.setString(7, startDate);
					ps.setString(8, compensation);
					ps.setString(9, empID);
					ps.executeQuery();
					ps.close();
					ps = con.prepareStatement(Query.signupEmployee.stmt);
					ps.setString(1, newID);
					ps.setString(2, role);
					ps.executeQuery();
					ps.close();
					System.out.println("Employee successfuly added with id " + newID);
				} catch (SQLException e) {
					checkError(e);
					break;
				}
				return;
			case 2:
				return;
			}
		}
	}

	private void mngPayroll() {
		String empID = input("Employee ID");
		// TODO display info
		menu("Go Back");
	}

	private void mngInventory(String empID) {
		try {
			PreparedStatement ps = con.prepareStatement(Query.checkInventory.stmt);
			ps.setString(1, empID);
			ResultSet rs = ps.executeQuery();
			boolean hasRow = rs.next();
			while (hasRow) {
				System.out.println("Part ID: " + rs.getString(1));
				System.out.println("    Part Name: " + rs.getString(2) + " " + rs.getString(3));
				System.out.println("    Quantity: " + rs.getString(4));
				System.out.println("    Unit Price: " + rs.getString(5));
				System.out.println("    Minimum Quantity Threshold: " + rs.getString(6));
				System.out.println("    Minimum Order Threshold: " + rs.getString(7));
				hasRow = rs.next();
			}
			ps.close();
			rs.close();
		} catch (SQLException e) {
			checkError(e);
		}

		menu("Go Back");
	}

	private void mngOrders(String empID) {
		while (true) {
			int c = menu("Order History", "New Order", "Go Back");
			switch (c) {
			case 1:
				mngOrderHistory(empID);
				break;
			case 2:
				mngNewOrder(empID);
				break;
			case 3:
				return;
			}
		}
	}

	private void mngOrderHistory(String empID) {
		try {
			PreparedStatement ps = con.prepareStatement(Query.orderHistory.stmt);
			ps.setString(1, empID);
			ps.setString(2, empID);
			ps.setString(3, empID);
			ResultSet rs = ps.executeQuery();
			boolean hasRow = rs.next();
			while (hasRow) {
				System.out.println("Order ID: " + rs.getString(1));
				System.out.println("    Date: " + rs.getString(2));
				System.out.println("    Part Name: " + rs.getString(3) + " " + rs.getString(4));
				System.out.println("    Supplier Name: " + rs.getString(5));
				System.out.println("    Purchaser Name: " + rs.getString(6));
				System.out.println("    Quantity: " + rs.getString(7));
				System.out.println("    Unit Price: " + rs.getString(8));
				System.out.println("    Total Cost: " + rs.getString(9));
				System.out.println("    Order Status: " + rs.getString(10));
				hasRow = rs.next();
			}
			ps.close();
			rs.close();
		} catch (SQLException e) {
			checkError(e);
		}
		menu("Go Back");
	}

	private void mngNewOrder(String empID) {
		String partID = input("Part ID");
		String quant = input("Quantity");
		int c = menu("Place Order", "Go Back");
		switch (c) {
		case 1:
			String newID = "O" + Integer.toString((int) (Math.random() * 10000));
			try {
				PreparedStatement ps = con.prepareStatement(Query.manualAddOrder.stmt);
				ps.setString(1, newID);
				ps.setString(2, partID);
				ps.setString(3, empID);
				ps.setString(4, quant);
				ps.executeQuery();
				ps = con.prepareStatement(Query.manualLinkDistributor.stmt);
				ps.setString(1, newID);
				ps.setString(2, partID);
				ps.setString(3, partID);
				ps.executeQuery();
				ps = con.prepareStatement(Query.orderMessage.stmt);
				ps.setString(1, newID);
				ResultSet rs = ps.executeQuery();
				rs.next();
				System.out.println("Order successfuly created with id " + newID);
				System.out.println("Estimated delivery date: " + rs.getString(1));
			} catch (SQLException e) {
				checkError(e);
			}
			return;
		case 2:
			return;
		}

	}

	private void mngNotifications() {
		while (true) {
			// TODO display notifications
			int c = menu("Order ID", "Go Back");
			switch (c) {
			case 1:
				mngNotificationsDetail();
				break;
			case 2:
				return;
			}
		}
	}

	private void mngNotificationsDetail() {
		// TODO display order details
		menu("Go Back");

	}

	private void mngNewCarModel() {
		String make = input("Make");
		String model = input("Model");
		String year = input("Year");
		System.out.println("--Service A--");
		String aMiles = input("Miles");
		String aMonths = input("Months");
		String aParts = input("Parts List");
		System.out.println("--Service B--");
		String bMiles = input("Miles");
		String bMonths = input("Months");
		String bParts = input("Additional Parts");
		System.out.println("--Service C--");
		String cMiles = input("Miles");
		String cMonths = input("Months");
		String cParts = input("Additional Parts");
		int c = menu("Add Car", "Go Back");
		switch (c) {
		case 1:
			// TODO add information to database
			return;
		case 2:
			return;
		}
	}

	private void mngCarServiceDetails() {
		// TODO display details for all car models
		menu("Go Back");

	}

	private void mngServiceHistory() {
		try {
			PreparedStatement ps = con.prepareStatement(Query.serviceHistory.stmt);
			ResultSet rs = ps.executeQuery();
			boolean hasRow = rs.next();
			while (hasRow) {
				System.out.println("Service ID: " + rs.getString(1));
				System.out.println("    License Plate: " + rs.getString(2));
				System.out.println("    Service Type: " + rs.getString(3));
				System.out.println("    Mechanic Name: " + rs.getString(4));
				System.out.println("    Service Start: " + rs.getString(5));
				System.out.println("    Service End: " + rs.getString(6));
				hasRow = rs.next();
			}
		} catch (SQLException e) {
			checkError(e);
		}
		menu("Go Back");

	}

	private void mngInvoices() {
		// TODO display info for all services completed
		menu("Go Back");
	}

}
