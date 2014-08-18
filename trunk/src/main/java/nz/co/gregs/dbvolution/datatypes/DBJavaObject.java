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
package nz.co.gregs.dbvolution.datatypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import nz.co.gregs.dbvolution.DBDatabase;
import nz.co.gregs.dbvolution.DBRow;

/**
 *
 * Implements the abstractions required for handling Java Objects stored in the
 * database
 *
 * @author Gregory Graham
 */
public class DBJavaObject extends QueryableDatatype {

    private static final long serialVersionUID = 1;

    @Override
    public String getSQLDatatype() {
        return "JAVA_OBJECT";
    }

    @Override
    public void setValue(Object newLiteralValue) {
        if (newLiteralValue instanceof DBJavaObject) {
            DBJavaObject blob = (DBJavaObject) newLiteralValue;
            setValue(blob.getValue());
        } else {
            setLiteralValue(newLiteralValue);
        }
    }

    @Override
    public void setFromResultSet(DBDatabase database, ResultSet resultSet, String fullColumnName) {
        blankQuery();
        if (resultSet == null || fullColumnName == null) {
            this.setToNull();
        } else {
            Object dbValue;
            try {
                dbValue = resultSet.getObject(fullColumnName);
                if (resultSet.wasNull()) {
                    dbValue = null;
                }
            } catch (SQLException ex) {
                dbValue = null;
            }
            if (dbValue == null) {
                this.setToNull();
            } else {
                this.setLiteralValue(dbValue);
            }
        }
        setUnchanged();
        setDefined(true);
    }

    @Override
    public String formatValueForSQLStatement(DBDatabase db) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DBJavaObject getQueryableDatatypeForExpressionValue() {
        return new DBJavaObject();
    }

    @Override
    public boolean isAggregator() {
        return false;
    }

    @Override
    public Set<DBRow> getTablesInvolved() {
        return new HashSet<DBRow>();
    }
}
