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
package nz.co.gregs.dbvolution.generators;

import nz.co.gregs.dbvolution.DBDatabase;
import nz.co.gregs.dbvolution.DBRow;
import nz.co.gregs.dbvolution.internal.PropertyWrapper;

public class Column implements DataGenerator, StringGenerator {

    private final PropertyWrapper propertyWrapperOfQDT;
    private final DBRow dbrow;
    private final Object field;

    public Column(DBRow row, Object field) {
        this.dbrow = row;
        this.field = field;
        this.propertyWrapperOfQDT = row.getPropertyWrapperOf(field);
    }

    @Override
    public String toSQLString(DBDatabase db) {
        return db.getDefinition().formatTableAliasAndColumnName(this.dbrow, propertyWrapperOfQDT.columnName());
    }

    @Override
    public DataGenerator copy() {
        return new Column(dbrow, field);
    }

}