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
import nz.co.gregs.dbvolution.DBDatabase;

/**
 * Extends DBStatement to add support for database transactions.
 *
 * <p>
 * Use {@link DBScript} to easily create a transaction.
 *
 * <p>
 * You should not need to create on of these as statements and transactions are
 * managed by DBDatabase automatically.
 *
 * <p>
 * Transactions are a collection of database actions that have a coherent
 * collective nature. This implies that even though they are separate java
 * statements that they should be handled collectively by the database.
 *
 * @author Gregory Graham
 */
public class DBTransactionStatement extends DBStatement {

	public DBTransactionStatement(DBDatabase database, DBStatement realStatement) throws SQLException {
		super(database, realStatement.getConnection());
	}

	@Override
	@SuppressWarnings("empty-statement")
	public void close() throws SQLException {
		realStatement.close();
		realStatement = connection.createStatement();
		;
	}

	public void transactionFinished() throws SQLException {
		super.close();
	}
}