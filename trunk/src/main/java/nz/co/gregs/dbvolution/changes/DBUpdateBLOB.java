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
package nz.co.gregs.dbvolution.changes;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import nz.co.gregs.dbvolution.DBDatabase;
import nz.co.gregs.dbvolution.DBRow;
import nz.co.gregs.dbvolution.databases.DBStatement;
import nz.co.gregs.dbvolution.databases.definitions.DBDefinition;
import nz.co.gregs.dbvolution.datatypes.DBLargeObject;

public class DBUpdateBLOB extends DBDataChange {

    private final String columnName;
    private final DBLargeObject blob;

    public DBUpdateBLOB(DBRow row, String columnName, DBLargeObject blob) {
        super(row);
        this.columnName = columnName;
        this.blob = (DBLargeObject)blob.copy();
    }

    @Override
    public boolean canBeBatched() {
        return false;
    }

    @Override
    public void execute(DBDatabase db, DBStatement statement) throws SQLException {
        DBDefinition defn = db.getDefinition();
        DBRow row = getRow();
        String sqlString = defn.beginUpdateLine()
                + defn.formatTableName(row.getTableName())
                + defn.beginSetClause()
                + defn.formatColumnName(columnName)
                + defn.getEqualsComparator()
                + defn.getPreparedVariableSymbol()
                + defn.beginWhereClause()
                + defn.formatColumnName(row.getPrimaryKeyColumnName())
                + defn.getEqualsComparator()
                + row.getPrimaryKey().toSQLString(db)
                + defn.endSQLStatement();
        db.printSQLIfRequested(sqlString);
        PreparedStatement prep = statement.getConnection().prepareStatement(sqlString);
        prep.setBinaryStream(1, blob.getInputStream(), blob.getSize());
        prep.execute();
    }

    @Override
    public List<String> getSQLStatements(DBDatabase db) {
        List<String> strs = new ArrayList<String>();
        strs.add("// SAVE BINARY DATA");
        return strs;
    }
}
