package nz.co.gregs.dbvolution.mysql;

import java.io.File;
import java.io.IOException;
import nz.co.gregs.dbvolution.databases.MySQLDB;
import nz.co.gregs.dbvolution.databases.MySQLMXJDB;

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
/**
 *
 * @author gregorygraham
 */
public class MySQLMXJDBInitialisation {

    public synchronized static MySQLDB getMySQLDBInstance() throws IOException {

        int port = Integer.parseInt(System.getProperty("c-mxj_test_port", "3336"));
        
        File ourAppDir = new File(System.getProperty("java.io.tmpdir"));
        File databaseDir = new File(ourAppDir, "test-mxj" + port);
        String databaseName = "dbvolutiontest";


        MySQLDB database = new MySQLMXJDB("localhost", port, databaseName, databaseDir.toString(), "dbvtest", "testpass");
        return database;
    }

	private MySQLMXJDBInitialisation() {
	}
}
