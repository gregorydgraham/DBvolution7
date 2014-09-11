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

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * Implements the abstractions required for handling Java Objects stored in the
 * database
 *
 * @author gregory.graham
 */
public class DBObject extends QueryableDatatype {

    public static final long serialVersionUID = 1;

    // TODO
    @Override
    public String getSQLDatatype() {
        return "JAVA_OBJECT";
    }

    @Override
    protected void setFromResultSet(ResultSet resultSet, String fullColumnName) throws SQLException {
        this.isLiterally(resultSet.getObject(fullColumnName));
    }
}