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
package nz.co.gregs.dbvolution.example;

import java.io.FileNotFoundException;
import java.io.IOException;
import nz.co.gregs.dbvolution.*;
import nz.co.gregs.dbvolution.annotations.*;

/**
 *
 * @author gregorygraham
 */
@DBTableName("car_company")
public class CarCompany  extends DBRow {
    
    @DBColumn("name")
    public DBString name =new DBString();
    
    @DBPrimaryKey
    @DBColumn("uid_carcompany")
    public DBInteger uidCarCompany = new DBInteger();
    
    public CarCompany() {
    }
    
    public CarCompany(String anme, int id){
        this.name.setValue(anme);
        this.uidCarCompany.setValue(id);
    }
}
