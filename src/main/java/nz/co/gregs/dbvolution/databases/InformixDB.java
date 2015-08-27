/*
 * Copyright 2013 Gregory Graham.
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

import nz.co.gregs.dbvolution.DBDatabase;
import java.sql.SQLException;
import javax.sql.DataSource;
import nz.co.gregs.dbvolution.databases.definitions.DBDefinition;
import nz.co.gregs.dbvolution.databases.definitions.InformixDBDefinition;

/**
 * A version of DBDatabase tweaked for Informix 7 and higher.
 *
 * @author Gregory Graham
 */
public class InformixDB extends DBDatabase {

	private final static String INFORMIXDRIVERNAME = "com.informix.jdbc.IfxDriver";

	/**
	 * Create a database object for a Informix 7+ database using the supplied definition and datasource.
	 *
	 * @param definition
	 * @param ds
	 * @throws SQLException
	 */
	protected InformixDB(DBDefinition definition, DataSource ds) throws SQLException {
		super(definition, ds);
		// Informix causes problems when using batched statements :(
		setBatchSQLStatementsWhenPossible(false);
	}

	/**
	 * Create a database object for a Informix 7+ database using the supplied definition and connection details.
	 *
	 * @param definition
	 * @param driverName 
	 * @param jdbcURL 
	 * @param username 
	 * @param password
	 */
	protected InformixDB(DBDefinition definition, String driverName, String jdbcURL, String username, String password) {
		super(definition, driverName, jdbcURL, username, password);
		// Informix causes problems when using batched statements :(
		setBatchSQLStatementsWhenPossible(false);
	}

	/**
	 * Creates a DBDatabase configured for Informix with the given JDBC URL,
	 * username, and password.
	 *
	 * <p>
	 * Remember to include the Informix JDBC driver in your classpath.
	 *
	 *
	 *
	 *
	 *
	 * 1 Database exceptions may be thrown
	 *
	 * @param jdbcURL jdbcURL
	 * @param username username
	 * @param password password
	 * @throws java.lang.ClassNotFoundException java.lang.ClassNotFoundException
	 * @throws java.sql.SQLException java.sql.SQLException
	 */
	public InformixDB(String jdbcURL, String username, String password) throws ClassNotFoundException, SQLException {
		this(new InformixDBDefinition(), INFORMIXDRIVERNAME, jdbcURL, username, password);
	}
	/**
	 * Creates a DBDatabase configured for Informix for the given data source.
	 *
	 * <p>
	 * Remember to include the Informix JDBC driver in your classpath.
	 *
	 *
	 *
	 * 1 Database exceptions may be thrown
	 *
	 * @param dataSource dataSource
	 * @throws java.lang.ClassNotFoundException java.lang.ClassNotFoundException
	 * @throws java.sql.SQLException java.sql.SQLException
	 */
	public InformixDB(DataSource dataSource) throws ClassNotFoundException, SQLException {
		this(new InformixDBDefinition(), dataSource);
	}

	@Override
	public DBDatabase clone() throws CloneNotSupportedException {
		return super.clone(); //To change body of generated methods, choose Tools | Templates.
	}
}
