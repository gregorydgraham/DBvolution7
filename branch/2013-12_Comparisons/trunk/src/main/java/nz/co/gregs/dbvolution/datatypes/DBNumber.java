/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.gregs.dbvolution.datatypes;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import nz.co.gregs.dbvolution.DBDatabase;
import nz.co.gregs.dbvolution.databases.definitions.DBDefinition;
import nz.co.gregs.dbvolution.operators.DBLikeCaseInsensitiveOperator;
import nz.co.gregs.dbvolution.operators.DBOperator;

/**
 *
 * @author gregory.graham
 */

public class DBNumber extends QueryableDatatype {

    public static final long serialVersionUID = 1;

    public DBNumber() {
        super();
    }

    /**
     *
     * @param aNumber
     */
    public DBNumber(Number aNumber) {
        super(aNumber);
    }

    /**
     *
     * @param aNumber
     */
    public DBNumber(Object aNumber) {
        super(aNumber);
        if (!(aNumber instanceof Number)) {
            initDBNumber(aNumber);
        }
    }

    @Override
    public void setValue(Object newLiteralValue) {
        initDBNumber(newLiteralValue);
    }

    private void initDBNumber(Object aNumber) {
        if (aNumber == null) {
            super.setValue(null);
        } else {
            if (aNumber instanceof Number) {
                super.setValue((Number) aNumber);
            } else {
                super.setValue(Double.parseDouble(aNumber.toString()));
            }
        }
    }

    @Override
    public void blankQuery() {
        super.blankQuery();
    }

    @Override
    public DBOperator useInOperator(Object... literalOptions) {
        ArrayList<DBNumber> intOptions = new ArrayList<DBNumber>();
        for (Object str : literalOptions) {
            intOptions.add(new DBNumber(str));
        }
        return useInOperator(intOptions.toArray(new DBNumber[]{}));
    }

    /**
     *
     * @param inValues
     * @return 
     */
    public DBOperator useInOperator(Number... inValues) {
        ArrayList<DBNumber> intOptions = new ArrayList<DBNumber>();
        for (Number num : inValues) {
            intOptions.add(new DBNumber(num));
        }
        return useInOperator(intOptions.toArray(new DBNumber[]{}));
    }

    /**
     *
     * @param inValues
     * @return 
     */
    public DBOperator useInOperator(List<Number> inValues) {
        ArrayList<DBNumber> intOptions = new ArrayList<DBNumber>();
        for (Number num : inValues) {
            intOptions.add(new DBNumber(num));
        }
        return useInOperator(intOptions.toArray(new DBNumber[]{}));
    }

    /**
     *
     * @param inValues
     */
    public DBOperator useInOperator(DBNumber... inValues) {
        return super.useInOperator(inValues);
    }

    public DBOperator useGreaterThanOperator(Number literalValue) {
        return this.useGreaterThanOperator(new DBNumber(literalValue));
    }

    @Override
    public String getWhereClause(DBDatabase db, String columnName) {
        if (this.getOperator() instanceof DBLikeCaseInsensitiveOperator) {
            throw new RuntimeException("NUMBER COLUMNS CAN'T USE \"LIKE\": " + columnName);
        } else {
            return super.getWhereClause(db, columnName);
        }
    }

    /**
     *
     * @param lower
     * @param upper
     */
    @Override
    public DBOperator useBetweenOperator(Object lower, Object upper) {
        DBNumber upperBoundNumber = new DBNumber(upper);
        DBNumber lowerBoundNumber = new DBNumber(lower);
        return super.useBetweenOperator(lowerBoundNumber, upperBoundNumber);
    }

    /**
     *
     * @param lower
     * @param upper
     */
    public DBOperator useBetweenOperator(Number lower, Number upper) {
        DBNumber upperBoundNumber = new DBNumber(upper);
        DBNumber lowerBoundNumber = new DBNumber(lower);
        return super.useBetweenOperator(lowerBoundNumber, upperBoundNumber);
    }

    @Override
    public DBOperator useEqualsOperator(Object literal) {
        if (literal == null || literal.toString().isEmpty()) {
            super.useEqualsOperator(null);
        } else {
            this.useEqualsOperator(Double.parseDouble(literal.toString()));
        }
        return getOperator();
    }

    /**
     *
     * @param literal
     */
    public DBOperator useEqualsOperator(Number literal) {
        DBOperator useEqualsOperator = super.useEqualsOperator(literal);
        return useEqualsOperator;
    }

    /**
     *
     * @param obj
     */
    @Override
    public DBOperator useLikeOperator(Object obj) {
        throw new RuntimeException("LIKE Comparison Cannot Be Used With Numeric Fields: " + obj);
    }

    @Override
    protected DBOperator useNullOperator() {
        DBOperator op = super.useNullOperator();
        return op;
    }

    /**
     *
     * @return
     */
    @Override
    public String getSQLDatatype() {
        return "NUMERIC(15,5)";
    }

    /**
     *
     * @param db
     * @return
     */
    @Override
    public String formatValueForSQLStatement(DBDatabase db) {
        DBDefinition defn = db.getDefinition();
        if (isNull()) {
            return defn.getNull();
        }
        return defn.beginNumberValue() + literalValue.toString() + defn.endNumberValue();
    }

    /**
     *
     * @return
     */
    @Override
    public Double doubleValue() {
        if (literalValue == null) {
            return null;
        } else if (literalValue instanceof Number) {
            return ((Number) literalValue).doubleValue();
        } else {
            return Double.parseDouble(literalValue.toString());
        }
    }

    /**
     *
     * @return
     */
    @Override
    public Long longValue() {
        if (literalValue == null) {
            return null;
        } else if (literalValue instanceof Number) {
            return ((Number) literalValue).longValue();
        } else {
            return Long.parseLong(literalValue.toString());
        }
    }

    /**
     *
     * @return
     */
    @Override
    public Integer intValue() {
        if (literalValue == null) {
            return null;
        } else if (literalValue instanceof Number) {
            return ((Number) literalValue).intValue();
        } else {
            return Integer.parseInt(literalValue.toString());
        }
    }

    /**
     *
     * @param resultSet
     * @param fullColumnName
     */
    @Override
    public void setFromResultSet(ResultSet resultSet, String fullColumnName) {
        if (resultSet == null || fullColumnName == null) {
            this.useNullOperator();
        } else {
            BigDecimal dbValue;
            try {
                dbValue = resultSet.getBigDecimal(fullColumnName);
                if (resultSet.wasNull()) {
                    dbValue = null;
                }
            } catch (SQLException ex) {
                dbValue = null;
            }
            if (dbValue == null) {
                this.useNullOperator();
            } else {
                this.useEqualsOperator(dbValue);
            }
        }
    }
}