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
package nz.co.gregs.dbvolution.databases;

import nz.co.gregs.dbvolution.DBDatabase;
import java.sql.SQLException;
import nz.co.gregs.dbvolution.databases.definitions.InformixDBDefinition;

/**
 *
 * @author gregory.graham
 */
public class InformixDB extends DBDatabase {

    public final static String INFORMIXDRIVERNAME = "com.informix.jdbc.IfxDriver";

    public InformixDB(String jdbcURL, String username, String password) throws ClassNotFoundException, SQLException {
        super(new InformixDBDefinition(), INFORMIXDRIVERNAME, jdbcURL, username, password);
        // Informix causes problems when using batched statements :(
        setBatchSQLStatementsWhenPossible(false);
    }
}