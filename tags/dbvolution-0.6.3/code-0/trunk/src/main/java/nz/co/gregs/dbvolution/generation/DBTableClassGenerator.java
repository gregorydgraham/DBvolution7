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
package nz.co.gregs.dbvolution.generation;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import nz.co.gregs.dbvolution.databases.DBDatabase;
import nz.co.gregs.dbvolution.databases.InformixDB;

/**
 *
 * @author gregorygraham
 */
public class DBTableClassGenerator {

    /**
     *
     * Creates DBTableRow classes corresponding to all the tables and views
     * accessible to the user specified in the database supplied.
     *
     * Classes are placed in the correct subdirectory of the base directory as
     * defined by the package name supplied.
     *
     * @param jdbcURL
     * @param username
     * @param password
     * @param packageName
     * @param baseDirectory
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws IntrospectionException
     * @throws InvocationTargetException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void generateClassesFromJDBCURLToDirectory(String jdbcURL, String username, String password, String packageName, String baseDirectory) throws ClassNotFoundException, SQLException, IllegalArgumentException, IllegalAccessException, IntrospectionException, InvocationTargetException, FileNotFoundException, IOException {
        InformixDB informixDB = new InformixDB(jdbcURL, username, password);

        String viewsPackage = packageName + ".views";
        String viewsPath = viewsPackage.replaceAll("[.]", "/");
        List<DBTableClass> generatedViews = DBTableClassGenerator.generateClassesOfViews(informixDB, viewsPackage);
        File dir = new File(baseDirectory + "/" + viewsPath);
        if (dir.mkdirs()) {
            saveGenerateClassesToDirectory(generatedViews, dir);
        }

        String tablesPackage = packageName + ".tables";
        String tablesPath = tablesPackage.replaceAll("[.]", "/");
        List<DBTableClass> generatedTables = DBTableClassGenerator.generateClassesOfTables(informixDB, tablesPackage);
        dir = new File(baseDirectory + "/" + tablesPath);
        if (dir.mkdirs()) {
            saveGenerateClassesToDirectory(generatedTables, dir);
        }
    }

    /**
     *
     * Saves the supplied DBTableRow classes as java files in the supplied
     * directory.
     *
     * No database interaction nor package name checking is performed.
     *
     * You probably want to use generateClassesFromJDBCURLToDirectory
     *
     * @param generatedClasses
     * @param classDirectory
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws IntrospectionException
     * @throws InvocationTargetException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void saveGenerateClassesToDirectory(List<DBTableClass> generatedClasses, File classDirectory) throws ClassNotFoundException, SQLException, IllegalArgumentException, IllegalAccessException, IntrospectionException, InvocationTargetException, FileNotFoundException, IOException {
        {
            File file;
            FileOutputStream fileOutputStream;
            for (DBTableClass clazz : generatedClasses) {
                System.out.println(clazz.className+" => "+classDirectory.getAbsolutePath()+"/"+clazz.className + ".java");
                file = new File(classDirectory, clazz.className + ".java");
                fileOutputStream = new FileOutputStream(file);
                System.out.println(clazz.javaSource);
                System.out.println("");
                fileOutputStream.write(clazz.javaSource.getBytes());
                fileOutputStream.close();
            }
        }
    }

    /**
     *
     * @param database
     * @param packageName
     * @return
     * @throws SQLException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws IntrospectionException
     * @throws InvocationTargetException
     */
    public static List<DBTableClass> generateClassesOfTables(DBDatabase database, String packageName) throws SQLException, IllegalArgumentException, IllegalAccessException, IntrospectionException, InvocationTargetException {
        return generateClassesOfObjectTypes(database, packageName, new String[]{"TABLE"});
    }

    /**
     *
     * @param database
     * @param packageName
     * @return
     * @throws SQLException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws IntrospectionException
     * @throws InvocationTargetException
     */
    public static List<DBTableClass> generateClassesOfViews(DBDatabase database, String packageName) throws SQLException, IllegalArgumentException, IllegalAccessException, IntrospectionException, InvocationTargetException {
        return generateClassesOfObjectTypes(database, packageName, new String[]{"VIEW"});
    }

    /**
     *
     * @param database
     * @param packageName
     * @param dbObjectTypes
     * @return
     * @throws SQLException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws IntrospectionException
     * @throws InvocationTargetException
     */
    public static List<DBTableClass> generateClassesOfObjectTypes(DBDatabase database, String packageName, String[] dbObjectTypes) throws SQLException, IllegalArgumentException, IllegalAccessException, IntrospectionException, InvocationTargetException {
        DBTableClassGenerator schema = new DBTableClassGenerator();
        List<DBTableClass> dbTableClasses = new ArrayList<DBTableClass>();
        String lineSeparator = System.getProperty("line.separator");
        String conceptBreak = lineSeparator + lineSeparator;

        Statement dbStatement = database.getDBStatement();
        Connection connection = dbStatement.getConnection();
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet tables = metaData.getTables(null, null, null, dbObjectTypes);

        while (tables.next()) {
            DBTableClass dbTableClass = new DBTableClass();
            StringBuilder javaSource = new StringBuilder();
            if (packageName != null) {
                javaSource.append("package ").append(packageName).append(";");
                javaSource.append(conceptBreak);
            }
            javaSource.append("import nz.co.gregs.dbvolution.*;");
            javaSource.append(lineSeparator);
            javaSource.append("import nz.co.gregs.dbvolution.annotations.*;");
            javaSource.append(lineSeparator);

            String tableName = tables.getString("TABLE_NAME");
            System.err.println(tableName);
            String className = toClassCase(tableName);
            javaSource.append("@DBTableName(\"").append(tableName).append("\") ");
            javaSource.append(lineSeparator);
            javaSource.append("public class ").append(className).append(" extends DBTableRow {");
            javaSource.append(conceptBreak);

            ResultSet columns = metaData.getColumns(null, null, tableName, null);
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String fieldName = toFieldCase(columnName);
                String columnType = getQueryableDatatypeOfSQLType(columns.getInt("DATA_TYPE"));
                javaSource.append("    @DBTableColumn(\"").append(columnName).append("\")");
                javaSource.append(lineSeparator);
                javaSource.append("    public ").append(columnType).append(" ").append(fieldName).append(" = new ").append(columnType).append("();");
                javaSource.append(conceptBreak);
            }
            javaSource.append("}");
            javaSource.append(conceptBreak);
            dbTableClass.className = className;
            dbTableClass.packageName = packageName;
            dbTableClass.javaSource = javaSource.toString();
            dbTableClasses.add(dbTableClass);
        }
        return dbTableClasses;
    }

    /**
     *
     * Returns a string of the appropriate QueryableDatatype for the specified
     * SQLType
     *
     * @param columnType
     * @return
     */
    public static String getQueryableDatatypeOfSQLType(int columnType) {
        String value = "";
        switch (columnType) {
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.BINARY:
            case Types.BOOLEAN:
            case Types.ROWID:
            case Types.SMALLINT:
                value = "DBInteger";
                break;
            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.NUMERIC:
            case Types.REAL:
                value = "DBNumber";
                break;
            case Types.VARCHAR:
            case Types.CHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.CLOB:
            case Types.NCLOB:
            case Types.LONGNVARCHAR:
            case Types.LONGVARCHAR:
                value = "DBString";
                break;
            case Types.DATE:
            case Types.TIME:
                value = "DBDate";
                break;
            case Types.TIMESTAMP:
                value = "DBDate";
                break;
            case Types.VARBINARY:
            case Types.JAVA_OBJECT:
            case Types.LONGVARBINARY:
                value = "DBBlob";
                break;
            default:
                throw new RuntimeException("Unknown Java SQL Type: " + columnType);
        }
        return value;
    }

    /**
     *
     * returns a good guess at the java CLASS version of a DB field name.
     *
     * I.e. changes "_" into an uppercase letter.
     *
     * @param s
     * @return
     */
    public static String toClassCase(String s) {
        String[] parts = s.split("_");
        String camelCaseString = "";
        for (String part : parts) {
            camelCaseString = camelCaseString + toProperCase(part);
        }
        return camelCaseString;
    }

    /**
     *
     * returns a good guess at the java field version of a DB field name.
     *
     * I.e. changes "_" into an uppercase letter.
     *
     * @param s
     * @return
     */
    public static String toFieldCase(String s) {
        String[] parts = s.split("_");
        String camelCaseString = "";
        for (String part : parts) {
            camelCaseString = camelCaseString + toProperCase(part);
        }
        camelCaseString = camelCaseString.substring(0, 1).toLowerCase() + camelCaseString.substring(1);
        return camelCaseString;
    }

    /**
     *
     * Capitalizes the first letter of the string
     *
     * @param s
     * @return
     */
    public static String toProperCase(String s) {
        return s.substring(0, 1).toUpperCase()
                + s.substring(1).toLowerCase();
    }
}