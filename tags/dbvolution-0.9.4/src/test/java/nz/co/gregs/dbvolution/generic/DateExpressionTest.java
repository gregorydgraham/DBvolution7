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
package nz.co.gregs.dbvolution.generic;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import nz.co.gregs.dbvolution.DBQuery;
import nz.co.gregs.dbvolution.example.Marque;
import nz.co.gregs.dbvolution.expressions.DateExpression;
import nz.co.gregs.dbvolution.operators.DBEqualsOperator;
import static org.hamcrest.Matchers.is;
import org.junit.Assert;
import org.junit.Test;

public class DateExpressionTest extends AbstractTest {

    public DateExpressionTest(Object testIterationName, Object db) {
        super(testIterationName, db);
    }

    @Test
    public void testCurrentDateFunction() throws SQLException {
//        database.setPrintSQLBeforeExecuting(true);
        Marque marq = new Marque();
        marq.creationDate.permittedRangeInclusive(DateExpression.currentDate(), null);
        List<Marque> got = database.get(marq);
//        database.print(got);
        Assert.assertThat(got.size(), is(0));

        database.insert(new Marque(3, "False", 1246974, "", 0, "", "     HUMMER               ", "", "Y", new Date(), 3, null));
        marq.creationDate.permittedRangeInclusive(DateExpression.currentDate(), null);
        got = database.get(marq);
//        database.print(got);
        Assert.assertThat(got.size(), is(1));

        marq.creationDate.permittedRangeInclusive(null, DateExpression.currentDate());
        got = database.get(marq);
//        database.print(got);
        Assert.assertThat(got.size(), is(21));
    }

    @Test
    public void testYearFunction() throws SQLException {
//        database.setPrintSQLBeforeExecuting(true);
        Marque marq = new Marque();
        DBQuery query = database.getDBQuery(marq);
        query.addComparison(
                marq.column(marq.creationDate).year(),
                new DBEqualsOperator(2014));
        List<Marque> got = query.getAllInstancesOf(marq);
        database.print(got);
        Assert.assertThat(got.size(), is(0));

        query = database.getDBQuery(marq);
        query.addComparison(
                marq.column(marq.creationDate).year(),
                new DBEqualsOperator(2013));
        got = query.getAllInstancesOf(marq);
        database.print(got);
        Assert.assertThat(got.size(), is(21));

    }

    @Test
    public void testMonthFunction() throws SQLException {
//        database.setPrintSQLBeforeExecuting(true);
        Marque marq = new Marque();
        DBQuery query = database.getDBQuery(marq);
        query.addComparison(
                marq.column(marq.creationDate).month(),
                new DBEqualsOperator(3));
        List<Marque> got = query.getAllInstancesOf(marq);
        database.print(got);
        Assert.assertThat(got.size(), is(18));

        query = database.getDBQuery(marq);
        query.addComparison(
                marq.column(marq.creationDate).month(),
                new DBEqualsOperator(4));
        got = query.getAllInstancesOf(marq);
        database.print(got);
        Assert.assertThat(got.size(), is(3));

    }

    @Test
    public void testDayFunction() throws SQLException {
//        database.setPrintSQLBeforeExecuting(true);
        Marque marq = new Marque();
        DBQuery query = database.getDBQuery(marq);
        query.addComparison(
                marq.column(marq.creationDate).day(),
                new DBEqualsOperator(23));
        List<Marque> got = query.getAllInstancesOf(marq);
        database.print(got);
        Assert.assertThat(got.size(), is(18));

        query = database.getDBQuery(marq);
        query.addComparison(
                marq.column(marq.creationDate).day(),
                new DBEqualsOperator(2));
        got = query.getAllInstancesOf(marq);
        database.print(got);
        Assert.assertThat(got.size(), is(3));

    }
}