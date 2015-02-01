/*
 * Copyright 2014 gregory.graham.
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import nz.co.gregs.dbvolution.DBRow;
import nz.co.gregs.dbvolution.datatypes.DBDate;
import nz.co.gregs.dbvolution.datatypes.DBInteger;
import nz.co.gregs.dbvolution.datatypes.DBLargeObject;
import nz.co.gregs.dbvolution.datatypes.QueryableDatatype;
import nz.co.gregs.dbvolution.generation.DBTableField;
import nz.co.gregs.dbvolution.query.QueryOptions;

/**
 *
 * @author gregory.graham
 */
public class SQLiteDefinition extends DBDefinition {

	private static final DateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Override
	public String getDateFormattedForQuery(Date date) {
		//%Y-%m-%d %H:%M:%S.%s
//		return " STRFTIME('%Y-%m-%d %H:%M:%S', '" + DATETIME_FORMAT.format(date) + "') ";
//		return " '" + DATETIME_FORMAT.format(date) + "' ";
		return " DATETIME('" + DATETIME_FORMAT.format(date) + "') ";
	}


	@Override
	public boolean supportsGeneratedKeys(QueryOptions options) {
		return false;
	}

	@Override
	public String formatTableName(DBRow table) {
		return super.formatTableName(table).toUpperCase();
	}

	@Override
	public String getDropTableStart() {
		return super.getDropTableStart() + " IF EXISTS "; //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean prefersTrailingPrimaryKeyDefinition() {
		return false;
	}

	@Override
	public String getColumnAutoIncrementSuffix() {
		return " PRIMARY KEY AUTOINCREMENT ";
	}

	@Override
	protected String getSQLTypeOfDBDatatype(QueryableDatatype qdt) {
		if (qdt instanceof DBLargeObject) {
			return " TEXT ";
		} else if (qdt instanceof DBDate) {
			return " DATETIME ";
		} else {
			return super.getSQLTypeOfDBDatatype(qdt);
		}
	}

	@Override
	public void sanityCheckDBTableField(DBTableField dbTableField) {
		if (dbTableField.isPrimaryKey && dbTableField.columnType.equals(DBInteger.class)) {
			dbTableField.isAutoIncrement = true;
		}
	}

	@Override
	public boolean prefersLargeObjectsReadAsBase64CharacterStream() {
		return true;
	}

	@Override
	public boolean prefersLargeObjectsSetAsBase64String() {
		return true;
	}

	@Override
	public String doSubstringTransform(String originalString, String start, String length) {
		return " SUBSTR("
				+ originalString
				+ ", "
				+ start
				+ ","
				+ length
				+ ") ";
	}

	@Override
	public String getCurrentDateFunctionName() {
		return " DATETIME('now') ";
	}

	@Override
	public String getStringLengthFunctionName() {
		return "LENGTH";
	}

	@Override
	public String getTruncFunctionName() {
		// TRUNC is defined in SQLiteDB as a user defined function.
		return "TRUNC";
	}

	@Override
	public String getPositionFunction(String originalString, String stringToFind) {
		return "LOCATION_OF(" + originalString + ", " + stringToFind + ")";
	}

	@Override
	public String getCurrentUserFunctionName() {
		return "CURRENT_USER()";
	}

	@Override
	public String getStandardDeviationFunctionName() {
		return "STDEV";
	}

	@Override
	public String getMonthFunction(String dateExpression) {
		return " (CAST(strftime('%m', " + dateExpression + ") as INTEGER))";
	}

	@Override
	public String getYearFunction(String dateExpression) {
		return " (CAST(strftime('%Y', " + dateExpression + ") as INTEGER))";
	}

	@Override
	public String getDayFunction(String dateExpression) {
		return " (CAST(strftime('%d', " + dateExpression + ") as INTEGER))";
	}

	@Override
	public String getHourFunction(String dateExpression) {
		return " (CAST(strftime('%H', " + dateExpression + ") as INTEGER))";
	}

	@Override
	public String getMinuteFunction(String dateExpression) {
		return " (CAST(strftime('%M', " + dateExpression + ") as INTEGER))";
	}

	@Override
	public String getSecondFunction(String dateExpression) {
		return " (CAST(strftime('%S', " + dateExpression + ") as INTEGER))";
	}

	@Override
	public String getGreatestOfFunctionName() {
		return " MAX "; //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public String getLeastOfFunctionName() {
		return " MIN "; //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean prefersDatesReadAsStrings() {
		return true;
	}

	@Override
	public DateFormat getDateGetStringFormat() {
		return DATETIME_FORMAT;
	}
	@Override
	public boolean supportsRetrievingLastInsertedRowViaSQL() {
		return true;
	}

	@Override
	public String getRetrieveLastInsertedRowSQL() {
		return "select last_insert_rowid();";
	}
}