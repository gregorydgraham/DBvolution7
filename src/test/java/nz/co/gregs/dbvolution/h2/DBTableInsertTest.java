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
package nz.co.gregs.dbvolution.h2;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import nz.co.gregs.dbvolution.example.Marque;

/**
 *
 * @author gregory.graham
 */
public class DBTableInsertTest extends AbstractTest {
    
    Marque myTableRow = new Marque();
    
    public DBTableInsertTest(String testName) {
        super(testName);
    }

    public void testInsertRows() throws IllegalArgumentException, IllegalAccessException, IntrospectionException, InvocationTargetException, SQLException, InstantiationException, NoSuchMethodException {
        myTableRow.getUidMarque().isLiterally(999);
        myTableRow.getName().isLiterally("TOYOTA");
        myTableRow.getNumericCode().isLiterally(10);
        marques.insert(myTableRow);
        marques.getAllRows();
        marques.printRows();
        
        Date creationDate = new Date();
        List<Marque> myTableRows = new ArrayList<Marque>();
        myTableRows.add(new Marque(3, "False", 1246974, "", 3, "UV", "TVR", "", "Y",creationDate, 4));
        
        marques.insert(myTableRows);
        marques.getAllRows();
        marques.printRows();
    }
}
