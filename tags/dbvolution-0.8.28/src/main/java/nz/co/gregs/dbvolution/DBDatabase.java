/*
 * Copyright 2013 gregory.graham.
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
package nz.co.gregs.dbvolution;

import nz.co.gregs.dbvolution.transactions.DBTransaction;
import java.io.PrintStream;
import java.sql.*;
import java.util.*;
import javax.sql.DataSource;
import nz.co.gregs.dbvolution.actions.DBActionList;
import nz.co.gregs.dbvolution.databases.DBStatement;
import nz.co.gregs.dbvolution.databases.DBTransactionStatement;
import nz.co.gregs.dbvolution.datatypes.QueryableDatatype;
import nz.co.gregs.dbvolution.databases.definitions.DBDefinition;
import nz.co.gregs.dbvolution.exceptions.UnexpectedNumberOfRowsException;
import nz.co.gregs.dbvolution.internal.DBRowWrapperFactory;
import nz.co.gregs.dbvolution.internal.PropertyWrapper;

/**
 *
 * @author gregory.graham
 */
public abstract class DBDatabase {

    private String driverName = "";
    private String jdbcURL = "";
    private String username = "";
    private String password = null;
    private DataSource dataSource = null;
    private boolean printSQLBeforeExecuting;
    private boolean isInATransaction;
    private DBTransactionStatement transactionStatement;
    private final DBDefinition definition;
    private String databaseName;
    private boolean batchIfPossible = true;
    final DBRowWrapperFactory wrapperFactory = new DBRowWrapperFactory();

    /**
     *
     * @param definition
     * @param ds
     */
    public DBDatabase(DBDefinition definition, DataSource ds) {
        this.definition = definition;
        this.dataSource = ds;
    }

    /**
     *
     * @param definition
     * @param driverName
     * @param jdbcURL
     * @param username
     * @param password
     */
    public DBDatabase(DBDefinition definition, String driverName, String jdbcURL, String username, String password) {
        this.definition = definition;
        this.driverName = driverName;
        this.jdbcURL = jdbcURL;
        this.password = password;
        this.username = username;
    }

    private DBTransactionStatement getDBTransactionStatement() {
        final DBStatement dbStatement = getDBStatement();
        if (dbStatement instanceof DBTransactionStatement) {
            return (DBTransactionStatement) dbStatement;
        } else {
            return new DBTransactionStatement(this, dbStatement);
        }
    }

    /**
     *
     * @return
     */
    public synchronized DBStatement getDBStatement() {
        Connection connection;
        DBStatement statement;
        if (isInATransaction) {
            statement = this.transactionStatement;
        } else {
            if (this.dataSource == null) {
                try {
                    // load the driver
                    Class.forName(getDriverName());
                } catch (ClassNotFoundException noDriver) {
                    throw new RuntimeException("No Driver Found: please check the driver name is correct and the appropriate libaries have been supplied: DRIVERNAME=" + getDriverName(), noDriver);
                }
                try {
                    connection = DriverManager.getConnection(getJdbcURL(), getUsername(), getPassword());
                } catch (SQLException noConnection) {
                    throw new RuntimeException("Connection Not Established: please check the database URL, username, and password, and that the appropriate libaries have been supplied: URL=" + getJdbcURL() + " USERNAME=" + getUsername(), noConnection);
                }
            } else {
                try {
                    connection = dataSource.getConnection();
                } catch (SQLException noConnection) {
                    throw new RuntimeException("Connection Not Established using the DataSource: please check the datasource - " + dataSource.toString(), noConnection);
                }
            }
            try {
                statement = new DBStatement(this, connection.createStatement());
            } catch (SQLException noConnection) {
                throw new RuntimeException("Unable to create a Statement: please check the database URL, username, and password, and that the appropriate libaries have been supplied: URL=" + getJdbcURL() + " USERNAME=" + getUsername(), noConnection);
            }
        }
        return statement;
    }

    /**
     *
     * Convenience method to simplify switching from READONLY to COMMITTED
     * transaction
     *
     * @param <V>
     * @param dbTransaction
     * @param commit
     * @return
     * @throws SQLException
     * @throws Exception
     */
    synchronized public <V> V doTransaction(DBTransaction<V> dbTransaction, Boolean commit) throws SQLException, Exception {
        if (commit) {
            return doTransaction(dbTransaction);
        } else {
            return doReadOnlyTransaction(dbTransaction);
        }
    }

    /**
     *
     * Inserts DBRows and lists of DBRows into the correct tables automatically
     *
     * @param objs
     * @throws SQLException
     */
    public DBActionList insert(Object... objs) throws SQLException {
        DBActionList changes = new DBActionList();
        for (Object obj : objs) {
            if (obj instanceof List) {
                List<?> list = (List<?>) obj;
                if (list.size() > 0 && list.get(0) instanceof DBRow) {
                    @SuppressWarnings("unchecked")
                    List<DBRow> rowList = (List<DBRow>) list;
                    for (DBRow row : rowList) {
                        changes.addAll(this.getDBTable(row).insert(row));
                    }
                }
            } else if (obj instanceof DBRow) {
                DBRow row = (DBRow) obj;
                changes.addAll(this.getDBTable(row).insert(row));
            }
        }
        return changes;
    }

    /**
     *
     * Deletes DBRows and lists of DBRows from the correct tables automatically
     *
     * @param objs
     * @throws SQLException
     */
    public DBActionList delete(Object... objs) throws SQLException {
        DBActionList changes =new DBActionList();
        for (Object obj : objs) {
            if (obj instanceof List) {
                List<?> list = (List<?>) obj;
                if (list.size() > 0 && list.get(0) instanceof DBRow) {
                    @SuppressWarnings("unchecked")
                    List<DBRow> rowList = (List<DBRow>) list;
                    for (DBRow row : rowList) {
                        changes.addAll(this.getDBTable(row).delete(row));
                    }
                }
            } else if (obj instanceof DBRow) {
                DBRow row = (DBRow) obj;
                changes.addAll(this.getDBTable(row).delete(row));
            }
        }
        return changes;
    }

    /**
     *
     * Updates DBRows and lists of DBRows in the correct tables automatically
     *
     * @param objs
     * @throws SQLException
     */
    public void updateRowsAndListsOfRows(Object... objs) throws SQLException {
        for (Object obj : objs) {
            if (obj instanceof List) {
                List<?> list = (List<?>) obj;
                if (list.size() > 0 && list.get(0) instanceof DBRow) {
                    @SuppressWarnings("unchecked")
                    List<DBRow> rowList = (List<DBRow>) list;
                    for (DBRow row : rowList) {
                        this.getDBTable(row).update(row);
                    }
                }
            } else if (obj instanceof DBRow) {
                DBRow row = (DBRow) obj;
                this.getDBTable(row).update(row);
            }
        }
    }

    public void update(DBRow row) throws SQLException {
        this.getDBTable(row).update(row);
    }

    public void update(List<DBRow> list) throws SQLException {
        if (list.size() > 0 && list.get(0) instanceof DBRow) {
            for (DBRow row : list) {
                this.update(row);
            }
        }
    }

    public void update(DBRow[] list) throws SQLException {
        if (list.length > 0) {
            for (DBRow list1 : list) {
                this.update(list1);
            }
        }
    }

    /**
     *
     * Automatically selects the correct table and returns the selected rows as
     * a list
     *
     * @param <R>
     * @param row
     * @return 
     * @throws SQLException
     */
    public <R extends DBRow> List<R> get(R row) throws SQLException {
        DBTable<R> dbTable = getDBTable(row);
        return dbTable.getRowsByExample(row).toList();
    }

    /**
     *
     * Automatically selects the correct table and returns the selected rows as
     * a list
     *
     * @param <R>
     * @param expectedNumberOfRows
     * @param row
     * @return
     * @throws SQLException
     * @throws UnexpectedNumberOfRowsException
     */
    public <R extends DBRow> List<R> get(Long expectedNumberOfRows, R row) throws SQLException, UnexpectedNumberOfRowsException {
        if (expectedNumberOfRows == null) {
            return get(row);
        } else {
            return getDBTable(row).getRowsByExample(row, expectedNumberOfRows.intValue()).toList();
        }
    }

    /**
     *
     * creates a query and fetches the rows automatically
     *
     * @param rows
     * @throws SQLException
     */
    public List<DBQueryRow> get(DBRow... rows) throws SQLException {
        DBQuery dbQuery = getDBQuery(rows);
        return dbQuery.getAllRows();
    }

    /**
     *
     * Convenience method to print the rows from get(DBRow...)
     *
     * @param rows
     */
    public void print(List<?> rows) {
        for (Object row : rows) {
            System.out.println(row.toString());
        }
    }

    /**
     *
     * creates a query and fetches the rows automatically
     *
     * @param rows
     * @throws SQLException
     */
    public List<DBQueryRow> get(Long expectedNumberOfRows, DBRow... rows) throws SQLException, UnexpectedNumberOfRowsException {
        if (expectedNumberOfRows == null) {
            return get(rows);
        } else {
            return getDBQuery(rows).getAllRows(expectedNumberOfRows);
        }
    }

    /**
     *
     * @param <V>
     * @param dbTransaction
     * @return
     * @throws SQLException
     * @throws Exception
     */
    synchronized public <V> V doTransaction(DBTransaction<V> dbTransaction) throws SQLException, Exception {
        V returnValues = null;
        Connection connection;
        this.transactionStatement = getDBTransactionStatement();
        try {
            this.isInATransaction = true;
            connection = transactionStatement.getConnection();
            connection.setAutoCommit(false);
            try {
                returnValues = dbTransaction.doTransaction(this);
                connection.commit();
                System.err.println("Transaction Successful: Commit Performed");
                connection.setAutoCommit(true);
            } catch (Exception ex) {
                connection.rollback();
                System.err.println("Exception Occurred: ROLLBACK Performed");
                throw ex;
            } finally {
                connection.setAutoCommit(true);
                connection.close();
            }
        } finally {
            this.transactionStatement.transactionFinished();
            this.isInATransaction = false;
            transactionStatement = null;
        }
        return returnValues;
    }

    /**
     *
     * @param <V>
     * @param dbTransaction
     * @return
     * @throws SQLException
     * @throws Exception
     */
    synchronized public <V> V doReadOnlyTransaction(DBTransaction<V> dbTransaction) throws SQLException, Exception {
        Connection connection;
        V returnValues = null;
        boolean wasReadOnly = false;
        boolean wasAutoCommit = true;

        this.transactionStatement = getDBTransactionStatement();
        try {
            this.isInATransaction = true;

            connection = transactionStatement.getConnection();
            wasReadOnly = connection.isReadOnly();
            wasAutoCommit = connection.getAutoCommit();

            connection.setAutoCommit(false);
            try {
                returnValues = dbTransaction.doTransaction(this);
                connection.rollback();
                System.err.println("Transaction Successful: ROLLBACK Performed");
            } catch (Exception ex) {
                connection.rollback();
                System.err.println("Exception Occurred: ROLLBACK Performed");
                throw ex;
            } finally {
                connection.setAutoCommit(wasAutoCommit);
                connection.close();
            }
        } finally {
            this.transactionStatement.transactionFinished();
            this.isInATransaction = false;
            transactionStatement = null;
        }
        return returnValues;
    }

    /**
     * Convenience method to run a DBScript on this database
     *
     * equivalent to script.implement(this);
     *
     * @param script
     * @return
     * @throws Exception
     */
    public List<String> implement(DBScript script) throws Exception {
        return script.implement(this);
    }

    /**
     *
     * Convenience method to test a DBScript on this database
     *
     * equivalent to script.test(this);
     *
     * @param script
     * @return
     * @throws Exception
     */
    public List<String> test(DBScript script) throws Exception {
        return script.test(this);
    }

    /**
     * @return the driverName
     */
    public String getDriverName() {
        return driverName;
    }

    /**
     * @return the jdbcURL
     */
    public String getJdbcURL() {
        return jdbcURL;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     *
     * @param <R>
     * @param example
     * @return
     */
    public <R extends DBRow> DBTable<R> getDBTable(R example) {
        return DBTable.getInstance(this, example);
    }

    /**
     *
     * @param examples
     * @return
     */
    public DBQuery getDBQuery(DBRow... examples) {
        return DBQuery.getInstance(this, examples);
    }

    public void setPrintSQLBeforeExecuting(boolean b) {
        printSQLBeforeExecuting = b;
    }

    /**
     * @return the printSQLBeforeExecuting
     */
    public boolean isPrintSQLBeforeExecuting() {
        return printSQLBeforeExecuting;
    }

    public void printSQLIfRequested(String sqlString) {
        printSQLIfRequested(sqlString, System.out);
    }

    protected void printSQLIfRequested(String sqlString, PrintStream out) {
        if (printSQLBeforeExecuting) {
            out.println(sqlString);
        }
    }

    /**
     *
     * @param <TR>
     * @param newTableRow
     * @return
     * @throws SQLException
     */
    public <TR extends DBRow> void createTable(TR newTableRow) throws SQLException {
        StringBuilder sqlScript = new StringBuilder();
        List<PropertyWrapper> pkFields = new ArrayList<PropertyWrapper>();
        String lineSeparator = System.getProperty("line.separator");
        // table name

        sqlScript.append(definition.getCreateTableStart()).append(definition.formatTableName(newTableRow.getTableName())).append(definition.getCreateTableColumnsStart()).append(lineSeparator);

        // columns
        String sep = "";
        String nextSep = definition.getCreateTableColumnsSeparator();
        List<PropertyWrapper> fields = newTableRow.getPropertyWrappers();
        for (PropertyWrapper field : fields) {
            if (field.isColumn()) {
                QueryableDatatype qdt = field.getQueryableDatatype();
                if (qdt == null) {
                    // this is inefficient since the new qdt will be thrown away,
                    // but it's only for creating tables, which doesn't happen often.
                    qdt = QueryableDatatype.getQueryableDatatypeInstance(field.type());
                }

                String colName = field.columnName();
                sqlScript
                        .append(sep)
                        .append(definition.formatColumnName(colName))
                        .append(definition.getCreateTableColumnsNameAndTypeSeparator())
                        .append(definition.getSQLTypeOfDBDatatype(qdt));
                sep = nextSep + lineSeparator;

                if (field.isPrimaryKey()) {
                    pkFields.add(field);
                }
            }
        }

        // primary keys
        String pkStart = lineSeparator + definition.getCreateTablePrimaryKeyClauseStart();
        String pkMiddle = definition.getCreateTablePrimaryKeyClauseMiddle();
        String pkEnd = definition.getCreateTablePrimaryKeyClauseEnd() + lineSeparator;
        String pkSep = pkStart;
        for (PropertyWrapper field : pkFields) {
            sqlScript.append(pkSep).append(definition.formatColumnName(field.columnName()));
            pkSep = pkMiddle;
        }
        if (!pkSep.equalsIgnoreCase(pkStart)) {
            sqlScript.append(pkEnd);
        }

        //finish
        sqlScript.append(definition.getCreateTableColumnsEnd()).append(lineSeparator).append(definition.endSQLStatement());
        String sqlString = sqlScript.toString();
        printSQLIfRequested(sqlString);
        getDBStatement().execute(sqlString);
    }

    public <TR extends DBRow> void dropTable(TR tableRow) throws SQLException {
        StringBuilder sqlScript = new StringBuilder();

        sqlScript.append(definition.getDropTableStart()).append(definition.formatTableName(tableRow.getTableName())).append(definition.endSQLStatement());
        String sqlString = sqlScript.toString();
        printSQLIfRequested(sqlString);
        getDBStatement().execute(sqlString);
    }

    /**
     *
     * The easy way to drop a table that might not exist.
     *
     * @param <TR>
     * @param tableRow
     */
    @SuppressWarnings("empty-statement")
    public <TR extends DBRow> void dropTableNoExceptions(TR tableRow) {
        try {
            this.dropTable(tableRow);
        } catch (Exception exp) {
            ;
        }
    }

    public DBDefinition getDefinition() {
        return definition;
    }

    public boolean willCreateBlankQuery(DBRow row) {
        return row.willCreateBlankQuery(this);
    }

    public void dropDatabase() throws Exception {
        ;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    protected String setDatabaseName(String databaseName) {
        return this.databaseName = databaseName;
    }

    public boolean batchSQLStatementsWhenPossible() {
        return batchIfPossible;
    }

    public void setBatchSQLStatementsWhenPossible(boolean batchSQLStatementsWhenPossible) {
        batchIfPossible = batchSQLStatementsWhenPossible;
    }
}