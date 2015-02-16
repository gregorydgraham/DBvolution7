/*
 * Copyright 2013 greg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nz.co.gregs.dbvolution.databases;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import nz.co.gregs.dbvolution.DBDatabase;
import nz.co.gregs.dbvolution.databases.definitions.H2DBDefinition;
import nz.co.gregs.dbvolution.databases.supports.SupportsIntervalDatatype;
import nz.co.gregs.dbvolution.exceptions.DBRuntimeException;
import nz.co.gregs.dbvolution.exceptions.UnableToCreateDatabaseConnectionException;
import nz.co.gregs.dbvolution.exceptions.UnableToFindJDBCDriver;

/**
 * Stores all the required functionality to use an H2 database.
 *
 * @author Gregory Graham
 */
public class H2DB extends DBDatabase implements SupportsIntervalDatatype {

	/**
	 * Used to hold the database open
	 *
	 */
	protected Connection storedConnection;

	/**
	 * Creates a DBDatabase for a H2 database.
	 *
	 *
	 *
	 *
	 * 1 Database exceptions may be thrown
	 *
	 * @param jdbcURL jdbcURL
	 * @param username username
	 * @param password password
	 * @throws java.sql.SQLException java.sql.SQLException
	 */
	public H2DB(String jdbcURL, String username, String password) throws SQLException {
		super(new H2DBDefinition(), "org.h2.Driver", jdbcURL, username, password);
		jamDatabaseConnectionOpen();
		addIntervalFunctions();
	}

	private void addIntervalFunctions() throws UnableToFindJDBCDriver, UnableToCreateDatabaseConnectionException, SQLException {
		Connection connection = getConnection();
		Statement stmt = connection.createStatement();
		stmt.execute("CREATE ALIAS IF NOT EXISTS DBV_REVERSE AS $$ import java.util.*; @CODE String reverse(String s) { return new StringBuilder(s).reverse().toString(); } $$;");
		stmt.execute("CREATE ALIAS IF NOT EXISTS " + H2DBDefinition.INTERVAL_CREATION_FUNCTION + " DETERMINISTIC AS $$ \n"
				+ "import org.joda.time.Period;"
				+ "import java.util.*;"
				+ "@CODE String getIntervalString(Date original, Date compareTo) {\n"
				+ "		if (original==null||compareTo==null){return null;}\n"
				+ "		int years = original.getYear() - compareTo.getYear();\n"
				+ "		int months = original.getMonth() - compareTo.getMonth();\n"
				+ "		int days = original.getDay() - compareTo.getDay();\n"
				+ "		int hours = original.getHours() - compareTo.getHours();\n"
				+ "		int minutes = original.getMinutes() - compareTo.getMinutes();\n"
				+ "		int seconds = original.getSeconds() - compareTo.getSeconds();\n"
				+ "		int millis = (int) ((original.getTime() - ((original.getTime() / 1000) * 1000)) - (compareTo.getTime() - ((compareTo.getTime() / 1000) * 1000)));\n"
				+ "		String intervalString = \"P\" + years + \"Y\" + months + \"M\" + days + \"D\" + hours + \"h\" + minutes + \"m\" + seconds + \"s\" + millis;\n"
				+ "		return intervalString;"
				+ "	} $$;");
		stmt.execute("CREATE ALIAS IF NOT EXISTS " + H2DBDefinition.INTERVAL_DATEADDITION_FUNCTION + " DETERMINISTIC AS $$ \n"
				+ "import org.joda.time.Period;"
				+ "import java.util.*;"
				+ "import java.lang.*;"
				+ "@CODE Date addDateAndIntervalString(Date original, String intervalStr) {\n"
				+ "		if (original==null||intervalStr==null||intervalStr.length()==0||original.toString().length()==0||original.getTime()==0){return null;}else{\n"
				+ "		Calendar cal = new GregorianCalendar();\n"
				+ "		try{cal.setTime(original);}catch(Exception except){return null;}\n"
				+ "		int years = Integer.parseInt(intervalStr.replaceAll(\".*P([-0-9.]+)Y.*\", \"$1\"));\n"
				+ "		int months = Integer.parseInt(intervalStr.replaceAll(\".*Y([-0-9.]+)M.*\", \"$1\"));\n"
				+ "		int days = Integer.parseInt(intervalStr.replaceAll(\".*M([-0-9.]+)D.*\", \"$1\"));\n"
				+ "		int hours = Integer.parseInt(intervalStr.replaceAll(\".*D([-0-9.]+)h.*\", \"$1\"));\n"
				+ "		int minutes = Integer.parseInt(intervalStr.replaceAll(\".*h([-0-9.]+)m.*\", \"$1\"));\n"
				+ "		int seconds = Integer.parseInt(intervalStr.replaceAll(\".*m([-0-9.]+)s.*\", \"$1\"));\n"
				+ "		int millis = Integer.parseInt(intervalStr.replaceAll(\".*s([-0-9.]+)$\", \"$1\"));\n"
				+ "		cal.add(Calendar.YEAR, years);\n"
				+ "		cal.add(Calendar.MONTH, months);\n"
				+ "		cal.add(Calendar.DAY_OF_MONTH, days);\n"
				+ "		cal.add(Calendar.HOUR, hours);\n"
				+ "		cal.add(Calendar.MINUTE, minutes);\n"
				+ "		cal.add(Calendar.SECOND, seconds);\n"
				+ "		cal.add(Calendar.MILLISECOND, millis);\n"
				+ "		return cal.getTime();}\n"
				+ "	} $$;");
		stmt.execute("CREATE ALIAS IF NOT EXISTS " + H2DBDefinition.INTERVAL_DATESUBTRACTION_FUNCTION + " DETERMINISTIC AS $$ \n"
				+ "import org.joda.time.Period;"
				+ "import java.util.*;"
				+ "@CODE Date subtractDateAndIntervalString(Date original, String intervalStr) {\n"
				+ "		if (original==null||intervalStr==null){return null;}\n"
				+ "		Calendar cal = new GregorianCalendar();\n"
				+ "		cal.setTime(original);\n"
				+ "		int years = Integer.parseInt(intervalStr.replaceAll(\".*P([-0-9.]+)Y.*\", \"$1\"));\n"
				+ "		int months = Integer.parseInt(intervalStr.replaceAll(\".*Y([-0-9.]+)M.*\", \"$1\"));\n"
				+ "		int days = Integer.parseInt(intervalStr.replaceAll(\".*M([-0-9.]+)D.*\", \"$1\"));\n"
				+ "		int hours = Integer.parseInt(intervalStr.replaceAll(\".*D([-0-9.]+)h.*\", \"$1\"));\n"
				+ "		int minutes = Integer.parseInt(intervalStr.replaceAll(\".*h([-0-9.]+)m.*\", \"$1\"));\n"
				+ "		int seconds = Integer.parseInt(intervalStr.replaceAll(\".*m([-0-9.]+)s.*\", \"$1\"));\n"
				+ "		int millis = Integer.parseInt(intervalStr.replaceAll(\".*s([-0-9.]+)$\", \"$1\"));\n"
				+ "\n"
				+ "		cal.add(Calendar.YEAR, -1 * years);\n"
				+ "		cal.add(Calendar.MONTH, -1 * months);\n"
				+ "		cal.add(Calendar.DAY_OF_MONTH, -1 * days);\n"
				+ "		cal.add(Calendar.HOUR, -1 * hours);\n"
				+ "		cal.add(Calendar.MINUTE, -1 * minutes);\n"
				+ "		cal.add(Calendar.SECOND, -1 * seconds);\n"
				+ "		cal.add(Calendar.MILLISECOND, -1 * millis);\n"
				+ "		return cal.getTime();"
				+ "	} $$;");
		stmt.execute("CREATE ALIAS IF NOT EXISTS " + H2DBDefinition.INTERVAL_EQUALS_FUNCTION + " DETERMINISTIC AS $$ \n"
				+ "import org.joda.time.Period;"
				+ "import java.util.*;"
				+ "@CODE boolean isEqualTo(String original, String compareTo) {\n"
				+ "		if (original==null||compareTo==null){return false;}\n"
				+ "		String[] splitOriginal = original.split(\"[A-Za-z]\");\n"
				+ "		String[] splitCompareTo = compareTo.split(\"[A-Za-z]\");\n"
				+ "		for (int i = 1; i < splitCompareTo.length; i++) { // Start at 1 because the first split is empty\n"
				+ "			System.out.println(\"SPLITORIGINAL \"+i+\": \"+splitOriginal[i]);\n"
				+ "			int intOriginal = Integer.parseInt(splitOriginal[i]);\n"
				+ "			int intCompareTo = Integer.parseInt(splitCompareTo[i]);\n"
				+ "			if (intOriginal > intCompareTo) {\n"
				+ "				return false;\n"
				+ "			}\n"
				+ "			if (intOriginal < intCompareTo) {\n"
				+ "				return false;\n"
				+ "			}\n"
				+ "		}\n"
				+ "		return true;\n"
				+ "	} $$;");
		stmt.execute("CREATE ALIAS IF NOT EXISTS " + H2DBDefinition.INTERVAL_GREATERTHANEQUALS_FUNCTION + " DETERMINISTIC AS $$ \n"
				+ "import org.joda.time.Period;"
				+ "import java.util.*;"
				+ "@CODE boolean isEqualTo(String original, String compareTo) {\n"
				+ "		if (original==null||compareTo==null){return false;}\n"
				+ "		String[] splitOriginal = original.split(\"[A-Za-z]\");\n"
				+ "		String[] splitCompareTo = compareTo.split(\"[A-Za-z]\");\n"
				+ "		for (int i = 1; i < splitCompareTo.length; i++) { // Start at 1 because the first split is empty\n"
				+ "			System.out.println(\"SPLITORIGINAL \"+i+\": \"+splitOriginal[i]);\n"
				+ "			int intOriginal = Integer.parseInt(splitOriginal[i]);\n"
				+ "			int intCompareTo = Integer.parseInt(splitCompareTo[i]);\n"
				+ "			if (intOriginal > intCompareTo) {\n"
				+ "				return true;\n"
				+ "			}\n"
				+ "			if (intOriginal < intCompareTo) {\n"
				+ "				return false;\n"
				+ "			}\n"
				+ "		}\n"
				+ "		return true;\n"
				+ "	} $$;");
		stmt.execute("CREATE ALIAS IF NOT EXISTS " + H2DBDefinition.INTERVAL_GREATERTHAN_FUNCTION + " DETERMINISTIC AS $$ \n"
				+ "import org.joda.time.Period;"
				+ "import java.util.*;"
				+ "@CODE boolean isEqualTo(String original, String compareTo) {\n"
				+ "		if (original==null||compareTo==null){return false;}\n"
				+ "		String[] splitOriginal = original.split(\"[A-Za-z]\");\n"
				+ "		String[] splitCompareTo = compareTo.split(\"[A-Za-z]\");\n"
				+ "		for (int i = 1; i < splitCompareTo.length; i++) { // Start at 1 because the first split is empty\n"
				+ "			System.out.println(\"SPLITORIGINAL \"+i+\": \"+splitOriginal[i]);\n"
				+ "			int intOriginal = Integer.parseInt(splitOriginal[i]);\n"
				+ "			int intCompareTo = Integer.parseInt(splitCompareTo[i]);\n"
				+ "			if (intOriginal > intCompareTo) {\n"
				+ "				return true;\n"
				+ "			}\n"
				+ "			if (intOriginal < intCompareTo) {\n"
				+ "				return false;\n"
				+ "			}\n"
				+ "		}\n"
				+ "		return false;\n"
				+ "	} $$;");
		stmt.execute("CREATE ALIAS IF NOT EXISTS " + H2DBDefinition.INTERVAL_LESSTHANEQUALS_FUNCTION + " DETERMINISTIC AS $$ \n"
				+ "import org.joda.time.Period;"
				+ "import java.util.*;"
				+ "@CODE boolean isEqualTo(String original, String compareTo) {\n"
				+ "		if (original==null||compareTo==null){return false;}\n"
				+ "		String[] splitOriginal = original.split(\"[A-Za-z]\");\n"
				+ "		String[] splitCompareTo = compareTo.split(\"[A-Za-z]\");\n"
				+ "		for (int i = 1; i < splitCompareTo.length; i++) { // Start at 1 because the first split is empty\n"
				+ "			System.out.println(\"SPLITORIGINAL \"+i+\": \"+splitOriginal[i]);\n"
				+ "			int intOriginal = Integer.parseInt(splitOriginal[i]);\n"
				+ "			int intCompareTo = Integer.parseInt(splitCompareTo[i]);\n"
				+ "			if (intOriginal > intCompareTo) {\n"
				+ "				return false;\n"
				+ "			}\n"
				+ "			if (intOriginal < intCompareTo) {\n"
				+ "				return true;\n"
				+ "			}\n"
				+ "		}\n"
				+ "		return true;\n"
				+ "	} $$;");
		stmt.execute("CREATE ALIAS IF NOT EXISTS " + H2DBDefinition.INTERVAL_LESSTHAN_FUNCTION + " DETERMINISTIC AS $$ \n"
				+ "import org.joda.time.Period;"
				+ "import java.util.*;"
				+ "@CODE boolean isEqualTo(String original, String compareTo) {\n"
				+ "		if (original==null||compareTo==null){return false;}\n"
				+ "		String[] splitOriginal = original.split(\"[A-Za-z]\");\n"
				+ "		String[] splitCompareTo = compareTo.split(\"[A-Za-z]\");\n"
				+ "		for (int i = 1; i < splitCompareTo.length; i++) { // Start at 1 because the first split is empty\n"
				+ "			System.out.println(\"SPLITORIGINAL \"+i+\": \"+splitOriginal[i]);\n"
				+ "			int intOriginal = Integer.parseInt(splitOriginal[i]);\n"
				+ "			int intCompareTo = Integer.parseInt(splitCompareTo[i]);\n"
				+ "			if (intOriginal > intCompareTo) {\n"
				+ "				return false;\n"
				+ "			}\n"
				+ "			if (intOriginal < intCompareTo) {\n"
				+ "				return true;\n"
				+ "			}\n"
				+ "		}\n"
				+ "		return false;\n"
				+ "	} $$;");
	}

	/**
	 * Creates a DBDatabase for a H2 database in the file supplied.
	 *
	 *
	 *
	 *
	 *
	 * 1 Database exceptions may be thrown
	 *
	 * @param file file
	 * @param username username
	 * @param password password
	 * @throws java.io.IOException java.io.IOException
	 * @throws java.sql.SQLException java.sql.SQLException
	 */
	public H2DB(File file, String username, String password) throws IOException, SQLException {
		this("jdbc:h2:" + file.getCanonicalFile(), username, password);
	}

	/**
	 * Creates a DBDatabase for a H2 database.
	 *
	 *
	 * 1 Database exceptions may be thrown
	 *
	 * @param dataSource dataSource
	 * @throws java.sql.SQLException java.sql.SQLException
	 */
	public H2DB(DataSource dataSource) throws SQLException {
		super(new H2DBDefinition(), dataSource);
		jamDatabaseConnectionOpen();
	}

	private void jamDatabaseConnectionOpen() throws DBRuntimeException, SQLException {
		this.storedConnection = getConnection();
		this.storedConnection.createStatement();
	}

	@Override
	public boolean supportsFullOuterJoinNatively() {
		return false;
	}

	/**
	 * Clones the DBDatabase
	 *
	 * @return a clone of the database.
	 * @throws java.lang.CloneNotSupportedException
	 * java.lang.CloneNotSupportedException
	 *
	 */
	@Override
	public DBDatabase clone() throws CloneNotSupportedException {
		return super.clone(); //To change body of generated methods, choose Tools | Templates.
	}

}
