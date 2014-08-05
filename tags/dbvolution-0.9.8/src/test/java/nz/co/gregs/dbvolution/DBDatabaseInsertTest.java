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
package nz.co.gregs.dbvolution;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import nz.co.gregs.dbvolution.actions.DBActionList;
import nz.co.gregs.dbvolution.example.CarCompany;
import nz.co.gregs.dbvolution.example.Marque;
import nz.co.gregs.dbvolution.generic.AbstractTest;
import org.junit.Assert;
import org.junit.Test;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author gregory.graham
 */
public class DBDatabaseInsertTest extends AbstractTest{

    public DBDatabaseInsertTest(Object testIterationName, Object db) {
        super(testIterationName, db);
    }
    
    
    
    
    @Test
    public void testInsertRows() throws SQLException{
        int originalNumberOfMarques = marques.setBlankQueryAllowed(true).getAllRows().toList().size();
        int originalNumberOfCarCos = database.getDBTable(new CarCompany()).setBlankQueryAllowed(true).getAllRows().toList().size();

        Marque newMarque1 = new Marque();
        newMarque1.getUidMarque().setValue(999);
        newMarque1.getName().permittedValues("TOYOTA");
        newMarque1.getNumericCode().permittedValues(10);
        
        Date creationDate = new Date();
        List<Marque> myTableRows = new ArrayList<Marque>();
        Marque newMarque2 = new Marque(3, "False", 1246974, "", 3, "UV", "TVR", "", "Y",creationDate, 4,null);
        myTableRows.add(newMarque1);
        myTableRows.add(newMarque2);
        CarCompany carCompany = new CarCompany("TATA", 569);
        DBActionList changes = database.insert(myTableRows, carCompany);
        marques.getAllRows();
        marques.print();
        database.getDBTable(carCompany).setBlankQueryAllowed(true).getAllRows().print();
        Assert.assertThat(changes.size(), is(3));
        Assert.assertThat(marques.getAllRows().toList().size(), is(originalNumberOfMarques+2));
        Assert.assertThat(
                database.getDBTable(carCompany).setBlankQueryAllowed(true).getAllRows().toList().size(), 
                is(originalNumberOfCarCos+1));
    }
    
}