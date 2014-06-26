package nz.co.gregs.dbvolution.internal;

import nz.co.gregs.dbvolution.annotations.DBColumn;
import nz.co.gregs.dbvolution.annotations.DBPrimaryKey;
import nz.co.gregs.dbvolution.annotations.DBTableName;
import nz.co.gregs.dbvolution.databases.definitions.DBDatabase;
import nz.co.gregs.dbvolution.datatypes.DBInteger;
import nz.co.gregs.dbvolution.datatypes.QueryableDatatype;

import org.junit.Test;

public class DBRowClassWrapperUsabilityTest {
	private DBDatabase database = new DBDatabase();
	private MyExampleTableClass obj = new MyExampleTableClass();
	private DBRowWrapperFactory factory = new DBRowWrapperFactory();
	
	@Test
	public void easyToGetSpecificPropertyValueOnObjectWhenDoingInline() {
		QueryableDatatype qdt = new DBRowClassWrapper(MyExampleTableClass.class)
			.instanceAdaptorFor(database, obj).getDBPropertyByColumn("column1").getQueryableDatatype();
	}

	@Test
	public void easyToGetSpecificPropertyValueOnObjectWhenDoingVerbosely() {
		DBRowClassWrapper classWrapper = new DBRowClassWrapper(MyExampleTableClass.class);
		DBRowInstanceWrapper objectWrapper = classWrapper.instanceAdaptorFor(database, obj);
		DBProperty property = objectWrapper.getDBPropertyByColumn("column1");
		if (property != null) {
			QueryableDatatype qdt = property.getQueryableDatatype();
			property.setQueryableDatatype(qdt);
		}
	}

	@Test
	public void easyToGetInstanceWrapperGivenObject() {
		DBRowInstanceWrapper objectWrapper = factory.instanceWrapperFor(database, obj);
	}
	
	@Test
	public void easyToIterateOverPropertiesUsingFactory() {
		DBRowInstanceWrapper objectWrapper = factory.instanceWrapperFor(database, obj);
		for (DBProperty property: objectWrapper.getDBProperties()) {
			QueryableDatatype qdt = property.getQueryableDatatype();
		}
	}
	
	@DBTableName("table")
	public static class MyExampleTableClass {
		@DBPrimaryKey
		@DBColumn("column1")
		public DBInteger uid = new DBInteger();
	}

}