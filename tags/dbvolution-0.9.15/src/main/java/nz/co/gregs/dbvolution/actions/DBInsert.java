/*
 * Copyright 2013 gregorygraham.
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
package nz.co.gregs.dbvolution.actions;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import nz.co.gregs.dbvolution.DBDatabase;
import nz.co.gregs.dbvolution.DBRow;
import nz.co.gregs.dbvolution.databases.DBStatement;
import nz.co.gregs.dbvolution.databases.definitions.DBDefinition;
import nz.co.gregs.dbvolution.datatypes.DBLargeObject;
import nz.co.gregs.dbvolution.datatypes.QueryableDatatype;
import nz.co.gregs.dbvolution.internal.properties.PropertyWrapper;

/**
 *
 * @author gregorygraham
 */
public class DBInsert extends DBAction {

    private static DBInsert saver = new DBInsert();
    private static DBInsertLargeObjects blobSave = new DBInsertLargeObjects();
    private transient StringBuilder allColumns;
    private transient StringBuilder allValues;

    public <R extends DBRow> DBInsert(R row) {
        super(row);
    }

    public static DBActionList save(DBDatabase database, DBRow row) throws SQLException {
        return saver.execute(database, row);
    }

    private DBInsert() {
        super();
    }

    @Override
    public ArrayList<String> getSQLStatements(DBDatabase db, DBRow row) {
        DBDefinition defn = db.getDefinition();
        processAllFieldsForInsert(db, row);

        ArrayList<String> strs = new ArrayList<String>();
        strs.add(defn.beginInsertLine()
                + defn.formatTableName(row)
                + defn.beginInsertColumnList()
                + allColumns
                + defn.endInsertColumnList()
                + allValues
                + defn.endInsertLine());
        return strs;
    }

    @Override
    public DBActionList execute(DBDatabase db, DBRow row) throws SQLException {
        DBStatement statement = db.getDBStatement();

        DBActionList actions = new DBActionList(new DBInsert(row));
        for (String sql : getSQLStatements(db, row)) {
            statement.execute(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet generatedKeys = statement.getGeneratedKeys();
            try {
                ResultSetMetaData metaData = generatedKeys.getMetaData();
                while (generatedKeys.next()) {
                    for (int i = 1; i <= metaData.getColumnCount(); i++) {
                        System.out.println("GENERATED KEYS: " + generatedKeys.getLong(1));
                    }
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            } finally {
                generatedKeys.close();
            }
        }
        actions.addAll(blobSave.execute(db, row));
        row.setDefined();
        return actions;
    }

    private void processAllFieldsForInsert(DBDatabase database, DBRow row) {
        allColumns = new StringBuilder();
        allValues = new StringBuilder();
        DBDefinition defn = database.getDefinition();
        List<PropertyWrapper> props = row.getPropertyWrappers();
        String columnSeparator = "";
        String valuesSeparator = defn.beginValueClause();
        for (PropertyWrapper prop : props) {
            // BLOBS are not inserted normally so don't include them
            if (prop.isColumn()) {
                final QueryableDatatype qdt = prop.getQueryableDatatype();
                if (!(qdt instanceof DBLargeObject)) {
                    // nice normal columns
                    // Add the column
                    allColumns
                            .append(columnSeparator)
                            .append(" ")
                            .append(defn.formatColumnName(prop.columnName()));
                    columnSeparator = defn.getValuesClauseColumnSeparator();
                    // add the value
                    allValues.append(valuesSeparator).append(qdt.toSQLString(database));
                    valuesSeparator = defn.getValuesClauseValueSeparator();
                }
            }
        }
        allValues.append(defn.endValueClause());
    }

    @Override
    public DBActionList getRevertDBActionList() {
        DBActionList reverts = new DBActionList();
        if (this.row.getPrimaryKey() == null) {
            reverts.add(new DBDeleteUsingAllColumns(row));
        } else {
            reverts.add(new DBDeleteByPrimaryKey(row));
        }
        return reverts;
    }

    @Override
    protected DBActionList getActions(DBRow row) {
        return new DBActionList(new DBInsert(row));
    }

    /**
     * Creates a DBActionList of inserts actions for the rows.
     *
     * <p>
     * The actions created can be applied on a particular database using
     * {@link DBActionList#execute(nz.co.gregs.dbvolution.DBDatabase)}
     *
     * @param rows
     * @return a DBActionList of inserts.
     * @throws SQLException
     */
    public static DBActionList getInserts(DBRow... rows) throws SQLException {
        DBActionList inserts = new DBActionList();
        for (DBRow row : rows) {
            inserts.add(new DBInsert(row));
        }
        return inserts;
    }
}