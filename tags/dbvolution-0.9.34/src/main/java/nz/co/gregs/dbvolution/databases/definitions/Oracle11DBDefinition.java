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
package nz.co.gregs.dbvolution.databases.definitions;

import java.util.ArrayList;
import java.util.List;
import nz.co.gregs.dbvolution.DBDatabase;
import nz.co.gregs.dbvolution.query.QueryOptions;

public class Oracle11DBDefinition extends OracleDBDefinition {

	@Override
	public Object getLimitRowsSubClauseDuringSelectClause(QueryOptions options) {
		return "/*+ FIRST_ROWS(" + options.getRowLimit() + ") */";
	}

	@Override
	public Object getLimitRowsSubClauseAfterWhereClause(QueryOptions options) {
		return "";
	}

	@Override
	public boolean supportsPaging(QueryOptions options) {
		return false;
	}

	@Override
	public String getColumnAutoIncrementSuffix() {
		return "";
	}

	@Override
	public boolean usesTriggerBasedIdentities() {
		return true;
	}

	@Override
	public List<String> getTriggerBasedIdentitySQL(DBDatabase DB, String table, String column) {
//		    CREATE SEQUENCE dept_seq;
//
//Create a trigger to populate the ID column if it's not specified in the insert.
//
//    CREATE OR REPLACE TRIGGER dept_bir 
//    BEFORE INSERT ON departments 
//    FOR EACH ROW
//    WHEN (new.id IS NULL)
//    BEGIN
//      SELECT dept_seq.NEXTVAL
//      INTO   :new.id
//      FROM   dual;
//    END;

		List<String> result = new ArrayList<String>();
		String sequenceName = getPrimaryKeySequenceName(table, column);
		result.add("CREATE SEQUENCE " + sequenceName);

		String triggerName = getPrimaryKeyTriggerName(table, column);
		result.add("CREATE OR REPLACE TRIGGER "+DB.getUsername()+"." + triggerName + " \n"
				+ "    BEFORE INSERT ON "+DB.getUsername()+"." + table + " \n"
				+ "    FOR EACH ROW\n"
				+ "    WHEN (new." + column + " IS NULL)\n"
				+ "    BEGIN\n"
				+ "      SELECT " + sequenceName + ".NEXTVAL\n"
				+ "      INTO   :new." + column + "\n"
				+ "      FROM   dual;\n"
//				+ ":new."+column+" := "+sequenceName+".nextval; \n"
				+ "    END;\n");

		return result;
	}

	@Override
	public String getStringLengthFunctionName() {
		return "LENGTH";
	}

	@Override
	public String doSubstringTransform(String originalString, String start, String length) {
		return " SUBSTR("
				+ originalString
				+ ", "
				+ start
				+ (length.trim().isEmpty() ? "" : ", " + length)
				+ ") ";
	}
}