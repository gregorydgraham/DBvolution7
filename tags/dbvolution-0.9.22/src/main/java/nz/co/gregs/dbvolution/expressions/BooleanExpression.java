/*
 * Copyright 2014 Gregory Graham.
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
package nz.co.gregs.dbvolution.expressions;

import java.util.HashSet;
import java.util.Set;
import nz.co.gregs.dbvolution.DBDatabase;
import nz.co.gregs.dbvolution.DBRow;
import nz.co.gregs.dbvolution.datatypes.DBBoolean;
import nz.co.gregs.dbvolution.datatypes.DBNumber;
import nz.co.gregs.dbvolution.datatypes.QueryableDatatype;

public class BooleanExpression implements BooleanResult {

    private final BooleanResult bool1;

    protected BooleanExpression() {
        bool1 = new DBBoolean();
    }

    public BooleanExpression(BooleanResult booleanResult) {
        bool1 = booleanResult;
    }

    public BooleanExpression(Boolean bool) {
        bool1 = new DBBoolean(bool);
    }

    @Override
    public String toSQLString(DBDatabase db) {
        return bool1.toSQLString(db);
    }

    @Override
    public BooleanExpression copy() {
        return new BooleanExpression(this.bool1);
    }

    /**
     * Create An Appropriate BooleanExpression Object For This Object
     *
     * <p>
     * The expression framework requires a *Expression to work with. The easiest
     * way to get that is the {@code DBRow.column()} method.
     *
     * <p>
     * However if you wish your expression to start with a literal value it is a
     * little trickier.
     *
     * <p>
     * This method provides the easy route to a *Expression from a literal
     * value. Just call, for instance,
     * {@code StringExpression.value("STARTING STRING")} to get a
     * StringExpression and start the expression chain.
     *
     * <ul>
     * <li>Only object classes that are appropriate need to be handle by the
     * DBExpression subclass.<li>
     * <li>The implementation should be {@code static}</li>
     *
     * @param bool
     * @return a DBExpression instance that is appropriate to the subclass and
     * the value supplied.
     */
    public static BooleanExpression value(Boolean bool) {
        return new BooleanExpression(bool);
    }

    /**
     * Collects the expressions together and requires them all to be true.
     *
     * <p>
     * Creates a BooleanExpression of several Boolean Expressions by connecting
     * them using AND repeatedly.
     *
     * <p>
     * This expression returns true if and only if all the component expressions
     * are true
     *
     * @param booleanExpressions
     * @return a boolean expression that returns true IFF all the
     * booleanExpressions are true.
     * @see #anyOf(nz.co.gregs.dbvolution.expressions.BooleanExpression...)
     */
    public static BooleanExpression allOf(final BooleanExpression... booleanExpressions) {
        return new BooleanExpression(new DBNnaryBooleanArithmetic(booleanExpressions) {
            @Override
            protected String getEquationOperator(DBDatabase db) {
                return db.getDefinition().beginAndLine();
            }

            @Override
            public boolean isAggregator() {
                boolean result = false;
                for (BooleanExpression boex : booleanExpressions) {
                    result = result || boex.isAggregator();
                }
                return result;
            }
        });
    }

    /**
     * Collects the expressions together and only requires one to be true.
     *
     * <p>
     * Creates a BooleanExpression of several Boolean Expressions by connecting
     * them using OR repeatedly.
     *
     * <p>
     * This expression returns true if any of the component expressions is true
     *
     * @param booleanExpressions
     * @return a boolean expression that returns true if any of the
     * booleanExpressions is true.
     * @see #allOf(nz.co.gregs.dbvolution.expressions.BooleanExpression...)
     */
    public static BooleanExpression anyOf(final BooleanExpression... booleanExpressions) {
        return new BooleanExpression(new DBNnaryBooleanArithmetic(booleanExpressions) {
            @Override
            protected String getEquationOperator(DBDatabase db) {
                return db.getDefinition().beginOrLine();
            }

            @Override
            public boolean isAggregator() {
                boolean result = false;
                for (BooleanExpression boex : booleanExpressions) {
                    result = result || boex.isAggregator();
                }
                return result;
            }
        });
    }

    /**
     * Negates this BooleanExpression.
     *
     * <p>
     * The 3 main boolean operators are AND, OR, and NOT. This method implements
     * NOT.
     *
     * <p>
     * The boolean result of the expression will be negated by this call so that
     * TRUE becomes FALSE and FALSE becomes TRUE.
     *
     * <p>
     * Please note that databases use
     * <a href="https://en.wikipedia.org/wiki/Three-valued_logic">Three-valued
     * logic</a>
     * so {@link QueryableDatatype#isDBNull NULL} is also a valid result of this
     * expression
     *
     * @return a Boolean expression representing the negation of the current
     * expression.
     */
    public BooleanExpression negate() {
        return new BooleanExpression(new DBUnaryBooleanArithmetic(this) {
            @Override
            protected String getEquationOperator(DBDatabase db) {
                return db.getDefinition().getNegationFunctionName();
            }

            @Override
            public boolean isAggregator() {
                return false;
            }
        });
    }

    /**
     * Returns FALSE if this expression is TRUE, or TRUE if it is FALSE.
     *
     * <p>
     * Synonym for {@link #negate() the negate() method}
     *
     * <p>
     * The 3 main boolean operators are AND, OR, and NOT. This method implements
     * NOT.
     *
     * <p>
     * The boolean result of the expression will be negated by this call so that
     * TRUE becomes FALSE and FALSE becomes TRUE.
     *
     * <p>
     * Please note that databases use
     * <a href="https://en.wikipedia.org/wiki/Three-valued_logic">Three-valued
     * logic</a>
     * so {@link QueryableDatatype#isDBNull NULL} is also a valid result of this
     * expression
     *
     * @return a Boolean expression representing the negation of the current
     * expression.
     */
    public BooleanExpression not() {
        return this.negate();
    }

    public NumberExpression count() {
        return new NumberExpression(new DBUnaryNumberFunction(this) {
            @Override
            String getFunctionName(DBDatabase db) {
                return db.getDefinition().getCountFunctionName();
            }

            @Override
            public boolean isAggregator() {
                return true;
            }
        });
    }

    @Override
    public DBBoolean getQueryableDatatypeForExpressionValue() {
        return new DBBoolean();
    }

    @Override
    public boolean isAggregator() {
        if (bool1 != null) {
            return bool1.isAggregator();
        } else {
            return false;
        }
    }

    @Override
    public Set<DBRow> getTablesInvolved() {
        return bool1 == null ? new HashSet<DBRow>() : bool1.getTablesInvolved();
    }

    private static abstract class DBUnaryBooleanArithmetic implements BooleanResult {

        private BooleanExpression bool;

        public DBUnaryBooleanArithmetic(BooleanExpression bool) {
            this.bool = bool.copy();
        }

        @Override
        public DBBoolean getQueryableDatatypeForExpressionValue() {
            return new DBBoolean();
        }

        @Override
        public String toSQLString(DBDatabase db) {
            String returnStr = "";
            String op = this.getEquationOperator(db);
            returnStr = op + " " + bool.toSQLString(db);
            return returnStr;
        }

        @Override
        public DBUnaryBooleanArithmetic copy() {
            DBUnaryBooleanArithmetic newInstance;
            try {
                newInstance = getClass().newInstance();
            } catch (InstantiationException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
            newInstance.bool = bool.copy();
            return newInstance;
        }

        @Override
        public Set<DBRow> getTablesInvolved() {
            return bool == null ? new HashSet<DBRow>() : bool.getTablesInvolved();
        }

        protected abstract String getEquationOperator(DBDatabase db);
    }

    private static abstract class DBNnaryBooleanArithmetic implements BooleanResult {

        private BooleanResult[] bools;

        public DBNnaryBooleanArithmetic(BooleanResult... bools) {
            this.bools = bools;
        }

        @Override
        public DBBoolean getQueryableDatatypeForExpressionValue() {
            return new DBBoolean();
        }

        @Override
        public String toSQLString(DBDatabase db) {
            String returnStr = "";
            String separator = "";
            String op = this.getEquationOperator(db);
            for (BooleanResult boo : bools) {
                returnStr += separator + boo.toSQLString(db);
                separator = op;
            }
            return returnStr;
        }

        @Override
        public DBNnaryBooleanArithmetic copy() {
            DBNnaryBooleanArithmetic newInstance;
            try {
                newInstance = getClass().newInstance();
            } catch (InstantiationException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
            newInstance.bools = new BooleanResult[bools.length];
            for (int i = 0; i < newInstance.bools.length; i++) {
                newInstance.bools[i] = bools[i].copy();
            }
            return newInstance;
        }

        @Override
        public Set<DBRow> getTablesInvolved() {
            HashSet<DBRow> hashSet = new HashSet<DBRow>();
            for (BooleanResult boo : bools) {
                hashSet.addAll(boo.getTablesInvolved());
            }
            return hashSet;
        }

        protected abstract String getEquationOperator(DBDatabase db);
    }

    private static abstract class DBUnaryNumberFunction implements NumberResult {

        protected BooleanExpression only;

        public DBUnaryNumberFunction() {
            this.only = null;
        }

        public DBUnaryNumberFunction(BooleanExpression only) {
            this.only = only;
        }

        @Override
        public DBNumber getQueryableDatatypeForExpressionValue() {
            return new DBNumber();
        }

        abstract String getFunctionName(DBDatabase db);

        protected String beforeValue(DBDatabase db) {
            return "" + getFunctionName(db) + "( ";
        }

        protected String afterValue(DBDatabase db) {
            return ") ";
        }

        @Override
        public String toSQLString(DBDatabase db) {
            return this.beforeValue(db) + (only == null ? "" : only.toSQLString(db)) + this.afterValue(db);
        }

        @Override
        public DBUnaryNumberFunction copy() {
            DBUnaryNumberFunction newInstance;
            try {
                newInstance = getClass().newInstance();
            } catch (InstantiationException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
            newInstance.only = (only == null ? null : only.copy());
            return newInstance;
        }

        @Override
        public boolean isAggregator() {
            return only.isAggregator();
        }

        @Override
        public Set<DBRow> getTablesInvolved() {
            return only.getTablesInvolved();
        }
    }
}