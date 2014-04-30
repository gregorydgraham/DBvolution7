package nz.co.gregs.dbvolution.postgres;


import java.sql.SQLException;
import nz.co.gregs.dbvolution.databases.PostgresDB;
import nz.co.gregs.dbvolution.example.*;
import org.junit.Test;

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
/**
 *
 * @author gregory.graham
 */
public class PostgresDBInitialisationTest {

//    @Test
    public void createMySQLInstance() throws SQLException {
        final Marque marque = new Marque();
        final CarCompany carCompany = new CarCompany();
        PostgresDB database = new PostgresDB("ec2-23-21-104-121.compute-1.amazonaws.com", "5432", "", "postgres", "postgres");
        database.dropTableNoExceptions(marque);
        database.dropTableNoExceptions(carCompany);
        database.createTable(marque);
        database.createTable(carCompany);
        database.dropTableNoExceptions(marque);
        database.dropTableNoExceptions(carCompany);
    }
}