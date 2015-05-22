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

import java.sql.SQLException;
import javax.sql.DataSource;
import nz.co.gregs.dbvolution.DBDatabase;

/**
 * Extends the PostgreSQL database connection by adding SSL.
 *
 * @author Gregory Graham
 */
public class PostgresDBOverSSL extends PostgresDB {


	/**
	 * Creates a {@link DBDatabase } instance for the data source.
	 *
	 * @param ds	 ds	
	 * @throws java.sql.SQLException	
	 */
	public PostgresDBOverSSL(DataSource ds) throws SQLException {
        super(ds);
    }
	
	/**
	 * Creates a DBDatabase for a PostgreSQL database over SSL.
	 *
	 * @param hostname hostname
	 * @param databaseName databaseName
	 * @param port port
	 * @param username username
	 * @param password password
	 * @param urlExtras urlExtras
	 * @throws java.sql.SQLException
	 */
	public PostgresDBOverSSL(String hostname, int port, String databaseName, String username, String password, String urlExtras) throws SQLException {
        super(hostname, port, databaseName, username, password, "ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory"+(urlExtras==null||urlExtras.isEmpty()?"":"&"+urlExtras));
    }

	/**
	 * Creates a DBDatabase for a PostgreSQL database over SSL.
	 *
	 * @param hostname hostname
	 * @param password password
	 * @param databaseName databaseName
	 * @param port port
	 * @param username username
	 * @throws java.sql.SQLException
	 */
	public PostgresDBOverSSL(String hostname, int port, String databaseName, String username, String password) throws SQLException {
        this(hostname, port, databaseName, username, password, "");
    }

	@Override
	public DBDatabase clone() throws CloneNotSupportedException {
		return super.clone(); //To change body of generated methods, choose Tools | Templates.
	}
    
}