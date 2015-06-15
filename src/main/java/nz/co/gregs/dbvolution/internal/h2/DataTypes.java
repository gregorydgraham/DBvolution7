/*
 * Copyright 2015 gregorygraham.
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
package nz.co.gregs.dbvolution.internal.h2;

import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author gregorygraham
 */
public enum DataTypes implements DBVFeature{

	POINT2D("DBV_POINT2D", "VARCHAR(2000)", Point2DFunctions.values()),
	LINE2D("DBV_LINE2D", "VARCHAR(2001)", Line2DFunctions.values()),
	LINESEGMENT2D("DBV_LINESEGMENT2D", "VARCHAR(2001)", LineSegment2DFunctions.values()),
	POLYGON2D("DBV_POLYGON2D", "VARCHAR(2002)", Polygon2DFunctions.values()), 
	MULTIPOINT2D("DBV_MULTIPOINT2D", "VARCHAR(2003)", MultiPoint2DFunctions.values());
	private final String datatype;
	private final String actualType;
//	private final DBVFeature[] functions;

	DataTypes(String datatype, String actualType, DBVFeature[] functions) {
		this.datatype = datatype;
		this.actualType = actualType;
//		this.functions = functions;
	}

	@Override
	public String toString() {
		return datatype;
	}

	public void add(Statement stmt) throws SQLException {
//		try {
//			stmt.execute("DROP DOMAIN " + datatype + "; ");
//		} catch (SQLException sqlex) {
//			; // I don't care.
//		}
		stmt.execute("CREATE DOMAIN IF NOT EXISTS " + datatype + " AS " + actualType + "; ");

//		for (DBVFeature function : functions) {
//			function.add(stmt);
//		}
	}
	
	public static void addAll(Statement stmt) throws SQLException{
//		for (DataTypes datatype : values()) {
//			datatype.add(stmt);
//		}
	}

	public String datatype() {
		return datatype;
	}

	@Override
	public String alias() {
		return toString();
	}

}