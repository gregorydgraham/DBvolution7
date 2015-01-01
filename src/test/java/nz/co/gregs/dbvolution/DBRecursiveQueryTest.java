/*
 * Copyright 2014 Gregory Graham.
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
package nz.co.gregs.dbvolution;

import nz.co.gregs.dbvolution.exceptions.ColumnProvidedMustBeAForeignKey;
import nz.co.gregs.dbvolution.exceptions.ForeignKeyDoesNotReferenceATableInTheQuery;
import nz.co.gregs.dbvolution.exceptions.ForeignKeyIsNotRecursiveException;
import java.sql.SQLException;
import java.util.List;
import nz.co.gregs.dbvolution.annotations.*;
import nz.co.gregs.dbvolution.datatypes.*;
import nz.co.gregs.dbvolution.generic.AbstractTest;
import static org.hamcrest.Matchers.*;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Gregory Graham
 */
//@Ignore
public class DBRecursiveQueryTest extends AbstractTest {

	final Parts wing = new Parts(null, "wing");
	Parts aileron;
	Parts lever;
	Parts screw;

	final PartsStringKey wingString = new PartsStringKey("wing", null, "wing");
	PartsStringKey aileronString;
	PartsStringKey leverString;
	PartsStringKey screwString;

	final PartsWithoutTableName wingWithout = new PartsWithoutTableName(null, "wing");
	PartsWithoutTableName aileronWithout;

	public DBRecursiveQueryTest(Object testIterationName, Object db) {
		super(testIterationName, db);
	}

	@Override
	public void setup(DBDatabase database) throws Exception {
		super.setup(database);
		database.preventDroppingOfTables(false);
		database.dropTableNoExceptions(new Parts());
		database.createTable(new Parts());
		database.insert(wing);
		aileron = new Parts(wing.partID.intValue(), "aileron");
		database.insert(aileron);
		lever = new Parts(aileron.partID.intValue(), "lever");
		screw = new Parts(aileron.partID.intValue(), "screw");
		database.insert(lever);
		database.insert(screw);

		database.preventDroppingOfTables(false);
		database.dropTableNoExceptions(new PartsWithoutTableName());
		database.createTable(new PartsWithoutTableName());
		database.insert(wingWithout);
		aileronWithout = new PartsWithoutTableName(wing.partID.intValue(), "aileron");
		database.insert(aileronWithout);
		database.insert(new PartsWithoutTableName(aileron.partID.intValue(), "lever"));
		database.insert(new PartsWithoutTableName(aileron.partID.intValue(), "screw"));

		database.preventDroppingOfTables(false);
		database.dropTableNoExceptions(new CompletePart());
		database.createTable(new CompletePart());
		database.insert(new CompletePart(aileron.partID.intValue(), "Aileron"));

		database.preventDroppingOfTables(false);
		database.dropTableNoExceptions(new PartsStringKey());
		database.createTable(new PartsStringKey());
		database.insert(wingString);
		aileronString = new PartsStringKey("aileronid", wingString.partID.stringValue(), "aileron");
		database.insert(aileronString);
		leverString = new PartsStringKey("leverid", aileronString.partID.stringValue(), "lever");
		screwString = new PartsStringKey("screwid", aileronString.partID.stringValue(), "screw");
		database.insert(leverString);
		database.insert(screwString);
	}

//	@Ignore
	@Test
	public void descendSimpleTree() throws SQLException {
		Parts aileronID = new Parts();
		aileronID.partID.permittedValues(aileron.partID.intValue());
		final DBQuery findTheAileronQuery = database.getDBQuery(aileronID);

		DBRecursiveQuery recursive = new DBRecursiveQuery(findTheAileronQuery, aileronID.column(aileronID.subPartOf));
		@SuppressWarnings("unchecked")
		List<Parts> componentsOfTheAileron = recursive.getDescendants();

		database.print(componentsOfTheAileron);
		Assert.assertThat(componentsOfTheAileron.size(), is(3));
		final Parts firstPart = componentsOfTheAileron.get(0);
		final DBString firstName = firstPart.name;
		Assert.assertThat(firstName.stringValue(),
				anyOf(is("aileron"), is("lever"), is("screw"))
		);
	}

//	@Ignore
	@Test
	public void ascendSimpleTree() throws SQLException {
		Parts aileronID = new Parts();
		aileronID.partID.permittedValues(aileron.partID.intValue());
		final DBQuery findTheAileronQuery = database.getDBQuery(aileronID);

		DBRecursiveQuery recursive = new DBRecursiveQuery(findTheAileronQuery, aileronID.column(aileronID.subPartOf));
		@SuppressWarnings("unchecked")
		List<Parts> componentsOfTheAileron = recursive.getAncestors();

		database.print(componentsOfTheAileron);
		Assert.assertThat(componentsOfTheAileron.size(), is(2));
	}

//	@Ignore
	@Test
	public void descendSimpleTreeWithoutTableName() throws SQLException {
		PartsWithoutTableName aileronID = new PartsWithoutTableName();
		aileronID.partID.permittedValues(aileronWithout.partID.intValue());
		final DBQuery findTheAileronQuery = database.getDBQuery(aileronID);

		DBRecursiveQuery recursive = new DBRecursiveQuery(findTheAileronQuery, aileronID.column(aileronID.subPartOf));
		@SuppressWarnings("unchecked")
		List<PartsWithoutTableName> componentsOfTheAileron
				= recursive.getDescendants();

		database.print(componentsOfTheAileron);
		Assert.assertThat(componentsOfTheAileron.size(), is(3));
	}

//	@Ignore
	@Test
	public void ascendSimpleTreeWithoutTableName() throws SQLException {
		PartsWithoutTableName aileronID = new PartsWithoutTableName();
		aileronID.partID.permittedValues(aileronWithout.partID.intValue());
		final DBQuery findTheAileronQuery = database.getDBQuery(aileronID);

		DBRecursiveQuery recursive = new DBRecursiveQuery(findTheAileronQuery, aileronID.column(aileronID.subPartOf));
		@SuppressWarnings("unchecked")
		List<PartsWithoutTableName> componentsOfTheAileron
				= recursive.getAncestors();

		database.print(componentsOfTheAileron);
		Assert.assertThat(componentsOfTheAileron.size(), is(2));
	}

//	@Ignore
	@Test
	public void descendTreeFrom2TableQuery() throws SQLException {
		Parts part = new Parts();
		CompletePart aileronID = new CompletePart();
		aileronID.name.permittedValues("Aileron");
		final DBQuery findTheAileronQuery = database.getDBQuery(new Parts(), aileronID);

		DBRecursiveQuery<Parts> recursive = new DBRecursiveQuery<Parts>(findTheAileronQuery, part.column(part.subPartOf));
		List<Parts> componentsOfTheAileron
				= recursive.getDescendants();

		database.print(componentsOfTheAileron);
		Assert.assertThat(componentsOfTheAileron.size(), is(3));
	}

//	@Ignore
	@Test
	public void ascendTreeFrom2TableQuery() throws SQLException {
		Parts part = new Parts();
		CompletePart aileronID = new CompletePart();
		aileronID.name.permittedValues("Aileron");
		final DBQuery findTheAileronQuery = database.getDBQuery(new Parts(), aileronID);

		DBRecursiveQuery<Parts> recursive = new DBRecursiveQuery<Parts>(findTheAileronQuery, part.column(part.subPartOf));
		List<Parts> componentsOfTheAileron
				= recursive.getAncestors();

		database.print(componentsOfTheAileron);
		Assert.assertThat(componentsOfTheAileron.size(), is(2));
	}

//	@Ignore
	@Test
	public void descendTreeFrom2TableQueryToPathsList() throws SQLException {
		Parts part = new Parts();
		CompletePart aileronID = new CompletePart();
		aileronID.name.permittedValues("Aileron");
		final DBQuery findTheAileronQuery = database.getDBQuery(new Parts(), aileronID);

		DBRecursiveQuery<Parts> recursive = new DBRecursiveQuery<Parts>(findTheAileronQuery, part.column(part.subPartOf));
		List<Parts> componentsOfTheAileron = recursive.getDescendants();
		database.print(componentsOfTheAileron);
		Assert.assertThat(componentsOfTheAileron.size(), is(3));
	}

//	@Ignore
	@Test
	public void ascendTreeFrom2TableQueryToPathsList() throws SQLException {
		Parts part = new Parts();
		CompletePart aileronID = new CompletePart();
		aileronID.name.permittedValues("Aileron");
		final DBQuery findTheAileronQuery = database.getDBQuery(new Parts(), aileronID);

		DBRecursiveQuery<Parts> recursive = new DBRecursiveQuery<Parts>(findTheAileronQuery, part.column(part.subPartOf));
		List<Parts> componentsOfTheAileron
				= recursive.getAncestors();

		database.print(componentsOfTheAileron);
		Assert.assertThat(componentsOfTheAileron.size(), is(2));
	}

//	@Ignore
	@Test
	public void getPathToRootAsList() throws SQLException {
		Parts part = new Parts();
		CompletePart aileronID = new CompletePart();
		aileronID.name.permittedValues("Aileron");
		final DBQuery findTheAileronQuery = database.getDBQuery(new Parts(), aileronID);

		DBRecursiveQuery<Parts> recursive = new DBRecursiveQuery<Parts>(findTheAileronQuery, part.column(part.subPartOf));
		List<Parts> pathToTheWing
				= recursive.getPathToRoot();

		database.print(pathToTheWing);
		Assert.assertThat(pathToTheWing.size(), is(2));
		Assert.assertThat(pathToTheWing.get(0).name.stringValue(), is("aileron"));
		Assert.assertThat(pathToTheWing.get(1).name.stringValue(), is("wing"));
	}

//	@Ignore
	@Test
	public void checkEverythingWorksForStringIDs() throws SQLException {
		PartsStringKey aileronID = new PartsStringKey();
		aileronID.name.permittedValues("aileron");
		final DBQuery findTheAileronQuery = database.getDBQuery(aileronID);

		DBRecursiveQuery<PartsStringKey> recursive = new DBRecursiveQuery<PartsStringKey>(findTheAileronQuery, aileronID.column(aileronID.subPartOf));
		List<PartsStringKey> pathToTheWing
				= recursive.getPathToRoot();

		database.print(pathToTheWing);
		Assert.assertThat(pathToTheWing.size(), is(2));
		Assert.assertThat(pathToTheWing.get(0).name.stringValue(), is("aileron"));
		Assert.assertThat(pathToTheWing.get(1).name.stringValue(), is("wing"));
	}

	@Test(expected = ForeignKeyDoesNotReferenceATableInTheQuery.class)
	public void checkForeignKeyInvolvesQueryTablesException() throws SQLException {
		Parts part  = new Parts();
		PartsStringKey aileronID = new PartsStringKey();
		aileronID.name.permittedValues("aileron");
		final DBQuery findTheAileronQuery = database.getDBQuery(aileronID);

		DBRecursiveQuery<PartsStringKey> recursive = new DBRecursiveQuery<PartsStringKey>(
				findTheAileronQuery, part.column(part.subPartOf));
	}

	@Test(expected = ColumnProvidedMustBeAForeignKey.class)
	public void checkColumnIsNotAForeignKeyException() throws SQLException {
		PartsStringKey aileronID = new PartsStringKey();
		aileronID.name.permittedValues("aileron");
		final DBQuery findTheAileronQuery = database.getDBQuery(aileronID);

		DBRecursiveQuery<PartsStringKey> recursive = new DBRecursiveQuery<PartsStringKey>(
				findTheAileronQuery, aileronID.column(aileronID.name));
	}

	@Test(expected = ForeignKeyIsNotRecursiveException.class)
	public void checkForeignKeyIsRecursiveException() throws SQLException {
		PartsStringKey aileronID = new PartsStringKey();
		aileronID.name.permittedValues("aileron");
		final DBQuery findTheAileronQuery = database.getDBQuery(aileronID, new Parts());

		DBRecursiveQuery<PartsStringKey> recursive = new DBRecursiveQuery<PartsStringKey>(
				findTheAileronQuery, aileronID.column(aileronID.fkToParts));
	}

	//TODO Generate a Tree from the list of descendants.
	// TODO Investigate need for "part_id"
	@DBTableName("parts")
	public static class Parts extends DBRow {

		@DBColumn("part_id")
		@DBPrimaryKey
		@DBAutoIncrement
		public DBInteger partID = new DBInteger();

		@DBColumn
		@DBForeignKey(Parts.ParentPart.class)
		public DBInteger subPartOf = new DBInteger();

		@DBColumn
		public DBString name = new DBString();

		public Parts() {
			super();
		}

		public Parts(Integer parentPartID, String name) {
			super();
			this.subPartOf.setValue(parentPartID);
			this.name.setValue(name);
		}

		public static class ParentPart extends Parts {

			public ParentPart() {
				super();
			}
		}
	}

	@DBTableName("parts_string_key")
	public static class PartsStringKey extends DBRow {

		@DBColumn("part_id")
		@DBPrimaryKey
//		@DBAutoIncrement
		public DBString partID = new DBString();

		@DBColumn
		@DBForeignKey(PartsStringKey.ParentPart.class)
		public DBString subPartOf = new DBString();

		@DBColumn
		public DBString name = new DBString();
		
		@DBColumn
		@DBForeignKey(Parts.class)
		public DBInteger fkToParts = new DBInteger();


		public PartsStringKey() {
			super();
		}

		public PartsStringKey(String id, String parentPartID, String name) {
			super();
			this.partID.setValue(id);
			this.subPartOf.setValue(parentPartID);
			this.name.setValue(name);
		}

		public static class ParentPart extends PartsStringKey {

			public ParentPart() {
				super();
			}
		}
	}

	@DBTableName("complete_parts")
	public static class CompletePart extends DBRow {

		@DBColumn//("complete_part_id")
		@DBPrimaryKey
		@DBAutoIncrement
		public DBInteger completePartID = new DBInteger();

		@DBColumn
		@DBForeignKey(Parts.class)
		public DBInteger partID = new DBInteger();

		@DBColumn
		public DBString name = new DBString();

		public CompletePart() {
			super();
		}

		public CompletePart(Integer parentPartID, String name) {
			super();
			this.partID.setValue(parentPartID);
			this.name.setValue(name);
		}

	}

	// TODO Investigate need for "part_id"
	public static class PartsWithoutTableName extends DBRow {

		@DBColumn("part_id")
		@DBPrimaryKey
		@DBAutoIncrement
		public DBInteger partID = new DBInteger();

		@DBColumn
		@DBForeignKey(ParentPart.class)
		public DBInteger subPartOf = new DBInteger();

		@DBColumn
		public DBString name = new DBString();

		public PartsWithoutTableName() {
			super();
		}

		public PartsWithoutTableName(Integer parentPartID, String name) {
			super();
			this.subPartOf.setValue(parentPartID);
			this.name.setValue(name);
		}

		public static class ParentPart extends PartsWithoutTableName {

			public ParentPart() {
				super();
			}
		}
	}
}