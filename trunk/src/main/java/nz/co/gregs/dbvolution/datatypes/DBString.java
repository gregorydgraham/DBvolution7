/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.gregs.dbvolution.datatypes;

import java.util.Date;
import nz.co.gregs.dbvolution.DBDatabase;
import nz.co.gregs.dbvolution.databases.definitions.DBDefinition;
import nz.co.gregs.dbvolution.operators.DBOperator;

/**
 *
 * @author gregory.graham
 */
public class DBString extends QueryableDatatype {

    private static final long serialVersionUID = 1L;

    public DBString() {
        super();
    }

    public void setValue(String str) {
        super.setValue(str);
    }

    @Override
    public String getSQLDatatype() {
        return "VARCHAR(1000)";
    }

    @Override
    public String formatValueForSQLStatement(DBDatabase db) {
        DBDefinition defn = db.getDefinition();
        
        if (literalValue instanceof Date) {
            return defn.getDateFormattedForQuery((Date) literalValue);
        } else {
            String unsafeValue = literalValue.toString();
            return defn.beginStringValue() + defn.safeString(unsafeValue) + defn.endStringValue();
        }
//    }
    }

}
