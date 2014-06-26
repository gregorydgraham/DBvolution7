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
package nz.co.gregs.dbvolution.generation;

import java.util.HashMap;
import java.util.Map;

import nz.co.gregs.dbvolution.generation.ast.ParsedField;


/**
 * Helps to resolve database column names to class names,
 * and vice-versa, while avoiding duplications of names.
 */
// TODO: implement smarts
public class ColumnNameResolver {
	// maps column-name to property-name
	// maps to null to represent that column/property name is known of, but has no mapping as yet
	private Map<String,String> columnNameToPropertyNameMap = new HashMap<String, String>();
	private Map<String,String> propertyNameToColumnNameMap = new HashMap<String, String>();
	private Map<String,ParsedField> propertyNameToPropertyMap = new HashMap<String, ParsedField>();
	
	public ColumnNameResolver() {
	}
	
	public void addColumn(String columnName) {
		// don't overwrite if already have map from databaseName to class
		if (!columnNameToPropertyNameMap.containsKey(columnName)) {
			columnNameToPropertyNameMap.put(columnName, null);
		}
	}

	public void addClass(String propertyName, String columnName) {
		propertyNameToColumnNameMap.put(propertyName, columnName);
	}
	
	public void addClass(ParsedField parsedField) {
		String columnName = parsedField.getColumnNameIfSet(); // may be null
		propertyNameToColumnNameMap.put(parsedField.getName(), columnName);
		propertyNameToPropertyMap.put(parsedField.getName(), parsedField);
	}
	
	/**
	 * Gets the unique class name allocated to the column.
	 * @param columnName
	 * @return the simple class name
	 */
	// TODO: actually implement duplication avoidance logic
	public String getPropertyNameFor(String columnName) {
		String propertyName = columnNameToPropertyNameMap.get(columnName);
		if (propertyName != null) {
			return propertyName;
		}
		
		propertyName = DBTableClassGenerator.toFieldCase(columnName);
		addClass(propertyName, columnName);
		return propertyName;
	}
	
	
}