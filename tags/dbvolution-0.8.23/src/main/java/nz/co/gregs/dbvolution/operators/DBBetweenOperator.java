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

import nz.co.gregs.dbvolution.datatypes.QueryableDatatype;
import nz.co.gregs.dbvolution.DBDatabase;
import nz.co.gregs.dbvolution.exceptions.InappropriateRelationshipOperator;

/**
 *
 * @author gregorygraham
 */
public class DBBetweenOperator extends DBOperator{
    public static final long serialVersionUID = 1L;

    private final QueryableDatatype lowValue;
    private final QueryableDatatype highValue;
    
    public DBBetweenOperator(QueryableDatatype lowValue, QueryableDatatype highValue){
        super();
        this.lowValue =lowValue;
        this.highValue = highValue;
    }
    
    @Override
    public String generateWhereLine(DBDatabase db, String columnName) {
//        lowValue.setDatabase(database);
        String lowerSQLValue = lowValue.getSQLValue(db);
//        highValue.setDatabase(db);
        String upperSQLValue = highValue.getSQLValue(db);
        String beginWhereLine = db.getDefinition().beginAndLine();
        return beginWhereLine + (invertOperator?"!(":"(")+columnName + " between " + lowerSQLValue + " and "+upperSQLValue+")";
    }

    @Override
    public String generateRelationship(DBDatabase database, String columnName, String otherColumnName) {
        throw new InappropriateRelationshipOperator(this);
    }

    @Override
    public DBOperator getInverseOperator() {
        throw new InappropriateRelationshipOperator(this);
    }
    
}