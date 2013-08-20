/*
 * Copyright Error: on line 4, column 29 in Templates/Licenses/license-apache20.txt
 Expecting a date here, found: 6/06/2013 gregorygraham.
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

import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import nz.co.gregs.dbvolution.annotations.DBColumn;
import nz.co.gregs.dbvolution.annotations.DBForeignKey;
import nz.co.gregs.dbvolution.databases.DBDatabase;

/**
 *
 * @author gregorygraham
 */
public class DBQuery {

    DBDatabase database;
    private List<DBRow> queryTables;
    private List<DBQueryRow> results;
    private Map<Class<?>, Map<String, DBRow>> existingInstances = new HashMap<Class<?>, Map<String, DBRow>>();
    private Long rowLimit;
    private DBRow[] sortBase;
    private QueryableDatatype[] sortOrder;
    private String resultSQL;

    private DBQuery(DBDatabase database) {
        this.queryTables = new ArrayList<DBRow>();
        this.database = database;
        this.results = null;
        this.resultSQL = null;
    }

//    private DBQuery(DBDatabase database, DBRow... examples) {
//        this(database);
//        for (DBRow example : examples) {
//            this.add(example);
//        }
//    }
    public static DBQuery getInstance(DBDatabase database, DBRow... examples) {
        DBQuery dbQuery = new DBQuery(database);
        for (DBRow example : examples) {
            dbQuery.add(example);
        }
        return dbQuery;
    }

    /**
     *
     * Add a table to the query
     *
     * @param table
     */
    public DBQuery add(DBRow table) {
        queryTables.add(table);
        results = null;
        resultSQL = null;
        return this;
    }

    public String getSQLForQuery() throws SQLException {
        return getSQLForQuery(null);
    }

    private String getSQLForQuery(String providedSelectClause) throws SQLException {
        StringBuilder selectClause = new StringBuilder().append(database.beginSelectStatement());
        StringBuilder fromClause = new StringBuilder().append(database.beginFromClause());
        StringBuilder whereClause = new StringBuilder().append(database.beginWhereClause()).append(database.getTrueOperation());
        ArrayList<DBRow> otherTables = new ArrayList<DBRow>();
        String lineSep = System.getProperty("line.separator");

        if (rowLimit != null) {
            selectClause.append(database.getTopClause(rowLimit));
        }

        String separator = "";
        String colSep = database.getStartingSelectSubClauseSeparator();
        String tableName;

        for (DBRow tabRow : queryTables) {
            otherTables.clear();
            otherTables.addAll(queryTables);
            otherTables.remove(tabRow);
            tableName = tabRow.getTableName();

            if (providedSelectClause == null) {
                List<String> columnNames = tabRow.getColumnNames();
                for (String columnName : columnNames) {
                    String formattedColumnName = database.formatTableAndColumnNameForSelectClause(tableName, columnName);
                    selectClause.append(colSep).append(formattedColumnName);
                    colSep = database.getSubsequentSelectSubClauseSeparator() + lineSep;
                }
            } else {
                selectClause = new StringBuilder(providedSelectClause);
            }
            fromClause.append(separator).append(tableName);
//            tabRow.setDatabase(database);
            String tabRowCriteria = tabRow.getWhereClause(database);
            if (tabRowCriteria != null && !tabRowCriteria.isEmpty()) {
                whereClause.append(lineSep).append(tabRowCriteria);
            }
            List<String> adHocRelationshipSQL = tabRow.getAdHocRelationshipSQL();
            for (String sql : adHocRelationshipSQL) {
                whereClause.append(sql);
            }

            for (DBRow otherTab : otherTables) {
                Map<DBForeignKey, DBColumn> fks = otherTab.getForeignKeys();
                for (DBForeignKey fk : fks.keySet()) {
                    //tabRow.setDatabase(database);
                    String formattedPK = database.formatTableAndColumnName(tableName, tabRow.getPrimaryKeyName());
                    Class<? extends DBRow> pkClass = fk.value();
                    DBRow fkReferencesTable = DBRow.getDBRow(pkClass);
                    String fkReferencesColumn = database.formatTableAndColumnName(fkReferencesTable.getTableName(), fkReferencesTable.getPrimaryKeyName());
                    if (formattedPK.equalsIgnoreCase(fkReferencesColumn)) {
                        String fkColumnName = fks.get(fk).value();
                        String formattedFK = database.formatTableAndColumnName(otherTab.getTableName(), fkColumnName);
                        whereClause
                                .append(lineSep)
                                .append(database.beginAndLine())
                                .append(formattedPK)
                                .append(database.getEqualsComparator())
                                .append(formattedFK);
                    }
                }
            }

            separator = ", " + lineSep;
            otherTables.addAll(queryTables);
        }
        final String sqlString =
                selectClause.append(lineSep)
                .append(fromClause).append(lineSep)
                .append(whereClause).append(lineSep)
                .append(getOrderByClause()).append(lineSep)
                .append(database.endSQLStatement())
                .toString();
        if (database.isPrintSQLBeforeExecuting()) {
            System.out.println(sqlString);
        }

        return sqlString;
    }

    public String getSQLForCount() throws SQLException {
        return getSQLForQuery(database.beginSelectStatement() + database.countStarClause());
    }

    public List<DBQueryRow> getAllRows() throws SQLException {
        results = new ArrayList<DBQueryRow>();
        resultSQL = this.getSQLForQuery();
        DBQueryRow queryRow;

        Statement dbStatement = database.getDBStatement();
        ResultSet resultSet = dbStatement.executeQuery(resultSQL);
        while (resultSet.next()) {
            queryRow = new DBQueryRow();
            for (DBRow tableRow : queryTables) {
                DBRow newInstance = DBRow.getDBRow(tableRow.getClass());
                //newInstance.setDatabase(database);
                Map<String, QueryableDatatype> columnsAndQueryableDatatypes = newInstance.getColumnsAndQueryableDatatypes();
                for (String columnName : columnsAndQueryableDatatypes.keySet()) {
                    QueryableDatatype qdt = columnsAndQueryableDatatypes.get(columnName);
                    String fullColumnName = database.formatColumnNameForResultSet(tableRow.getTableName(), columnName);
                    qdt.setFromResultSet(resultSet, fullColumnName);
//                    String stringOfValue = resultSet.getString(fullColumnName);
//                    qdt.isLiterally(stringOfValue);
                }
                Map<String, DBRow> existingInstancesOfThisTableRow = existingInstances.get(tableRow.getClass());
                if (existingInstancesOfThisTableRow == null) {
                    existingInstancesOfThisTableRow = new HashMap<String, DBRow>();
                    existingInstances.put(tableRow.getClass(), existingInstancesOfThisTableRow);
                }
                DBRow existingInstance = existingInstancesOfThisTableRow.get(newInstance.getPrimaryKeySQLStringValue(database));
                if (existingInstance == null) {
                    existingInstance = newInstance;
                    existingInstancesOfThisTableRow.put(existingInstance.getPrimaryKeySQLStringValue(database), existingInstance);
                }
                queryRow.put(existingInstance.getClass(), existingInstance);
            }
            results.add(queryRow);
        }
        return results;
    }

    /**
     *
     * Expects there to be exactly one(1) object of the exemplar type.
     *
     * An UnexpectedNumberOfRowsException is thrown if there is zero or more
     * than one row.
     *
     * @param <R>
     * @param exemplar
     * @return
     * @throws SQLException
     * @throws UnexpectedNumberOfRowsException
     */
    public <R extends DBRow> R getOnlyInstanceOf(R exemplar) throws SQLException, UnexpectedNumberOfRowsException {
        List<R> allInstancesFound = getAllInstancesOf(exemplar, 1);
        return allInstancesFound.get(0);
    }

    /**
     *
     * @param <R>: A Java Object that extends DBRow
     * @param exemplar: The DBRow class that you would like returned.
     * @param expected: The expected number of rows, an exception will be thrown
     * if this expectation is not met.
     * @return
     * @throws SQLException
     * @throws UnexpectedNumberOfRowsException
     */
    public <R extends DBRow> List<R> getAllInstancesOf(R exemplar, int expected) throws SQLException, UnexpectedNumberOfRowsException {
        List<R> allInstancesFound = getAllInstancesOf(exemplar);
        final int actual = allInstancesFound.size();
        if (actual > expected) {
            throw new UnexpectedNumberOfRowsException(expected, actual, "Too Many Results: expected " + expected + ", actually got " + actual);
        } else if (actual < expected) {
            throw new UnexpectedNumberOfRowsException(expected, actual, "Too Few Results: expected " + expected + ", actually got " + actual);
        } else {
            return allInstancesFound;
        }
    }

    private boolean needsResults() {
        try {
            return results == null
                    || results.isEmpty()
                    || resultSQL == null
                    || !resultSQL.equals(getSQLForQuery());
        } catch (SQLException ex) {
            return true;
        }
    }

    public <R extends DBRow> List<R> getAllInstancesOf(R exemplar) throws SQLException {
        List<R> arrayList = new ArrayList<R>();
        if (this.needsResults()) {
            getAllRows();
        }
        if (!results.isEmpty()) {
            for (DBQueryRow row : results) {
                final R found = row.get(exemplar);
                if (found != null) { // in case there are no items of the exemplar
                    if (!arrayList.contains(found)) {
                        arrayList.add(found);
                    }
                }
            }
        }
        return arrayList;
    }

    /**
     *
     * What is this for????
     *
     * @param <R>
     * @param exemplar
     * @return
     * @throws SQLException
     * @deprecated
     */
    @Deprecated
    public <R extends DBRow> List<DBRow> getAllInstancesOfExemplarAsDBRow(R exemplar) throws SQLException {
        HashSet<DBRow> objList = new HashSet<DBRow>();
        ArrayList<DBRow> arrayList = new ArrayList<DBRow>();
        if (this.needsResults()) {
            getAllRows();
        }
        if (!results.isEmpty()) {
            for (DBQueryRow row : results) {
                final DBRow found = row.get(exemplar);
                if (found != null) { // in case there are no items of the exemplar
                    objList.add(found);
                }
            }

            arrayList.addAll(objList);
        }
        return arrayList;
    }

    /**
     * Convenience method to print all the rows in the current collection
     * Equivalent to: printAll(System.out);
     *
     */
    public void print() throws SQLException {
        print(System.out);
    }

    /**
     * Fast way to print the results
     *
     * myTable.printRows(System.err);
     *
     * @param ps
     */
    public void print(PrintStream ps) throws SQLException {
        if (needsResults()) {
            this.getAllRows();
        }

        for (DBQueryRow row : this.results) {
            for (DBRow tab : this.queryTables) {
                DBRow rowPart = row.get(tab);
                String rowPartStr = rowPart.toString();
                ps.print(rowPartStr);
            }
            ps.println();
        }
    }

    /**
     * Fast way to print the results
     *
     * myTable.printRows(System.err);
     *
     * @param printStream
     */
    public void printAllDataColumns(PrintStream printStream) throws SQLException {
        if (needsResults()) {
            this.getAllRows();
        }

        for (DBQueryRow row : this.results) {
            for (DBRow tab : this.queryTables) {
                DBRow rowPart = row.get(tab);
                String rowPartStr = rowPart.toStringMinusFKs();
                printStream.print(rowPartStr);
            }
            printStream.println();
        }
    }

    /**
     * Fast way to print the results
     *
     * myTable.printAllPrimaryKeys(System.err);
     *
     * @param ps
     */
    public void printAllPrimaryKeys(PrintStream ps) throws SQLException {
        if (needsResults()) {
            this.getAllRows();
        }

        for (DBQueryRow row : this.results) {
            for (DBRow tab : this.queryTables) {
                DBRow rowPart = row.get(tab);
                String rowPartStr = rowPart.getPrimaryKeySQLStringValue(database);
                ps.print(" " + rowPart.getPrimaryKeyName() + ": " + rowPartStr);
            }
            ps.println();
        }
    }

    public DBQuery clear() {
        this.queryTables.clear();
        results = null;
        return this;
    }

    public Long count() throws SQLException {
        if (results != null) {
            return new Long(results.size());
        } else {
            Long result = 0L;

            Statement dbStatement = database.getDBStatement();
            ResultSet resultSet = dbStatement.executeQuery(this.getSQLForCount());
            while (resultSet.next()) {
                result = resultSet.getLong(1);
            }
            return result;
        }
    }

    public boolean willCreateBlankQuery() {
        boolean willCreateBlankQuery = true;
        for (DBRow table : queryTables) {
            willCreateBlankQuery = willCreateBlankQuery && table.willCreateBlankQuery(this.database);
        }
        return willCreateBlankQuery;
    }

    public void setRowLimit(int i) {
        rowLimit = new Long(i);
    }

    public void clearRowLimit() {
        rowLimit = null;
    }

    public void setSortOrder(DBRow[] baseRows, QueryableDatatype... baseRowColumns) {
        sortBase = baseRows;
        sortOrder = baseRowColumns;
        results = null;
    }

    public void clearSortOrder() {
        sortOrder = null;
    }

    private String getOrderByClause() {
        if (sortOrder != null) {
            StringBuilder orderByClause = new StringBuilder(database.beginOrderByClause());
            String sortSeparator = database.getStartingOrderByClauseSeparator();
            for (QueryableDatatype qdt : sortOrder) {
                final String dbColumnName = DBRow.getTableAndColumnName(sortBase, qdt);
                if (dbColumnName != null) {
                    orderByClause.append(sortSeparator).append(dbColumnName).append(database.getOrderByDirectionClause(qdt.getSortOrder()));
                    sortSeparator = database.getSubsequentOrderByClauseSeparator();
                }
            }
            orderByClause.append(database.endOrderByClause());
            return orderByClause.toString();
        }
        return "";
    }
}
