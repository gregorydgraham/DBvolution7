/*
 * Copyright 2013 Gregory Graham.
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

import nz.co.gregs.dbvolution.datatypes.DBString;
import nz.co.gregs.dbvolution.datatypes.DBInteger;
import nz.co.gregs.dbvolution.*;
import nz.co.gregs.dbvolution.annotations.*;

/**
 *
 * @author Gregory Graham
 */
@DBTableName("car_company")
public class CarCompany extends DBRow {

    public static final long serialVersionUID = 1L;

    @DBColumn("name")
    public DBString name = new DBString();
    @DBPrimaryKey
    @DBColumn("uid_carcompany")
    public DBInteger uidCarCompany = new DBInteger();

    public CarCompany() {
    }

    public CarCompany(String anme, int id) {
        this.name.setValue(anme);
        this.uidCarCompany.setValue(id);
    }
}