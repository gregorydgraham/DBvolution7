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
package nz.co.gregs.dbvolution;

import java.io.Serializable;

import nz.co.gregs.dbvolution.databases.definitions.DBDefinition;
import nz.co.gregs.dbvolution.datatypes.QueryableDatatype;
import nz.co.gregs.dbvolution.exceptions.IncorrectDBRowInstanceSuppliedException;
import nz.co.gregs.dbvolution.internal.properties.PropertyWrapper;
import nz.co.gregs.dbvolution.operators.DBEqualsOperator;
import nz.co.gregs.dbvolution.operators.DBOperator;

/**
 *
 * @author gregorygraham
 */
public class DBRelationship implements Serializable {

    public static final long serialVersionUID = 1L;
    private DBRow firstTable;
    private DBRow secondTable;
    private PropertyWrapper firstColumnPropertyWrapper;
    private PropertyWrapper secondColumnPropertyWrapper;
    private DBOperator operation;

    public DBRelationship(DBRow thisTable, QueryableDatatype thisTableField, DBRow otherTable, Object otherTableField) {
        this(thisTable, thisTableField, otherTable, otherTableField, new DBEqualsOperator(thisTableField));
    }

    /**
     * Creates a relationship between the first table's column
     * and the second tables' column, identified
     * by the object references of column's fields and/or methods.
     * 
     * <p> For example the following code snippet will create
     * a relationship between the customer's fkAddress column
     * and the adress's uid column:
     * <pre>
     * Customer customer = ...;
     * Address address = ...;
     * DBOperator operator = ...;
     * new DBRelationship(customer, customer.fkAddress, address, address.uid, operator);
     * </pre>
     *
     * <p> Requires that {@code thisTableField} is from the {@code thisTable} instance,
     * and {@code otherTableField} is from the {@code otherTable} instance.
     * @param thisTable
     * @param thisTableField
     * @param otherTable
     * @param otherTableField
     * @param operator
     * @throws IncorrectDBRowInstanceSuppliedException if {@code thisTableField} is not
     * from the {@code thisTable} instance or if {@code otherTableField} is not from 
     * the {@code otherTable} instance
     */
    public DBRelationship(DBRow thisTable, Object thisTableField, DBRow otherTable, Object otherTableField, DBOperator operator) {
        this.firstTable = DBRow.copyDBRow(thisTable);
        this.secondTable = DBRow.copyDBRow(otherTable);
        this.operation = operator;
        
        this.firstColumnPropertyWrapper = thisTable.getPropertyWrapperOf(thisTableField);
        if (firstColumnPropertyWrapper == null) {
            throw new IncorrectDBRowInstanceSuppliedException(thisTable, thisTableField);
        }
        
        this.secondColumnPropertyWrapper = otherTable.getPropertyWrapperOf(otherTableField);
        if (secondColumnPropertyWrapper == null) {
            throw new IncorrectDBRowInstanceSuppliedException(otherTable, otherTableField);
        }
    }

    public String generateSQL(DBDatabase database) {
        final DBDefinition definition = database.getDefinition();
        return getOperation().generateRelationship(database,
                definition.formatTableAliasAndColumnName(firstTable, firstColumnPropertyWrapper.columnName()),
                definition.formatTableAliasAndColumnName(secondTable, secondColumnPropertyWrapper.columnName()));
    }

    static public String generateSQL(DBDatabase database, DBRow firstTable, PropertyWrapper firstColumnProp, DBOperator operation, DBRow secondTable, PropertyWrapper secondColumnProp) {
        final DBDefinition definition = database.getDefinition();
        return operation.generateRelationship(database,
                definition.formatTableAliasAndColumnName(firstTable, firstColumnProp.columnName()),
                definition.formatTableAliasAndColumnName(secondTable, secondColumnProp.columnName()));
    }

    /**
     * @return the firstTable
     */
    public DBRow getFirstTable() {
        return firstTable;
    }

    /**
     * @return the secondTable
     */
    public DBRow getSecondTable() {
        return secondTable;
    }

    /**
     * @return the firstColumn
     */
    public PropertyWrapper getFirstColumnPropertyWrapper() {
        return firstColumnPropertyWrapper;
    }

    /**
     * @return the secondColumn
     */
    public PropertyWrapper getSecondColumnPropertyWrapper() {
        return secondColumnPropertyWrapper;
    }

    /**
     * @return the operation
     */
    public DBOperator getOperation() {
        return operation;
    }
}