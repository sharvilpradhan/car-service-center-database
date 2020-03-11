package ui;

public enum Query {

	authenticate("SELECT L.ROLE\r\n" + "FROM LOGIN L\r\n" + "WHERE L.USERNAME = ? AND L.PASSWORD = ?\r\n" + ""),
	getCustomerId("SELECT C.CUSID\r\n" + "FROM CUSTOMER C\r\n" + "WHERE C.EMAIL = ?\r\n" + ""),
	customerProfile(
			"SELECT CUSID, NAME, ADDRESS, EMAIL, PHONE \r\n" + "FROM CUSTOMER\r\n" + "WHERE CUSID = ?\r\n" + ""),
	customerVehicles("SELECT LICENSE_PLATE,MAKE_NAME,MODEL,YEAR,PURCHASE_DATE\r\n" + "FROM CAR \r\n"
			+ "WHERE CUSID = ?\r\n" + ""),
	customerUpdateName("UPDATE CUSTOMER\r\n" + "SET NAME =?\r\n" + "WHERE CUSID =?"),
	customerUpdateAddress("UPDATE CUSTOMER\r\n" + "SET ADDRESS =?\r\n" + "WHERE CUSID =?"),
	customerUpdatePhone("UPDATE CUSTOMER\r\n" + "SET PHONE=?\r\n" + "WHERE CUSID =?"),
	customerUpdatePassword("UPDATE LOGIN\r\n" + "SET PASSWORD =?\r\n" + "WHERE EMAIL = ?"),
	registerCar(
			"INSERT INTO CAR (LICENSE_PLATE, MAKE_NAME, MODEL, YEAR, PURCHASE_DATE, CURRENT_MILEAGE, LAST_SERVICE_DATE, CUSID) VALUES (?, ?, ?, ?, to_date(?, 'YYYY-MM-DD'), ?, to_date(?, 'YYYY-MM-DD'), ?)"),
	customerViewMaintenanceHistory(
			"SELECT SID, LICENSE_PLATE, EMPID, REQ_DATE, CATEGORYNAME, SERVICE END DATE/TIME(CALCULATE), SERVICE STATUS(CALCULATE)\r\n"
					+ "FROM REQUEST R, MAINTENANCE_SERVICE M\r\n" + "WHERE R.REQID = M.REQID AND R.CUSID = ?; \r\n"
					+ ""),
	employeeProfile(
			"SELECT M.EMPID, M.NAME, M.ADDRESS, M.EMAIL, M.PHONE, S.NAME, M.POSITION, M.START_DATE, M.SALARY\r\n"
					+ "FROM MONTHLY M, SERVICE_CENTER S\r\n" + "WHERE M.EMPID = ? AND M.SID = S.SID\r\n" + ""),
	employeeUpdateName("UPDATE MONTHLY\r\n" + "SET NAME = ?\r\n" + "WHERE EMPID = ?\r\n" + ""),
	employeeUpdateAddress("UPDATE MONTHLY\r\n" + "SET ADDRESS = ?\r\n" + "WHERE EMPID = ?\r\n" + ""),
	employeeUpdateEmail("UPDATE MONTHLY\r\n" + "SET EMAIL = ?\r\n" + "WHERE EMPID = ?\r\n" + ""),
	employeeUpdatePhone("UPDATE MONTHLY\r\n" + "SET PHONE = ?\r\n" + "WHERE EMPID = ?\r\n" + ""),
	employeeUpdatePassword("UPDATE LOGIN\r\n" + "SET PASSWORD = ?\r\n" + "WHERE USERNAME = ?\r\n" + ""),
	lookupCustomerProfile(
			"SELECT CUSID, NAME, ADDRESS, EMAIL, PHONE \r\n" + "FROM CUSTOMER\r\n" + "WHERE EMAIL = ?\r\n" + ""),
	lookupCustomerVehicles("SELECT V.LICENSE_PLATE, V.MAKE_NAME, V.MODEL, V.YEAR, V.PURCHASE_DATE\r\n"
			+ "FROM CAR V, CUSTOMER C\r\n" + "WHERE C.EMAIL = ? AND V.CUSID = C.CUSID\r\n" + ""),
	addReceptionist("INSERT INTO MONTHLY (EMPID, NAME, ADDRESS, EMAIL, PHONE, POSITION, START_DATE,\r\n"
			+ "SALARY, SID)\r\n"
			+ "VALUES (?, ?, ?, ?, ?, ?, to_date(?, 'YYYY-MM-DD'), ?, (SELECT SID FROM MONTHLY WHERE EMPID = ?))\r\n"
			+ ""),
	addMechanic("INSERT INTO HOURLY (EMPID, NAME, ADDRESS, EMAIL, PHONE, POSITION, START_DATE,\r\n" + "WAGE, SID)\r\n"
			+ "VALUES (?, ?, ?, ?, ?, ?, to_date(?, 'YYYY-MM-DD'), ?, (SELECT SID FROM MONTHLY WHERE EMPID = ?))\r\n"
			+ ""),
	signupEmployee("INSERT INTO LOGIN (USERNAME, PASSWORD, ROLE) VALUES (?, '12345678', ?)"),
	checkInventory("SELECT P.PARTID, P.MAKE_NAME, P.NAME, I.QUANTITY, P.PRICE, I.MIN_QUANTITY, I.MIN_ORDER\r\n"
			+ "FROM PART_TYPE P, INVENTORY I, MONTHLY M\r\n"
			+ "WHERE M.EMPID = ? AND M.SID = I.SID AND I.PARTID = P.PARTID\r\n" + ""),
	orderHistory("SELECT\r\n" + "	O.ORDERID,\r\n" + "	O.PO_DATE,\r\n" + "	T.MAKE_NAME,\r\n" + "	T.NAME,\r\n"
			+ "	S.NAME,\r\n" + "	P.NAME,\r\n" + "	O.QUANTITY,\r\n" + "	T.PRICE,\r\n"
			+ "	(O.QUANTITY * T.PRICE) AS TOTAL_COST,\r\n" + "	O.STATUS\r\n" + "FROM\r\n" + "	PART_ORDER O,\r\n"
			+ "	PART_TYPE T,\r\n" + "	MONTHLY M,\r\n" + "	SERVICE_CENTER P,\r\n" + "	SERVICE_CENTER S,\r\n"
			+ "	FROM_SERVICE_CENTER F\r\n" + "WHERE\r\n" + "	M.EMPID = ?\r\n" + "	AND P.SID = M.SID\r\n"
			+ "	AND O.PLACED_BY = P.SID\r\n" + "	AND O.PARTID = T.PARTID\r\n" + "	AND F.ORDERID = O.ORDERID\r\n"
			+ "	AND S.SID = F.SID\r\n" + "UNION SELECT\r\n" + "	O.ORDERID,\r\n" + "	O.PO_DATE,\r\n"
			+ "	T.MAKE_NAME,\r\n" + "	T.NAME,\r\n" + "	S.NAME,\r\n" + "	P.NAME,\r\n" + "	O.QUANTITY,\r\n"
			+ "	T.PRICE,\r\n" + "	(O.QUANTITY * T.PRICE) AS TOTAL_COST,\r\n" + "	O.STATUS\r\n" + "FROM\r\n"
			+ "	PART_ORDER O,\r\n" + "	PART_TYPE T,\r\n" + "	MONTHLY M,\r\n" + "	SERVICE_CENTER P,\r\n"
			+ "	DISTRIBUTOR S,\r\n" + "	FROM_DIST F\r\n" + "WHERE\r\n" + "	M.EMPID = ?\r\n"
			+ "	AND P.SID = M.SID\r\n" + "	AND O.PLACED_BY = P.SID\r\n" + "	AND O.PARTID = T.PARTID\r\n"
			+ "	AND F.ORDERID = O.ORDERID\r\n" + "	AND S.DISTID = F.DISTID\r\n" + "UNION\r\n" + "SELECT\r\n"
			+ "	O.ORDERID,\r\n" + "	O.PO_DATE,\r\n" + "	T.MAKE_NAME,\r\n" + "	T.NAME,\r\n" + "	S.NAME,\r\n"
			+ "	P.NAME,\r\n" + "	O.QUANTITY,\r\n" + "	T.PRICE,\r\n"
			+ "	(O.QUANTITY * T.PRICE) AS TOTAL_COST,\r\n" + "	O.STATUS\r\n" + "FROM\r\n" + "	PART_ORDER O,\r\n"
			+ "	PART_TYPE T,\r\n" + "	MONTHLY M,\r\n" + "	SERVICE_CENTER P,\r\n" + "	SERVICE_CENTER S,\r\n"
			+ "	FROM_SERVICE_CENTER F\r\n" + "WHERE\r\n" + "	M.EMPID = ?\r\n" + "	AND S.SID = M.SID\r\n"
			+ "	AND F.SID = S.SID\r\n" + "	AND F.ORDERID = O.ORDERID\r\n" + "	AND P.SID = O.PLACED_BY\r\n"
			+ "	AND T.PARTID = O.PARTID\r\n" + ""),
	customerScheduleRepairInfo("SELECT DISTINCT RN.R_NAME, RN.DIAGNOSTICS, RN.D_FEE, P.NAME,C.COUNT, B.B_NAME\r\n"
			+ "FROM REPAIR_NAME RN, REQUEST R, CAR, CONTAINS C, PART_TYPE P, USES U, BASIC_SERVICE B, REPAIR\r\n"
			+ "WHERE RN.R_NAME = ?\r\n" + "AND R.CUSID = ?\r\n" + "AND R.LICENSE_PLATE = ?\r\n"
			+ "AND REPAIR.R_NAME = RN.R_NAME\r\n" + "AND CAR.LICENSE_PLATE = R.LICENSE_PLATE\r\n"
			+ "AND C.MAKE_NAME = CAR.MAKE_NAME\r\n" + "AND C.MODEL = CAR.MODEL\r\n" + "AND P.PARTID = C.PARTID\r\n"
			+ "AND U.PARTID = P.PARTID\r\n" + "AND B.BASIC_SERVICE_ID = U.BASIC_SERVICE_ID\r\n"
			+ "AND B.BASIC_SERVICE_ID = REPAIR.BASIC_SERVICE_ID"),
	serviceHistory("SELECT\r\n" + "	R.REQID,\r\n" + "	R.LICENSE_PLATE,\r\n" + "	'Maintenance' AS SERVICE_TYPE,\r\n"
			+ "	E.NAME,\r\n" + "	R.REQ_DATE,\r\n" + "	R.REQ_DATE + (R.DURATION / 48) AS END_DATE\r\n" + "FROM\r\n"
			+ "	REQUEST R,\r\n" + "	MAINTENANCE_SERVICE M,\r\n" + "	HOURLY E\r\n" + "WHERE\r\n"
			+ "	M.REQID = R.REQID\r\n" + "	AND R.EMPID = E.EMPID\r\n" + "UNION SELECT\r\n" + "	R.REQID,\r\n"
			+ "	R.LICENSE_PLATE,\r\n" + "	'Repair' AS SERVICE_TYPE,\r\n" + "	E.NAME,\r\n" + "	R.REQ_DATE,\r\n"
			+ "	R.REQ_DATE + (R.DURATION / 48) AS END_DATE\r\n" + "FROM\r\n" + "	REQUEST R,\r\n"
			+ "	REPAIR_SERVICE M,\r\n" + "	HOURLY E\r\n" + "WHERE\r\n" + "	M.REQID = R.REQID\r\n"
			+ "	AND R.EMPID = E.EMPID\r\n" + ""),
	customerServiceHistory("SELECT\r\n" + "	R.REQID,\r\n" + "	R.LICENSE_PLATE,\r\n"
			+ "	'Maintenance' AS SERVICE_TYPE,\r\n" + "	E.NAME,\r\n" + "	R.REQ_DATE,\r\n"
			+ "	R.REQ_DATE + (R.DURATION / 48) AS END_DATE\r\n" + "FROM\r\n" + "	REQUEST R,\r\n"
			+ "	MAINTENANCE_SERVICE M,\r\n" + "	HOURLY E\r\n" + "WHERE\r\n" + "	M.REQID = R.REQID\r\n"
			+ "	AND R.EMPID = E.EMPID\r\n" + "	AND R.CUSID = ?\r\n" + "UNION SELECT\r\n" + "	R.REQID,\r\n"
			+ "	R.LICENSE_PLATE,\r\n" + "	'Repair' AS SERVICE_TYPE,\r\n" + "	E.NAME,\r\n" + "	R.REQ_DATE,\r\n"
			+ "	R.REQ_DATE + (R.DURATION / 48) AS END_DATE\r\n" + "FROM\r\n" + "	REQUEST R,\r\n"
			+ "	REPAIR_SERVICE M,\r\n" + "	HOURLY E\r\n" + "WHERE\r\n" + "	M.REQID = R.REQID\r\n"
			+ "	AND R.EMPID = E.EMPID\r\n" + "	AND R.CUSID = ?\r\n" + ""),
	lookupCustomerServiceHistory(
			"SELECT\r\n" + "	R.REQID,\r\n" + "	R.LICENSE_PLATE,\r\n" + "	'Maintenance' AS SERVICE_TYPE,\r\n"
					+ "	E.NAME,\r\n" + "	R.REQ_DATE,\r\n" + "	R.REQ_DATE + (R.DURATION / 48) AS END_DATE\r\n"
					+ "FROM\r\n" + "	REQUEST R,\r\n" + "	MAINTENANCE_SERVICE M,\r\n" + "	HOURLY E,\r\n"
					+ "	CUSTOMER C\r\n" + "WHERE\r\n" + "	M.REQID = R.REQID\r\n" + "	AND R.EMPID = E.EMPID\r\n"
					+ "	AND R.CUSID = C.CUSID\r\n" + "	AND C.EMAIL = ?\r\n" + "UNION SELECT\r\n" + "	R.REQID,\r\n"
					+ "	R.LICENSE_PLATE,\r\n" + "	'Repair' AS SERVICE_TYPE,\r\n" + "	E.NAME,\r\n"
					+ "	R.REQ_DATE,\r\n" + "	R.REQ_DATE + (R.DURATION / 48) AS END_DATE\r\n" + "FROM\r\n"
					+ "	REQUEST R,\r\n" + "	REPAIR_SERVICE M,\r\n" + "	HOURLY E,\r\n" + "	CUSTOMER C\r\n"
					+ "WHERE\r\n" + "	M.REQID = R.REQID\r\n" + "	AND R.EMPID = E.EMPID\r\n"
					+ "	AND R.CUSID = C.CUSID\r\n" + "	AND C.EMAIL = ?"),
	manualAddOrder("INSERT\r\n" + "	INTO\r\n" + "		PART_ORDER (ORDERID,\r\n" + "		PARTID,\r\n"
			+ "		PLACED_BY,\r\n" + "		STATUS,\r\n" + "		QUANTITY,\r\n" + "		PO_DATE)\r\n"
			+ "	VALUES (?,\r\n" + "	?,\r\n" + "	(\r\n" + "	SELECT\r\n" + "		SID\r\n" + "	FROM\r\n"
			+ "		MONTHLY\r\n" + "	WHERE\r\n" + "		EMPID = ?),\r\n" + "	'pending',\r\n" + "	?,\r\n"
			+ "	CURRENT_DATE )"),
	manualLinkDistributor("INSERT\r\n" + "	INTO\r\n" + "		FROM_DIST (ORDERID,\r\n" + "		DISTID,\r\n"
			+ "		PARTID)\r\n" + "	VALUES (?,\r\n" + "	(\r\n" + "	SELECT\r\n" + "		DISTID\r\n" + "	FROM\r\n"
			+ "		WINDOWS\r\n" + "	WHERE\r\n" + "		PARTID = ?\r\n" + "		AND DELIVERY_WINDOW IS NOT NULL\r\n"
			+ "		AND rownum = 1),\r\n" + "	?)"),
	orderMessage("SELECT\r\n" + "	O.PO_DATE + W.DELIVERY_WINDOW AS EST_DELIVERY\r\n" + "FROM\r\n"
			+ "	PART_ORDER O,\r\n" + "	FROM_DIST F,\r\n" + "	WINDOWS W\r\n" + "WHERE\r\n" + "	O.ORDERID = ?\r\n"
			+ "	AND O.ORDERID = F.ORDERID\r\n" + "	AND W.DISTID = F.DISTID\r\n" + "	AND W.PARTID = O.PARTID"),
	other("");

	public String stmt;

	Query(String stmt) {
		this.stmt = stmt;
	}
}
