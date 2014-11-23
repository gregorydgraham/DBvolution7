package nz.co.gregs.dbvolution;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nz.co.gregs.dbvolution.annotations.DBSelectQuery;
import nz.co.gregs.dbvolution.annotations.DBColumn;
import nz.co.gregs.dbvolution.annotations.DBPrimaryKey;
import nz.co.gregs.dbvolution.databases.DBDatabase;

/**
 *
 * @param <E>
 * @author gregory.graham
 */
public class DBTable<E extends DBRow> {

    private static final long serialVersionUID = 1L;
    private static boolean printSQLBeforeExecuting = false;
    private DBDatabase theDatabase = null;
    E dummy;
    private java.util.ArrayList<E> listOfRows = new java.util.ArrayList<E>();

    /**
     * With a DBDatabase subclass it's easier
     *
     * @param myDatabase
     * @param dummyObject
     */
    public DBTable(DBDatabase myDatabase, E dummyObject) {
        this.theDatabase = myDatabase;
        dummy = dummyObject;
    }

    /**
     * Set this to TRUE to see the actual SQL that is executed.
     *
     * @param aPrintSQLBeforeExecuting the printSQLBeforeExecuting to set
     */
    public static void setPrintSQLBeforeExecuting(boolean aPrintSQLBeforeExecuting) {
        printSQLBeforeExecuting = aPrintSQLBeforeExecuting;
    }

    private String getDBColumnName(Field field) {
        String columnName = "";

        if (field.isAnnotationPresent(DBColumn.class)) {
            DBColumn annotation = field.getAnnotation(DBColumn.class);
            columnName = annotation.value();
            if (columnName
                    == null || columnName.isEmpty()) {
                columnName = field.getName();
            }
        }
        return columnName;
    }

    private String getAllFieldsForSelect() {
        StringBuilder allFields = new StringBuilder();
        @SuppressWarnings("unchecked")
        Class<E> thisClass = (Class<E>) dummy.getClass();
        Field[] fields = thisClass.getDeclaredFields();
        String separator = "";
        for (Field field : fields) {
            if (field.isAnnotationPresent(DBColumn.class)) {
                allFields.append(separator).append(" ").append(getDBColumnName(field));
                separator = ",";
            }
        }
        return allFields.toString();
    }

    private String getSelectStatement() {
        StringBuilder selectStatement = new StringBuilder();
        DBSelectQuery selectQueryAnnotation = dummy.getClass().getAnnotation(DBSelectQuery.class);
        if (selectQueryAnnotation != null) {
            selectStatement.append(selectQueryAnnotation.value());
        } else {
            selectStatement.append("select ");
            selectStatement.append(getAllFieldsForSelect()).append(" from ").append(dummy.getTableName()).append(";");
        }

        return selectStatement.toString();
    }

    public String getSelectStatementForWhereClause() {
        StringBuilder selectStatement = new StringBuilder();
        DBSelectQuery selectQueryAnnotation = dummy.getClass().getAnnotation(DBSelectQuery.class);
        if (selectQueryAnnotation != null) {
            selectStatement
                    .append(selectQueryAnnotation.value())
                    .append(theDatabase.beginWhereClause())
                    .append(theDatabase.getTrueOperation());
        } else {
            selectStatement.append(theDatabase.beginSelectStatement());

            selectStatement
                    .append(getAllFieldsForSelect())
                    .append(theDatabase.beginFromClause())
                    .append(dummy.getTableName())
                    .append(theDatabase.beginWhereClause())
                    .append(theDatabase.getTrueOperation());
        }
        return selectStatement.toString();
    }

    /**
     * Use this carefully as it does what it says on the label: Gets All Rows of
     * the table from the database.
     *
     * If your database has umpteen gazillion rows in VeryBig table and you call
     * this, don't come crying to me.
     *
     * @throws SQLException
     */
    public DBTable<E> getAllRows() throws SQLException {
        this.listOfRows.clear();

        String selectStatement = this.getSelectStatement();

        if (printSQLBeforeExecuting || theDatabase.isPrintSQLBeforeExecuting()) {
            System.out.println(selectStatement);
        }

        Statement statement;
        ResultSet resultSet;
        statement = this.theDatabase.getDBStatement();
        try {
            boolean executed = statement.execute(selectStatement);
        } catch (SQLException noConnection) {
            throw new RuntimeException("Unable to create a Statement: please check the database URL, username, and password, and that the appropriate libaries have been supplied: URL=" + theDatabase.getJdbcURL() + " USERNAME=" + theDatabase.getUsername(), noConnection);
        }
        try {
            resultSet = statement.getResultSet();
        } catch (SQLException noConnection) {
            throw new RuntimeException("Unable to create a Statement: please check the database URL, username, and password, and that the appropriate libaries have been supplied: URL=" + theDatabase.getJdbcURL() + " USERNAME=" + theDatabase.getUsername(), noConnection);
        }
        addAllFields(this, resultSet);
        return this;
    }

    private void addAllFields(DBTable<E> dbTable, ResultSet resultSet) throws SQLException {
        ResultSetMetaData rsMeta = resultSet.getMetaData();
        Map<String, Integer> dbColumnNames = new HashMap<String, Integer>();
        for (int k = 1; k <= rsMeta.getColumnCount(); k++) {
            dbColumnNames.put(rsMeta.getColumnName(k), k);
        }

        while (resultSet.next()) {
            @SuppressWarnings("unchecked")
            E tableRow = (E) DBRow.getInstance(dummy.getClass());

            Field[] fields = tableRow.getClass().getDeclaredFields();



            for (Field field : fields) {
                if (field.isAnnotationPresent(DBColumn.class)) {
                    String dbColumnName = getDBColumnName(field);
                    int dbColumnIndex = dbColumnNames.get(theDatabase.formatColumnName(dbColumnName));

                    setObjectFieldValueToColumnValue(rsMeta, dbColumnIndex, field, tableRow, resultSet, dbColumnName);
                }
            }
            dbTable.listOfRows.add(tableRow);
        }
    }

    private void setObjectFieldValueToColumnValue(ResultSetMetaData rsMeta, int dbColumnIndex, Field field, DBRow tableRow, ResultSet resultSet, String dbColumnName) throws SQLException {
        QueryableDatatype qdt = tableRow.getQueryableValueOfField(field);
        int columnType = rsMeta.getColumnType(dbColumnIndex);
        switch (columnType) {
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.BINARY:
            case Types.BOOLEAN:
            case Types.ROWID:
            case Types.SMALLINT:
                Long aLong = resultSet.getLong(dbColumnName);
                qdt.isLiterally(aLong);
                break;
            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.NUMERIC:
            case Types.REAL:
                Double aDouble = resultSet.getDouble(dbColumnName);
                qdt.isLiterally(aDouble);
                break;
            case Types.VARCHAR:
            case Types.CHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.CLOB:
            case Types.NCLOB:
            case Types.LONGNVARCHAR:
            case Types.LONGVARCHAR:
                String string = resultSet.getString(dbColumnName);
                qdt.isLiterally(string);
                break;
            case Types.DATE:
            case Types.TIME:
                Date date = resultSet.getDate(dbColumnName);
                qdt.isLiterally(date);
                break;
            case Types.TIMESTAMP:
                Timestamp timestamp = resultSet.getTimestamp(dbColumnName);
                qdt.isLiterally(timestamp);
                break;
            case Types.VARBINARY:
            case Types.JAVA_OBJECT:
            case Types.LONGVARBINARY:
                Object obj = resultSet.getObject(dbColumnName);
                qdt.isLiterally(obj);
                break;
            default:
                throw new RuntimeException("Unknown Java SQL Type: " + rsMeta.getColumnType(dbColumnIndex));
        }
    }

    private String getPrimaryKeyColumn() {
        String pkColumn = "";
        @SuppressWarnings("unchecked")
        Class<E> thisClass = (Class<E>) dummy.getClass();
        Field[] fields = thisClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(DBPrimaryKey.class)) {
                pkColumn = this.getDBColumnName(field);
            }
        }
        if (pkColumn.isEmpty()) {
            throw new RuntimeException("Primary Key Field Not Defined: Please define the primary key field of " + thisClass.getSimpleName() + " using the @DBPrimaryKey annotation.");
        } else {
            return pkColumn;
        }
    }

    private String escapeSingleQuotes(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("'", "''").replace("\\", "\\\\");
    }

    private DBTable<E> getRows(String whereClause) throws SQLException {
        this.listOfRows.clear();
        String selectStatement = this.getSelectStatementForWhereClause() + whereClause + ";";
        if (printSQLBeforeExecuting || theDatabase.isPrintSQLBeforeExecuting()) {
            System.out.println(selectStatement);
        }

        Statement statement = theDatabase.getDBStatement();
        boolean executed = statement.execute(selectStatement);
        ResultSet resultSet = statement.getResultSet();

        addAllFields(this, resultSet);
        return this;
    }

    /**
     * Retrieves the row (or rows in a bad database) that has the specified
     * primary key The primary key column is identified by the
     *
     * @DBPrimaryKey annotation in the TableRow subclass
     *
     * @param pkValue
     * @return
     * @throws SQLException
     */
    public DBTable<E> getRowsByPrimaryKey(Object pkValue) throws SQLException {
        String whereClause = " and " + getPrimaryKeyColumn() + " = '" + escapeSingleQuotes(pkValue.toString()) + "'";
        this.getRows(whereClause);
        return this;
    }

    public DBTable<E> getRowsByPrimaryKey(Number pkValue) throws SQLException {
        String whereClause = " and " + getPrimaryKeyColumn() + " = " + pkValue + " ";
        this.getRows(whereClause);
        return this;
    }

    public DBTable<E> getRowsByPrimaryKey(Date pkValue) throws SQLException {
        String whereClause = " and " + getPrimaryKeyColumn() + " = " + this.theDatabase.getDateFormattedForQuery(pkValue) + " ";
        this.getRows(whereClause);
        return this;
    }

    /**
     * Using TableRow subclass as an example this method retrieves all the
     * appropriate records The following will retrieve all records from the
     * table where the Language column contains JAVA MyTableRow myExample = new
     * MyTableRow(); myExample.getLanguage.useLikeComparison("%JAVA%"); (new
     * DBTable<MyTableRow>()).getByExample(myExample);
     *
     * All columns defined within the TableRow subclass as QueryableDatatype
     * (e.g. DBNumber, DBString, etc) can be used in this way N.B. an actual
     *
     * @param queryTemplate
     * @return
     * @throws SQLException
     */
    public DBTable<E> getRowsByExample(E queryTemplate) throws SQLException {
        return getRows(getSQLForExample(queryTemplate));
    }

    public E getOnlyRowByExample(E queryTemplate) throws SQLException, UnexpectedNumberOfRowsException {
        return getRowsByExample(queryTemplate, 1).listOfRows.get(0);
    }

    public DBTable<E> getRowsByExample(E queryTemplate, int expectedNumberOfRows) throws SQLException, UnexpectedNumberOfRowsException {
        DBTable<E> rowsByExample = getRowsByExample(queryTemplate);
        int actualNumberOfRows = rowsByExample.toList().size();
        if (actualNumberOfRows == expectedNumberOfRows) {
            return rowsByExample;
        } else {
            throw new UnexpectedNumberOfRowsException(expectedNumberOfRows, actualNumberOfRows, "Unexpected Number Of Rows Detected: was expecting "
                    + expectedNumberOfRows
                    + ", found "
                    + actualNumberOfRows);
        }
    }

    /**
     * Returns the WHERE clause used by the getByExample method. Provided to aid
     * understanding and debugging.
     *
     * @param query
     * @return
     */
    public String getSQLForExample(E query) {
//        query.setDatabase(theDatabase);
        return query.getWhereClause(theDatabase);
    }

    /**
     * For the particularly hard queries, just provide the actual WHERE clause
     * you want to use.
     *
     * myExample.getLanguage.isLike("%JAVA%"); is similar to: getByRawSQL("and
     * language like '%JAVA%'");
     *
     * N.B. the starting AND is optional and avoid trailing semicolons
     *
     * @param sqlWhereClause
     * @return
     */
    public DBTable<E> getByRawSQL(String sqlWhereClause) throws SQLException {
        if (sqlWhereClause.toLowerCase().matches("^\\s*and\\s+.*")) {
            return getRows(sqlWhereClause.replaceAll("\\s*;\\s*$", ""));
        } else {
            return getRows(" AND " + sqlWhereClause.replaceAll("\\s*;\\s*$", ""));
        }
    }

    /**
     * Convenience method to print all the rows in the current collection
     * Equivalent to: printRows(System.out)
     *
     */
    public void printAllRows() {
        printAllRows(System.out);
    }

    /**
     * the same as printAllRows but allows you to specify the PrintStream
     * required
     *
     * myTable.printAllRows(System.err);
     *
     * @param ps
     */
    public void printAllRows(PrintStream ps) {
        for (E row : this.listOfRows) {
            ps.println(row);
        }
    }

//    private QueryableDatatype getQueryableDatatypeOfField(DBTableRow tableRow, Field field) {
//        QueryableDatatype qdt = null;
//        BeanInfo info = Introspector.getBeanInfo(tableRow.getClass());
//        PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
//        String fieldName = field.getName();
//        for (PropertyDescriptor pd : descriptors) {
//            String pdName = pd.getName();
//            if (pdName.equals(fieldName)) {
//                try {
//                    Method readMethod = pd.getReadMethod();
//                    if (readMethod == null) {
//                        Object possQDT = field.get(tableRow);
//                        if (possQDT instanceof QueryableDatatype) {
//                            return (QueryableDatatype) possQDT;
//                        } else {
//                            throw new RuntimeException("Unable To Access Read Method for \"" + field.getName() + "\" in class " + tableRow.getClass().getSimpleName());
//                        }
//                    } else {
//                        Object fieldQDT = readMethod.invoke(tableRow);
//                        if (fieldQDT instanceof QueryableDatatype) {
//                            qdt = (QueryableDatatype) fieldQDT;
//                            qdt.setDatabase(this.theDatabase);
//                        }
//                    }
//                    break;
//                } catch (IllegalAccessException illacc) {
//                    throw new RuntimeException("Could Not Access SET Method for " + tableRow.getClass().getSimpleName() + "." + field.getName() + ": Please ensure the SET method is public: " + tableRow.getClass().getSimpleName() + "." + field.getName());
//                }
//            }
//        }
//
//        if (qdt == null) {
//            Object possQDT = field.get(tableRow);
//            if (possQDT instanceof QueryableDatatype) {
//                return (QueryableDatatype) possQDT;
//            } else {
//                throw new RuntimeException("Unable Access QueryDatatype for \"" + field.getName() + "\" in class " + tableRow.getClass().getSimpleName());
//            }
//        }
//
//        return qdt;
//    }
    /**
     *
     * Returns the first row of the table, particularly helpful when you know
     * there is only one row
     *
     * @return
     */
    public E getFirstRow() {
        if (this.listOfRows.size() > 0) {
            return this.listOfRows.get(0);
        } else {
            return null;
        }
    }

    public E getOnlyRow() throws UnexpectedNumberOfRowsException {
        if (this.listOfRows.size() > 0) {
            return this.listOfRows.get(0);
        } else {
            throw new UnexpectedNumberOfRowsException(1, listOfRows.size(), "Unexpected Number Of Rows Detected: was expecting 1, found " + listOfRows.size());
        }
    }

    public void insert(E newRow) throws SQLException {
        ArrayList<E> arrayList = new ArrayList<E>();
        arrayList.add(newRow);
        insert(arrayList);
    }

    /**
     *
     * @param newRows
     * @throws SQLException
     */
    public void insert(List<E> newRows) throws SQLException {
        Statement statement = theDatabase.getDBStatement();
        List<String> allInserts = getSQLForInsert(newRows);
        for (String sql : allInserts) {
            if (printSQLBeforeExecuting || theDatabase.isPrintSQLBeforeExecuting()) {
                System.out.println(sql);
            }
            statement.addBatch(sql);
        }
        statement.executeBatch();
    }

    public String getSQLForInsert(E newRow) {
        ArrayList<E> arrayList = new ArrayList<E>();
        arrayList.add(newRow);
        return getSQLForInsert(arrayList).get(0);
    }

    /**
     *
     * @param newRows
     * @return
     */
    public List<String> getSQLForInsert(List<E> newRows) {
        List<String> allInserts = new ArrayList<String>();
        for (E row : newRows) {
//            row.setDatabase(theDatabase);
            String sql =
                    theDatabase.beginInsertLine()
                    + row.getTableName()
                    + theDatabase.beginInsertColumnList()
                    + this.getAllFieldsForSelect()
                    + theDatabase.endInsertColumnList()
                    + row.getValuesClause(theDatabase)
                    + theDatabase.endInsertLine();
            allInserts.add(sql);
        }
        return allInserts;
    }

    public void delete(E oldRow) throws SQLException {
        ArrayList<E> arrayList = new ArrayList<E>();
        arrayList.add(oldRow);
        delete(arrayList);
    }

    /**
     *
     * @param oldRows
     * @throws SQLException
     */
    public void delete(List<E> oldRows) throws SQLException {
        Statement statement = theDatabase.getDBStatement();
        List<String> allSQL = getSQLForDelete(oldRows);
        for (String sql : allSQL) {
            if (printSQLBeforeExecuting || theDatabase.isPrintSQLBeforeExecuting()) {
                System.out.println(sql);
            }
            statement.addBatch(sql);
        }
        statement.executeBatch();
    }

    /**
     *
     * Convenience method
     *
     * @param oldRow
     * @return
     */
    public String getSQLForDelete(E oldRow) {
        ArrayList<E> arrayList = new ArrayList<E>();
        arrayList.add(oldRow);
        return getSQLForDelete(arrayList).get(0);
    }

    /**
     *
     * @param oldRows
     * @return
     */
    public List<String> getSQLForDelete(List<E> oldRows) {
        List<String> allInserts = new ArrayList<String>();
        for (E row : oldRows) {
//            row.setDatabase(theDatabase);
            String sql =
                    theDatabase.beginDeleteLine()
                    + row.getTableName()
                    + theDatabase.beginWhereClause()
                    + this.getPrimaryKeyColumn()
                    + theDatabase.getEqualsComparator()
                    + row.getPrimaryKeySQLStringValue(theDatabase)
                    + theDatabase.endDeleteLine();
            allInserts.add(sql);
        }
        return allInserts;
    }

    public void update(E oldRow) throws SQLException {
        ArrayList<E> arrayList = new ArrayList<E>();
        arrayList.add(oldRow);
        update(arrayList);
    }

    public void update(List<E> oldRows) throws SQLException {
        Statement statement = theDatabase.getDBStatement();
        List<String> allSQL = getSQLForUpdate(oldRows);
        for (String sql : allSQL) {
            if (printSQLBeforeExecuting || theDatabase.isPrintSQLBeforeExecuting()) {
                System.out.println(sql);
            }
            statement.addBatch(sql);
        }
        statement.executeBatch();
    }

    /**
     *
     * Convenience method for getSQLForUpdate(List<E>)
     *
     * @param oldRow
     * @return
     */
    public List<String> getSQLForUpdate(E oldRow) {
        ArrayList<E> arrayList = new ArrayList<E>();
        arrayList.add(oldRow);
        return getSQLForUpdate(arrayList);
    }

    /**
     * Creates the SQL used to update the rows. Helpful debugging and mollifying
     * grumpy DBAs
     *
     *
     * @param oldRows
     * @return
     */
    public List<String> getSQLForUpdate(List<E> oldRows) {
        List<String> allSQL = new ArrayList<String>();
        for (E row : oldRows) {
//            row.setDatabase(theDatabase);
            String sql =
                    theDatabase.beginUpdateLine()
                    + theDatabase.formatTableName(row.getTableName())
                    + theDatabase.beginSetClause();
            sql = sql + row.getSetClause(theDatabase);
            sql = sql + theDatabase.beginWhereClause()
                    + theDatabase.formatColumnName(this.getPrimaryKeyColumn())
                    + theDatabase.getEqualsComparator()
                    + row.getPrimaryKeySQLStringValue(theDatabase)
                    + theDatabase.endDeleteLine();
            allSQL.add(sql);
        }

        return allSQL;
    }

    /**
     *
     * @param query
     * @param sqlWhereClause
     * @return
     */
    public String getWhereClauseWithExampleAndRawSQL(E query, String sqlWhereClause) {
        if (sqlWhereClause.toLowerCase().matches("^\\s*and\\s+.*")) {
            return getSQLForExample(query) + sqlWhereClause.replaceAll("\\s*;\\s*$", "");
        } else {
            return getSQLForExample(query) + " AND " + sqlWhereClause.replaceAll("\\s*;\\s*$", "");
        }
    }

    /**
     *
     * @return
     */
    public List<E> toList() {
        return new java.util.ArrayList<E>(listOfRows);
    }

    public List<Number> getPrimaryKeysAsNumber() {
        List<Number> primaryKeys = new ArrayList<Number>();
        for (E e : listOfRows) {
            primaryKeys.add(e.getPrimaryKeyLongValue());
        }
        return primaryKeys;
    }

    public List<String> getPrimaryKeysAsString() {
        List<String> primaryKeys = new ArrayList<String>();
        for (E e : listOfRows) {
            primaryKeys.add(e.getPrimaryKeyStringValue());
        }
        return primaryKeys;
    }
}