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
package nz.co.gregs.dbvolution.generic;

import java.sql.SQLException;
import java.util.List;
import nz.co.gregs.dbvolution.example.Marque;
import org.junit.Assert;
import org.junit.Test;
import static org.hamcrest.Matchers.*;

public class UpdateTest extends AbstractTest {

    public UpdateTest(Object db) {
        super(db);
    }

    @Test
    public void updateNewRow() throws SQLException {
        myMarqueRow.uidMarque.permittedValues(4);
        marques.insert(myMarqueRow);
        Marque insertedRow = marques.getRowsByPrimaryKey(4).getFirstRow();
        insertedRow.individualAllocationsAllowed.permittedValues("Y");
        String sqlForUpdate = marques.getSQLForUpdate(insertedRow);
        Assert.assertThat(sqlForUpdate.toLowerCase(),
                is("UPDATE MARQUE SET INTINDALLOCALLOWED = 'Y' WHERE UID_MARQUE = 4;".toLowerCase()));
        marques.update(insertedRow);
        insertedRow = marques.getRowsByPrimaryKey(4).getFirstRow();
        Assert.assertThat(insertedRow.individualAllocationsAllowed.toString(), is("Y"));
    }

    @Test
    public void updateExistingRow() throws SQLException {
        Marque marque = new Marque();
        marque.name.permittedValues("PEUGEOT");
        List<Marque> rowsByExample = marques.getRowsByExample(marque).toList();
        Assert.assertThat(rowsByExample.size(), is(1));
        Marque peugeot = rowsByExample.get(0);
        System.out.println(peugeot);
        peugeot.individualAllocationsAllowed.permittedValues("Y");
        String sqlForUpdate = marques.getSQLForUpdate(peugeot);
        Assert.assertThat(sqlForUpdate.toLowerCase(),
                is("UPDATE MARQUE SET INTINDALLOCALLOWED = 'Y' WHERE UID_MARQUE = 4893059;".toLowerCase()));
        marques.update(peugeot);
        Marque updatePeugeot = marques.getRowsByExample(marque).getFirstRow();
        Assert.assertThat(updatePeugeot.individualAllocationsAllowed.toString(), is("Y"));
    }
}