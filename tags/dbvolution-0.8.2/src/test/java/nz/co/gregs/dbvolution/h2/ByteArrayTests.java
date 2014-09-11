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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import nz.co.gregs.dbvolution.UnexpectedNumberOfRowsException;
import nz.co.gregs.dbvolution.example.CompanyLogo;
import org.junit.Test;

/**
 *
 * @author gregorygraham
 */
public class ByteArrayTests extends AbstractTest {

    @Override
    public void setUp() throws Exception {
        super.setUp(); //To change body of generated methods, choose Tools | Templates.
        CompanyLogo companyLogo = new CompanyLogo();
        myDatabase.dropTableNoExceptions(companyLogo);
        myDatabase.createTable(companyLogo);
    }

    @Override
    public void tearDown() throws Exception {
        myDatabase.dropTableNoExceptions(new CompanyLogo());
        super.tearDown(); //To change body of generated methods, choose Tools | Templates.
    }
    
    

    @Test
    public void createRowWithByteArray() throws FileNotFoundException, IOException, SQLException {

        CompanyLogo companyLogo = new CompanyLogo();
        companyLogo.logoID.setValue(1);
        companyLogo.carCompany.setValue(1);//Toyota
        companyLogo.imageFilename.setValue("toyota_logo.jpg");
        companyLogo.imageBytes.readFromFileSystem("toyota_share_logo.jpg");
        myDatabase.getDBTable(companyLogo).insert(companyLogo);
    }

    @Test
    public void retrieveRowWithByteArray() throws FileNotFoundException, IOException, SQLException, UnexpectedNumberOfRowsException {

        CompanyLogo companyLogo = new CompanyLogo();
        companyLogo.logoID.setValue(1);
        companyLogo.carCompany.setValue(1);//Toyota
        companyLogo.imageFilename.setValue("toyota_logo.jpg");
        companyLogo.imageBytes.readFromFileSystem("toyota_share_logo.jpg");
        myDatabase.insert(companyLogo);

        companyLogo = new CompanyLogo();
        companyLogo.logoID.permittedValues(1);
        CompanyLogo firstRow = myDatabase.getDBTable(companyLogo).getOnlyRowByExample(companyLogo);
        System.out.println("" + firstRow.toString());
        
    }
}