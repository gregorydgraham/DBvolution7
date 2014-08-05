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
package nz.co.gregs.dbvolution.actions;

import java.sql.SQLException;
import java.util.List;
import nz.co.gregs.dbvolution.DBDatabase;
import nz.co.gregs.dbvolution.DBRow;

/**
 *
 * @author gregorygraham
 */
public abstract class DBAction {

    protected final DBRow row;

    protected abstract List<String> getSQLStatements(DBDatabase db, DBRow row);
    protected abstract DBActionList getRevertDBActionList();
    protected abstract DBActionList getActions(DBRow row);

    DBAction() {
        super();
        row = null;
    }

    /**
     * Standard action constructor
     * 
     * saves a copy of the row to ensure immutability
     *
     * @param <R>
     * @param row
     */
    public <R extends DBRow> DBAction(R row) {
        super();
        this.row = DBRow.copyDBRow(row);
    }

    /**
     * Actions happen all by themselves but when you want to know what will actually happen on the database
     * use this method to get a complete list of all the SQL required.
     *
     * @param db
     * @return the list of SQL strings that equates to this action.
     */
    public final List<String> getSQLStatements(DBDatabase db) {
        return getSQLStatements(db, row);
    }

    /**
     *
     * This method performs the DB action and returns a list of all actions
     * perform in the process.
     *
     * The supplied row will be changed by the action in an appropriate way,
     * however the Action's will contain an unchanged and unchangeable copy of
     * the row for internal use.
     *
     * @param db
     * @param row
     * @return The complete list of all actions performed to complete this action on the database
     * @throws SQLException
     */
    protected abstract DBActionList execute(DBDatabase db, DBRow row) throws SQLException;

    /**
     * This method performs the DB action.
     *
     * The Action's internal information will not be changed and can be repeated
     *
     * @param database
     * @return the actions executed as a DBActionList
     * @throws SQLException
     */
    protected final DBActionList execute(DBDatabase database) throws SQLException{
        return this.execute(database,DBRow.copyDBRow(row));
    }

}