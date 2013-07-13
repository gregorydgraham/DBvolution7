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
package nz.co.gregs.dbvolution.h2;

import java.sql.SQLException;
import nz.co.gregs.dbvolution.DBQuery;
import nz.co.gregs.dbvolution.example.CarCompany;
import nz.co.gregs.dbvolution.example.Marque;

/**
 *
 * @author gregorygraham
 */
public class AdHocRelationshipTest extends AbstractTest{

    public AdHocRelationshipTest(String name) {
        super(name);
    }
    
    public void testAdHocRelationshipTest() throws SQLException{
        Marque marque = new Marque();
        CarCompany carCompany = new CarCompany();
        
        marque.addRelationship(marque.name, carCompany, carCompany.name);
        
        DBQuery query = new DBQuery(myDatabase, carCompany, marque);
        
        query.getAllRows();
        query.printAllRows();
        
        assertTrue("There should only be rows for FORD and TOYOTA", query.getAllRows().size()==2);
    }
    
    
}
