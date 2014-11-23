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
import nz.co.gregs.dbvolution.datatypes.DBBoolean;
import nz.co.gregs.dbvolution.datatypes.DBDate;
import nz.co.gregs.dbvolution.datatypes.QueryableDatatype;

/**
 *
 * @author gregory.graham
 */
public class NuoDBDefinition extends DBDefinition{

	private static final DateFormat DATETIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	@Override
	@SuppressWarnings("deprecation")
	public String getDateFormattedForQuery(Date date) {

		return " DATE_FROM_STR('" + DATETIME_FORMAT.format(date) + "', 'dd/MM/yyyy HH:mm:ss') ";

	}

	@Override
	protected String getSQLTypeOfDBDatatype(QueryableDatatype qdt) {
		if (qdt instanceof DBBoolean) {
			return " boolean ";
		} else if (qdt instanceof DBDate) {
			return " TIMESTAMP(0) ";
//		} else if (qdt instanceof DBByteArray) {
//			return " LONGBLOB ";
//		} else if (qdt instanceof DBLargeObject) {
//			return " LONGBLOB ";
		} else {
			return qdt.getSQLDatatype();
		}
	}

	@Override
	public String doTruncTransform(String firstString, String secondString) {
		//A1-MOD(A1,1*(A1/ABS(A1)))
		return ""+firstString+"-MOD("+firstString+",1*("+firstString+"/ABS("+firstString+")))";
	}

	@Override
	public boolean supportsExpFunction() {
		return false;
	}

	@Override
	public String getCurrentUserFunctionName() {
		return "USER()";
	}

	@Override
	public boolean supportsStandardDeviationFunction() {
		return false;
	}

//	@Override
//	public String getEmptyString() {
//		return "\"\"";
//	}
	
	@Override
	public boolean supportsLeftTrimFunction() {
		return false;
	}
	
	/**
	 * God-awful hack to get past a bug in NuoDB LTRIM.
	 * 
	 * <p>
	 * To be removed as soon as NuoDB fixes the bug.
	 * 
	 * @param toSQLString
	 * @return a hack masquerading as SQL.
	 * @deprecated 
	 */
	@Override
	@Deprecated
	public String doLeftTrimFunction(String toSQLString) {
		return " (("+toSQLString+") not like '% ') and LTRIM("+toSQLString+")";
	}


	
}