/*
 * Copyright 2014 gregory.graham.
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
package nz.co.gregs.dbvolution.query;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import nz.co.gregs.dbvolution.DBQuery;
import nz.co.gregs.dbvolution.columns.BooleanColumn;
import nz.co.gregs.dbvolution.columns.ColumnProvider;
import nz.co.gregs.dbvolution.columns.DateColumn;
import nz.co.gregs.dbvolution.columns.IntegerColumn;
import nz.co.gregs.dbvolution.columns.LargeObjectColumn;
import nz.co.gregs.dbvolution.columns.NumberColumn;
import nz.co.gregs.dbvolution.columns.StringColumn;
import nz.co.gregs.dbvolution.datatypes.DBBoolean;
import nz.co.gregs.dbvolution.datatypes.DBDate;
import nz.co.gregs.dbvolution.datatypes.DBInteger;
import nz.co.gregs.dbvolution.datatypes.DBLargeObject;
import nz.co.gregs.dbvolution.datatypes.DBNumber;
import nz.co.gregs.dbvolution.datatypes.DBString;
import nz.co.gregs.dbvolution.datatypes.QueryableDatatype;
import nz.co.gregs.dbvolution.exceptions.IncorrectRowProviderInstanceSuppliedException;
import nz.co.gregs.dbvolution.expressions.DBExpression;
import nz.co.gregs.dbvolution.expressions.NumberExpression;
import nz.co.gregs.dbvolution.expressions.NumberResult;
import nz.co.gregs.dbvolution.expressions.StringExpression;
import nz.co.gregs.dbvolution.expressions.StringResult;
import nz.co.gregs.dbvolution.internal.properties.RowDefinitionWrapperFactory;
import nz.co.gregs.dbvolution.internal.properties.PropertyWrapper;
import nz.co.gregs.dbvolution.internal.properties.RowDefinitionInstanceWrapper;

/**
 * Encapsulates the concept of a row that has fields/columns and is part of a table/view on a database.
 *
 * @author gregory.graham
 */
public class RowDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final RowDefinitionWrapperFactory wrapperFactory = new RowDefinitionWrapperFactory();
    private transient RowDefinitionInstanceWrapper wrapper = null;

    /**
     * Gets a wrapper for the underlying property (field or method) given the
     * property's object reference.
     *
     * <p>
     * For example the following code snippet will get a property wrapper for
     * the {@literal name} field:
     * <pre>
     * Customer customer = ...;
     * getPropertyWrapperOf(customer.name);
     * </pre>
     *
     * @param qdt
     * @return the PropertyWrapper associated with the Object suppled.
     */
    public PropertyWrapper getPropertyWrapperOf(Object qdt) {
        List<PropertyWrapper> props = getWrapper().getPropertyWrappers();

        Object qdtOfProp;
        for (PropertyWrapper prop : props) {
            qdtOfProp = prop.rawJavaValue();
            if (qdtOfProp == qdt) {
                return prop;
            }
        }
        return null;
    }

    protected RowDefinitionInstanceWrapper getWrapper() {
        if (wrapper == null) {
            wrapper = wrapperFactory.instanceWrapperFor(this);
        }
        return wrapper;
    }

    /**
     * Returns the PropertyWrappers used internally to maintain the relationship
     * between fields and columns
     *
     * @return non-null list of property wrappers, empty if none
     */
    public List<PropertyWrapper> getPropertyWrappers() {
        return getWrapper().getPropertyWrappers();
    }

    /**
     * Creates a new LargeObjectColumn instance to help create
     * {@link DBExpression expressions}
     *
     * <p>
     * This method is the easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
     * @return A LargeObjectColumn representing the supplied field
     */
    public LargeObjectColumn column(DBLargeObject fieldOfThisInstance) {
        return new LargeObjectColumn(this, fieldOfThisInstance);
    }

    /**
     * Creates a new ColumnProvider instance to help create
     * {@link DBExpression expressions}
     *
     * <p>
     * This method is the easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
     * @return A ColumnProvider representing the supplied field
     */
    public ColumnProvider column(QueryableDatatype fieldOfThisInstance) throws IncorrectRowProviderInstanceSuppliedException {
        ColumnProvider col = null;
        if (DBBoolean.class.isAssignableFrom(fieldOfThisInstance.getClass())) {
            col = this.column((DBBoolean) fieldOfThisInstance);
        } else if (DBDate.class.isAssignableFrom(fieldOfThisInstance.getClass())) {
            col = this.column((DBDate) fieldOfThisInstance);
        } else if (DBLargeObject.class.isAssignableFrom(fieldOfThisInstance.getClass())) {
            col = this.column((DBLargeObject) fieldOfThisInstance);
        } else if (DBInteger.class.isAssignableFrom(fieldOfThisInstance.getClass())) {
            col = this.column((DBInteger) fieldOfThisInstance);
        } else if (DBNumber.class.isAssignableFrom(fieldOfThisInstance.getClass())) {
            col = this.column((DBNumber) fieldOfThisInstance);
        } else if (DBString.class.isAssignableFrom(fieldOfThisInstance.getClass())) {
            col = this.column((DBString) fieldOfThisInstance);
        }
        if (col == null) {
            throw new IncorrectRowProviderInstanceSuppliedException(this, fieldOfThisInstance);
        }
        return col;
    }

    /**
     * Creates a new BooleanColumn instance to help create
     * {@link DBExpression expressions}
     *
     * <p>
     * This method is the easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
     * @return A LargeObjectColumn representing the supplied field
     */
    public BooleanColumn column(DBBoolean fieldOfThisInstance) {
        return new BooleanColumn(this, fieldOfThisInstance);
    }

    /**
     * Creates a new BooleanColumn instance to help create
     * {@link DBExpression expressions}
     *
     * <p>
     * This method is the easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
     * @return A Column representing the supplied field
     */
    public BooleanColumn column(Boolean fieldOfThisInstance) {
        return new BooleanColumn(this, fieldOfThisInstance);
    }

    /**
     * Creates a new StringColumn instance to help create
     * {@link DBExpression expressions}.
     *
     * <p>
     * This method is the easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
     * @return A Column representing the supplied field
     */
    public StringColumn column(DBString fieldOfThisInstance) {
        return new StringColumn(this, fieldOfThisInstance);
    }

    /**
     * Creates a new StringColumn instance to help create
     * {@link DBExpression expressions}
     *
     * <p>
     * This method is the easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
     * @return A Column representing the supplied field
     */
    public StringColumn column(String fieldOfThisInstance) {
        return new StringColumn(this, fieldOfThisInstance);
    }

    /**
     * Creates a new StringExpression for the field with a default value for NULL entries.
     *
     * <p>
     * This method is the easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
	 * @param valueToUseIfDBNull
     * @return A StringExpression representing the supplied field with a default value for NULLs
     */
    public StringExpression column(DBString fieldOfThisInstance, StringResult valueToUseIfDBNull) {
        return (new StringColumn(this, fieldOfThisInstance)).ifDBNull(valueToUseIfDBNull);
    }

    /**
     * Creates a new StringExpression for the field with a default value for NULL entries.
     *
     * <p>
     * This method is the easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
	 * @param valueToUseIfDBNull
     * @return A StringExpression representing the supplied field with a default value for NULLs
     */
    public StringExpression column(DBString fieldOfThisInstance, String valueToUseIfDBNull) {
        return (new StringColumn(this, fieldOfThisInstance)).ifDBNull(valueToUseIfDBNull);
    }

    /**
     * Creates a new StringExpression for the field with a default value for NULL entries.
     *
     * <p>
     * This method is the easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
	 * @param valueToUseIfDBNull
     * @return A StringExpression representing the supplied field with a default value for NULLs
     */
    public StringExpression column(String fieldOfThisInstance, StringResult valueToUseIfDBNull) {
        return (new StringColumn(this, fieldOfThisInstance)).ifDBNull(valueToUseIfDBNull);
    }

    /**
     * Creates a new StringExpression for the field with a default value for NULL entries.
     *
     * <p>
     * This method is the easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
	 * @param valueToUseIfDBNull
     * @return A StringExpression representing the supplied field with a default value for NULLs
     */
    public StringExpression column(String fieldOfThisInstance, String valueToUseIfDBNull) {
        return (new StringColumn(this, fieldOfThisInstance)).ifDBNull(valueToUseIfDBNull);
    }
	
    /**
     * Creates a new NumberColumn instance to help create
     * {@link DBExpression expressions}
     *
     * <p>
     * This method is the easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
     * @return A Column representing the supplied field
     */
    public NumberColumn column(DBNumber fieldOfThisInstance) {
        return new NumberColumn(this, fieldOfThisInstance);
    }

    /**
     * Creates a new NumberColumn instance to help create
     * {@link DBExpression expressions}
     *
     * <p>
     * This method is the easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
     * @return A Column representing the supplied field
     */
    public NumberColumn column(Number fieldOfThisInstance) {
        return new NumberColumn(this, fieldOfThisInstance);
    }

    /**
     * Creates a new NumberExpression representing the column and supplying a default value for NULL entries.
     *
     * <p>
     * This method is an easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
	 * @param valueToUseIfDBNull
     * @return A NumberExpression representing the supplied field with a default for DBNULL values
     */
    public NumberExpression column(Number fieldOfThisInstance, Number valueToUseIfDBNull) {
        return new NumberColumn(this, fieldOfThisInstance).ifDBNull(valueToUseIfDBNull);
    }


    /**
     * Creates a new NumberExpression representing the column and supplying a default value for NULL entries.
     *
     * <p>
     * This method is an easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
	 * @param valueToUseIfDBNull
     * @return A NumberExpression representing the supplied field with a default for DBNULL values
     */
    public NumberExpression column(DBNumber fieldOfThisInstance, Number valueToUseIfDBNull) {
        return new NumberColumn(this, fieldOfThisInstance).ifDBNull(valueToUseIfDBNull);
    }


    /**
     * Creates a new NumberExpression representing the column and supplying a default value for NULL entries.
     *
     * <p>
     * This method is an easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
	 * @param valueToUseIfDBNull
     * @return A NumberExpression representing the supplied field with a default for DBNULL values
     */
    public NumberExpression column(DBNumber fieldOfThisInstance, NumberResult valueToUseIfDBNull) {
        return new NumberColumn(this, fieldOfThisInstance).ifDBNull(valueToUseIfDBNull);
    }

    /**
     * Creates a new IntegerColumn instance to help create
     * {@link DBExpression expressions}
     *
     * <p>
     * This method is the easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
     * @return A Column representing the supplied field
     */
    public IntegerColumn column(DBInteger fieldOfThisInstance) {
        return new IntegerColumn(this, fieldOfThisInstance);
    }

    /**
     * Creates a new IntegerColumn instance to help create
     * {@link DBExpression expressions}
     *
     * <p>
     * This method is the easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
     * @return A Column representing the supplied field
     */
    public IntegerColumn column(Long fieldOfThisInstance) {
        return new IntegerColumn(this, fieldOfThisInstance);
    }

    /**
     * Creates a new IntegerColumn instance to help create
     * {@link DBExpression expressions}
     *
     * <p>
     * This method is the easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
     * @return A Column representing the supplied field
     */
    public IntegerColumn column(Integer fieldOfThisInstance) {
        return new IntegerColumn(this, fieldOfThisInstance);
    }

    /**
     * Creates a new NumberExpression representing the column and supplying a default value for NULL entries.
     *
     * <p>
     * This method is an easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
	 * @param valueToUseIfDBNull
     * @return A NumberExpression representing the supplied field with a default for DBNULL values
     */
    public NumberExpression column(Integer fieldOfThisInstance, Integer valueToUseIfDBNull) {
        return new IntegerColumn(this, fieldOfThisInstance).ifDBNull(valueToUseIfDBNull);
    }

    /**
     * Creates a new NumberExpression representing the column and supplying a default value for NULL entries.
     *
     * <p>
     * This method is an easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
	 * @param valueToUseIfDBNull
     * @return A NumberExpression representing the supplied field with a default for DBNULL values
     */
    public NumberExpression column(Integer fieldOfThisInstance, Long valueToUseIfDBNull) {
        return new IntegerColumn(this, fieldOfThisInstance).ifDBNull(valueToUseIfDBNull);
    }

    /**
     * Creates a new NumberExpression representing the column and supplying a default value for NULL entries.
     *
     * <p>
     * This method is an easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
	 * @param valueToUseIfDBNull
     * @return A NumberExpression representing the supplied field with a default for DBNULL values
     */
    public NumberExpression column(Integer fieldOfThisInstance, NumberResult valueToUseIfDBNull) {
        return new IntegerColumn(this, fieldOfThisInstance).ifDBNull(valueToUseIfDBNull);
    }

    /**
     * Creates a new NumberExpression representing the column and supplying a default value for NULL entries.
     *
     * <p>
     * This method is an easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
	 * @param valueToUseIfDBNull
     * @return A NumberExpression representing the supplied field with a default for DBNULL values
     */
    public NumberExpression column(DBInteger fieldOfThisInstance, Integer valueToUseIfDBNull) {
        return new IntegerColumn(this, fieldOfThisInstance).ifDBNull(valueToUseIfDBNull);
    }

    /**
     * Creates a new NumberExpression representing the column and supplying a default value for NULL entries.
     *
     * <p>
     * This method is an easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
	 * @param valueToUseIfDBNull
     * @return A NumberExpression representing the supplied field with a default for DBNULL values
     */
    public NumberExpression column(DBInteger fieldOfThisInstance, Long valueToUseIfDBNull) {
        return new IntegerColumn(this, fieldOfThisInstance).ifDBNull(valueToUseIfDBNull);
    }

    /**
     * Creates a new NumberExpression representing the column and supplying a default value for NULL entries.
     *
     * <p>
     * This method is an easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
	 * @param valueToUseIfDBNull
     * @return A NumberExpression representing the supplied field with a default for DBNULL values
     */
    public NumberExpression column(DBInteger fieldOfThisInstance, NumberResult valueToUseIfDBNull) {
        return new IntegerColumn(this, fieldOfThisInstance).ifDBNull(valueToUseIfDBNull);
    }

    /**
     * Creates a new NumberExpression representing the column and supplying a default value for NULL entries.
     *
     * <p>
     * This method is an easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
	 * @param valueToUseIfDBNull
     * @return A NumberExpression representing the supplied field with a default for DBNULL values
     */
    public NumberExpression column(Long fieldOfThisInstance, Integer valueToUseIfDBNull) {
        return new IntegerColumn(this, fieldOfThisInstance).ifDBNull(valueToUseIfDBNull);
    }

    /**
     * Creates a new NumberExpression representing the column and supplying a default value for NULL entries.
     *
     * <p>
     * This method is an easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
	 * @param valueToUseIfDBNull
     * @return A NumberExpression representing the supplied field with a default for DBNULL values
     */
    public NumberExpression column(Long fieldOfThisInstance, Long valueToUseIfDBNull) {
        return new IntegerColumn(this, fieldOfThisInstance).ifDBNull(valueToUseIfDBNull);
    }

    /**
     * Creates a new NumberExpression representing the column and supplying a default value for NULL entries.
     *
     * <p>
     * This method is an easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
	 * @param valueToUseIfDBNull
     * @return A NumberExpression representing the supplied field with a default for DBNULL values
     */
    public NumberExpression column(Long fieldOfThisInstance, NumberResult valueToUseIfDBNull) {
        return new IntegerColumn(this, fieldOfThisInstance).ifDBNull(valueToUseIfDBNull);
    }

    /**
     * Creates a new DateColumn instance to help create
     * {@link DBExpression expressions}
     *
     * <p>
     * This method is the easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
     * @return A Column representing the supplied field
     */
    public DateColumn column(DBDate fieldOfThisInstance) {
        return new DateColumn(this, fieldOfThisInstance);
    }

    /**
     * Creates a new DateColumn instance to help create
     * {@link DBExpression expressions}
     *
     * <p>
     * This method is the easy way to create a reference to the database column
     * represented by the field for use in creating complex expressions within
     * your query.
     *
     * <p>
     * For use with the
     * {@link DBQuery#addCondition(nz.co.gregs.dbvolution.expressions.BooleanExpression) DBQuery addCondition method}
     *
     * @param fieldOfThisInstance
     * @return A Column representing the supplied field
     */
    public DateColumn column(Date fieldOfThisInstance) {
        return new DateColumn(this, fieldOfThisInstance);
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        List<PropertyWrapper> fields = getWrapper().getPropertyWrappers();

        String separator = "" + this.getClass().getSimpleName();

        for (PropertyWrapper field : fields) {
            if (field.isColumn()) {
                string.append(separator);
                string.append(" ");
                string.append(field.javaName());
                string.append(":");
                string.append(field.getQueryableDatatype());
                separator = ",";
            }
        }
        return string.toString();
    }

    public String getHTMLTableRow(String tableRowCSSClass) {
        StringBuilder string = new StringBuilder();
        List<PropertyWrapper> fields = getWrapper().getPropertyWrappers();

        
        string.append("<tr class=\"").append(tableRowCSSClass).append("\">");
        for (PropertyWrapper field : fields) {
            if (field.isColumn()) {
                string.append("<td>");
                string.append(field.getQueryableDatatype().stringValue());
                string.append("</td>");
            }
        }
        string.append("</tr>");
        return string.toString();
    }

    public String getHTMLTableHeader(String tableRowCSSClass, String tableHeaderCellCSSClass) {
        StringBuilder string = new StringBuilder();
        List<PropertyWrapper> fields = getWrapper().getPropertyWrappers();

        string.append("<tr class=\"").append(tableRowCSSClass).append("\">");
        for (PropertyWrapper field : fields) {
            if (field.isColumn()) {
                string.append("<th class=\"").append(tableHeaderCellCSSClass).append("\">");
                string.append(field.javaName());
                string.append("</th>");
            }
        }
        string.append("</tr>");
        return string.toString();
    }

}
