/*
 * Copyright 2014 gregorygraham.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nz.co.gregs.dbvolution.DBRow;
import nz.co.gregs.dbvolution.expressions.StringExpression;
import nz.co.gregs.dbvolution.expressions.StringResult;
import nz.co.gregs.dbvolution.operators.DBPermittedPatternOperator;
import nz.co.gregs.dbvolution.operators.DBPermittedRangeExclusiveOperator;
import nz.co.gregs.dbvolution.operators.DBPermittedRangeInclusiveOperator;
import nz.co.gregs.dbvolution.operators.DBPermittedRangeOperator;
import nz.co.gregs.dbvolution.operators.DBPermittedValuesIgnoreCaseOperator;
import nz.co.gregs.dbvolution.operators.DBPermittedValuesOperator;

/**
 * Like {@link DBInteger} except that the database value can be easily
 * interpreted as an enumeration with integer codes.
 *
 * @param <E> type of enumeration class
 */
public class DBStringEnum<E extends Enum<E> & DBEnumValue<String>> extends DBEnum<E> {

    private static final long serialVersionUID = 1L;

    public DBStringEnum() {
    }

    public DBStringEnum(String value) {
        super(value);
    }

    public DBStringEnum(StringResult stringExpression) {
        super(stringExpression);
    }

    public DBStringEnum(E value) {
        super(value);
    }

    @Override
    protected void validateLiteralValue(E enumValue) {
        Object localValue = enumValue.getCode();
        if (localValue != null) {
            if (!(localValue instanceof String)) {
                String enumMethodRef = enumValue.getClass().getName() + "." + enumValue.name() + ".getLiteralValue()";
                String literalValueTypeRef = localValue.getClass().getName();
                throw new IncompatibleClassChangeError("Enum literal type is not valid: "
                        + enumMethodRef + " returned a " + literalValueTypeRef + ", which is not valid for a " + this.getClass().getSimpleName());
            }
        }
    }

    @Override
    public String getSQLDatatype() {
        return new DBString().getSQLDatatype();
    }

    @Override
    public void setValue(Object newLiteralValue) {
        if (newLiteralValue instanceof String) {
            setValue((String) newLiteralValue);
        } else if (newLiteralValue instanceof DBString) {
            setValue(((DBString) newLiteralValue).getValue());
        } else {
            throw new ClassCastException(this.getClass().getSimpleName() + ".setValue() Called With A Non-String: Use only Strings with this class");
        }
    }

    public void setValue(String newLiteralValue) {
        super.setLiteralValue(newLiteralValue);
    }

    @Override
    public DBString getQueryableDatatypeForExpressionValue() {
        return new DBString();
    }

    @Override
    public boolean isAggregator() {
        return false;
    }

    @Override
    public Set<DBRow> getTablesInvolved() {
        return new HashSet<DBRow>();
    }

    protected String[] convertToLiteralString(E... enumValues) {
        String[] result = new String[enumValues.length];
        for (int i = 0; i < enumValues.length; i++) {
            E enumValue = enumValues[i];
            result[i] = convertToLiteralString(enumValue);
        }
        return result;
    }

    protected String[] convertToLiteralString(Collection<E> enumValues) {
        ArrayList<String> result = new ArrayList<String>();
        for (E e : enumValues) {
            result.add(convertToLiteralString(e));
        }
        return result.toArray(new String[]{});
    }

    protected final String convertToLiteralString(E enumValue) {
        if (enumValue == null || enumValue.getCode() == null) {
            return null;
        } else {
            validateLiteralValue(enumValue);
            String newLiteralValue = enumValue.getCode();
            return newLiteralValue;
        }
    }

    /**
     *
     * reduces the rows to only the object, Set, List, Array, or vararg of
     * objects
     *
     * @param permitted
     */
    public void permittedValues(String... permitted) {
        this.setOperator(new DBPermittedValuesOperator((Object[]) permitted));
    }

    /**
     *
     * reduces the rows to only the object, Set, List, Array, or vararg of
     * Strings ignoring letter case.
     *
     * @param permitted
     */
    public void permittedValuesIgnoreCase(String... permitted) {
        this.setOperator(new DBPermittedValuesIgnoreCaseOperator(permitted));
    }

    /**
     *
     * reduces the rows to only the object, Set, List, Array, or vararg of
     * Strings ignoring letter case.
     *
     * @param permitted
     */
    public void permittedValuesIgnoreCase(StringExpression... permitted) {
        this.setOperator(new DBPermittedValuesIgnoreCaseOperator(permitted));
    }

    /**
     *
     * reduces the rows to only the object, Set, List, Array, or vararg of
     * Strings ignoring letter case.
     *
     * @param permitted
     */
    public void permittedValuesIgnoreCase(List<String> permitted) {
        this.setOperator(new DBPermittedValuesIgnoreCaseOperator(permitted));
    }

    /**
     *
     * reduces the rows to only the object, Set, List, Array, or vararg of
     * Strings ignoring letter case.
     *
     * @param permitted
     */
    public void permittedValuesIgnoreCase(Set<String> permitted) {
        this.setOperator(new DBPermittedValuesIgnoreCaseOperator(permitted));
    }

    /**
     * Reduces the rows to excluding the object, Set, List, Array, or vararg of
     * Strings ignoring letter case.
     *
     * @param excluded
     */
    public void excludedValuesIgnoreCase(String... excluded) {
        setOperator(new DBPermittedValuesIgnoreCaseOperator(excluded));
        negateOperator();
    }

    /**
     * Reduces the rows to excluding the object, Set, List, Array, or vararg of
     * Strings ignoring letter case.
     *
     * @param excluded
     */
    public void excludedValuesIgnoreCase(StringExpression... excluded) {
        setOperator(new DBPermittedValuesIgnoreCaseOperator(excluded));
        negateOperator();
    }

    /**
     * Reduces the rows to excluding the object, Set, List, Array, or vararg of
     * Strings ignoring letter case.
     *
     * @param excluded
     */
    public void excludedValuesIgnoreCase(List<String> excluded) {
        setOperator(new DBPermittedValuesIgnoreCaseOperator(excluded));
        negateOperator();
    }

    /**
     *
     * @param excluded
     */
    public void excludedValuesIgnoreCase(Set<String> excluded) {
        setOperator(new DBPermittedValuesIgnoreCaseOperator(excluded));
        negateOperator();
    }

    /**
     *
     * excludes the object, Set, List, Array, or vararg of objects
     *
     *
     * @param excluded
     */
    public void excludedValues(String... excluded) {
        this.setOperator(new DBPermittedValuesOperator((Object[]) excluded));
        negateOperator();
    }

    /**
     * Performs searches based on a range.
     *
     * if both ends of the range are specified the lower-bound will be included
     * in the search and the upper-bound excluded. I.e permittedRange(1,3) will
     * return 1 and 2.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and inclusive.
     * <br>
     * I.e permittedRange(1,null) will return 1,2,3,4,5, etc.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and exclusive.
     * <br>
     * I.e permittedRange(null, 5) will return 4,3,2,1, etc.
     *
     * @param lowerBound
     * @param upperBound
     */
    public void permittedRange(String lowerBound, String upperBound) {
        setOperator(new DBPermittedRangeOperator(lowerBound, upperBound));
    }

    /**
     * Performs searches based on a range.
     *
     * if both ends of the range are specified both the lower- and upper-bound
     * will be included in the search. I.e permittedRangeInclusive(1,3) will
     * return 1, 2, and 3.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and inclusive.
     * <br>
     * I.e permittedRangeInclusive(1,null) will return 1,2,3,4,5, etc.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and inclusive.
     * <br>
     * I.e permittedRangeInclusive(null, 5) will return 5,4,3,2,1, etc.
     *
     * @param lowerBound
     * @param upperBound
     */
    public void permittedRangeInclusive(String lowerBound, String upperBound) {
        setOperator(new DBPermittedRangeInclusiveOperator(lowerBound, upperBound));
    }

    /**
     * Performs searches based on a range.
     *
     * if both ends of the range are specified both the lower- and upper-bound
     * will be excluded in the search. I.e permittedRangeExclusive(1,3) will
     * return 2.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and exclusive.
     * <br>
     * I.e permittedRangeExclusive(1,null) will return 2,3,4,5, etc.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and exclusive.
     * <br>
     * I.e permittedRangeExclusive(null, 5) will return 4,3,2,1, etc.
     *
     * @param lowerBound
     * @param upperBound
     */
    public void permittedRangeExclusive(String lowerBound, String upperBound) {
        setOperator(new DBPermittedRangeExclusiveOperator(lowerBound, upperBound));
    }

    public void excludedRange(String lowerBound, String upperBound) {
        setOperator(new DBPermittedRangeOperator(lowerBound, upperBound));
        negateOperator();
    }

    public void excludedRangeInclusive(String lowerBound, String upperBound) {
        setOperator(new DBPermittedRangeInclusiveOperator(lowerBound, upperBound));
        negateOperator();
    }

    public void excludedRangeExclusive(String lowerBound, String upperBound) {
        setOperator(new DBPermittedRangeExclusiveOperator(lowerBound, upperBound));
        negateOperator();
    }

    /**
     * Perform searches based on using database compatible pattern matching
     *
     * <p>
     * This facilitates the LIKE operator.
     *
     * <p>
     * Please use the pattern system appropriate to your database.
     *
     * <p>
     * Java0-style regular expressions are not yet supported.
     *
     * @param pattern
     */
    public void permittedPattern(String pattern) {
        this.setOperator(new DBPermittedPatternOperator(pattern));
    }

    public void excludedPattern(String pattern) {
        this.setOperator(new DBPermittedPatternOperator(pattern));
        this.negateOperator();
    }

    public void permittedPattern(StringExpression pattern) {
        this.setOperator(new DBPermittedPatternOperator(pattern));
    }

    public void excludedPattern(StringExpression pattern) {
        this.setOperator(new DBPermittedPatternOperator(pattern));
        this.negateOperator();
    }

    /**
     *
     * reduces the rows to only the object, Set, List, Array, or vararg of
     * objects
     *
     * @param permitted
     */
    public void permittedValues(E... permitted) {
        this.setOperator(new DBPermittedValuesOperator(convertToLiteral(permitted)));
    }

    /**
     *
     * reduces the rows to only the object, Set, List, Array, or vararg of
     * Strings ignoring letter case.
     *
     * @param permitted
     */
    public void permittedValuesIgnoreCase(E... permitted) {
        this.setOperator(new DBPermittedValuesIgnoreCaseOperator((String[]) convertToLiteral(permitted)));
    }

    /**
     * Reduces the rows to excluding the object, Set, List, Array, or vararg of
     * Strings ignoring letter case.
     *
     * @param excluded
     */
    public void excludedValuesIgnoreCase(E... excluded) {
        setOperator(new DBPermittedValuesIgnoreCaseOperator(convertToLiteralString(excluded)));
        negateOperator();
    }

    /**
     * Reduces the rows to excluding the object, Set, List, Array, or vararg of
     * Strings ignoring letter case.
     *
     * @param excluded
     */
    public void excludedValuesIgnoreCase(Collection<E> excluded) {
        setOperator(new DBPermittedValuesIgnoreCaseOperator(convertToLiteralString(excluded)));
        negateOperator();
    }

    /**
     *
     * excludes the object, Set, List, Array, or vararg of objects
     *
     *
     * @param excluded
     */
    public void excludedValues(E... excluded) {
        this.setOperator(new DBPermittedValuesOperator((Object[]) convertToLiteralString(excluded)));
        negateOperator();
    }

    /**
     * Performs searches based on a range.
     *
     * if both ends of the range are specified the lower-bound will be included
     * in the search and the upper-bound excluded. I.e permittedRange(1,3) will
     * return 1 and 2.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and inclusive.
     * <br>
     * I.e permittedRange(1,null) will return 1,2,3,4,5, etc.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and exclusive.
     * <br>
     * I.e permittedRange(null, 5) will return 4,3,2,1, etc.
     *
     * @param lowerBound
     * @param upperBound
     */
    public void permittedRange(E lowerBound, E upperBound) {
        setOperator(new DBPermittedRangeOperator(convertToLiteralString(lowerBound), convertToLiteralString(upperBound)));
    }

    /**
     * Performs searches based on a range.
     *
     * if both ends of the range are specified both the lower- and upper-bound
     * will be included in the search. I.e permittedRangeInclusive(1,3) will
     * return 1, 2, and 3.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and inclusive.
     * <br>
     * I.e permittedRangeInclusive(1,null) will return 1,2,3,4,5, etc.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and inclusive.
     * <br>
     * I.e permittedRangeInclusive(null, 5) will return 5,4,3,2,1, etc.
     *
     * @param lowerBound
     * @param upperBound
     */
    public void permittedRangeInclusive(E lowerBound, E upperBound) {
        setOperator(new DBPermittedRangeInclusiveOperator(convertToLiteralString(lowerBound), convertToLiteralString(upperBound)));
    }

    /**
     * Performs searches based on a range.
     *
     * if both ends of the range are specified both the lower- and upper-bound
     * will be excluded in the search. I.e permittedRangeExclusive(1,3) will
     * return 2.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and exclusive.
     * <br>
     * I.e permittedRangeExclusive(1,null) will return 2,3,4,5, etc.
     *
     * <p>
     * if the upper-bound is null the range will be open ended and exclusive.
     * <br>
     * I.e permittedRangeExclusive(null, 5) will return 4,3,2,1, etc.
     *
     * @param lowerBound
     * @param upperBound
     */
    public void permittedRangeExclusive(E lowerBound, E upperBound) {
        setOperator(new DBPermittedRangeExclusiveOperator(convertToLiteralString(lowerBound), convertToLiteralString(upperBound)));
    }

    public void excludedRange(E lowerBound, E upperBound) {
        setOperator(new DBPermittedRangeOperator(convertToLiteralString(lowerBound), convertToLiteralString(upperBound)));
        negateOperator();
    }

    public void excludedRangeInclusive(E lowerBound, E upperBound) {
        setOperator(new DBPermittedRangeInclusiveOperator(convertToLiteralString(lowerBound), convertToLiteralString(upperBound)));
        negateOperator();
    }

    public void excludedRangeExclusive(E lowerBound, E upperBound) {
        setOperator(new DBPermittedRangeExclusiveOperator(convertToLiteralString(lowerBound), convertToLiteralString(upperBound)));
        negateOperator();
    }

    @Override
    public String getValue() {
        final Object value = super.literalValue;
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return (String) value;
        } else {
            return value.toString();
        }
    }
}