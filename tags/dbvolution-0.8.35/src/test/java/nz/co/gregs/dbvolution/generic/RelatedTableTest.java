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
package nz.co.gregs.dbvolution.generic;

import java.util.List;
import nz.co.gregs.dbvolution.DBRow;
import nz.co.gregs.dbvolution.example.*;
import org.junit.Assert;
import org.junit.Test;
import static org.hamcrest.Matchers.*;

public class RelatedTableTest extends AbstractTest {

    public RelatedTableTest(Object testIterationName, Object db) {
        super(testIterationName, db);
    }

    @Test
    public void getReferencedTablesTest() {
        Marque marque = new Marque();
        
        List<Class<? extends DBRow>> allReferencedTables = (new CarCompany()).getReferencedTables();
        Assert.assertThat(allReferencedTables.size(), is(0));
        
        allReferencedTables = marque.getReferencedTables();
        Assert.assertThat(allReferencedTables.size(), is(1));
        Assert.assertThat(allReferencedTables.contains(CarCompany.class), is(true));
        

        allReferencedTables = (new LinkCarCompanyAndLogo()).getReferencedTables();
        Assert.assertThat(allReferencedTables.size(), is(2));
        Assert.assertThat(allReferencedTables.contains(CarCompany.class), is(true));
        Assert.assertThat(allReferencedTables.contains(CompanyLogo.class), is(true));
    }

    @Test
    public void getRelatedTablesTest() {
        Marque marque = new Marque();
        
        List<Class<? extends DBRow>> allRelatedTables = (new CarCompany()).getAllRelatedTables();
        Assert.assertThat(allRelatedTables.size(), is(3));
        Assert.assertThat(allRelatedTables.contains(Marque.class), is(true));
        Assert.assertThat(allRelatedTables.contains(CompanyLogo.class), is(true));
        Assert.assertThat(allRelatedTables.contains(LinkCarCompanyAndLogo.class), is(true));
        
        allRelatedTables = marque.getAllRelatedTables();
        Assert.assertThat(allRelatedTables.size(), is(1));
        Assert.assertThat(allRelatedTables.contains(CarCompany.class), is(true));
        

        allRelatedTables = (new LinkCarCompanyAndLogo()).getAllRelatedTables();
        Assert.assertThat(allRelatedTables.size(), is(2));
        Assert.assertThat(allRelatedTables.contains(CarCompany.class), is(true));
        Assert.assertThat(allRelatedTables.contains(CompanyLogo.class), is(true));
    }
}