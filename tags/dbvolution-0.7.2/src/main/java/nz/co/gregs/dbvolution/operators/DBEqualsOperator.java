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
package nz.co.gregs.dbvolution.operators;

import nz.co.gregs.dbvolution.QueryableDatatype;
import nz.co.gregs.dbvolution.databases.DBDatabase;

/**
 *
 * @author gregorygraham
 */
public class DBEqualsOperator extends DBOperator {
    private final QueryableDatatype equalTo;

    /**
     *
     */
    public DBEqualsOperator(QueryableDatatype equalTo) {
        super();
        this.equalTo = equalTo;
    }

    public String getInverse() {
        return " <> ";
    }

    public String getOperator() {
        return " = ";
    }

    @Override
    public String generateWhereLine(DBDatabase database, String columnName) {
        equalTo.setDatabase(database);
        return database.beginAndLine() + columnName + (invertOperator ? getInverse() : getOperator()) + equalTo.getSQLValue() + " ";
    }
}