package nz.co.gregs.dbvolution.generation.ast;

import java.io.File;

import nz.co.gregs.dbvolution.datatypes.DBInteger;
import nz.co.gregs.dbvolution.generation.CodeGenerationConfiguration;
import nz.co.gregs.dbvolution.generation.ColumnNameResolver;
import nz.co.gregs.dbvolution.generation.ast.ParsedClass;
import nz.co.gregs.dbvolution.generation.ast.ParsedField;
import nz.co.gregs.dbvolution.generation.ast.ParsedMethod;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;
import org.junit.Test;

/**
 * @see http://help.eclipse.org/helios/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2FASTParser.html
 * @author Malcolm Lett
 */
public class LowLevelGenerationTests extends AbstractASTTest {
	@Test
	public void generatesWhenUsingLowLevelMethods() throws MalformedTreeException, BadLocationException {
		CodeGenerationConfiguration config = new CodeGenerationConfiguration();
		
		ParsedClass javatype = ParsedClass.newDBTableInstance(config,
				LowLevelGenerationTests.class.getName()+"_Marque",
				"t_3");
		System.out.println(javatype.toString());
		
		ColumnNameResolver columnNameResolver = new ColumnNameResolver();
		ParsedField newField = ParsedField.newDBColumnInstance(javatype.getTypeContext(),
				columnNameResolver.getPropertyNameFor("c_2"), DBInteger.class, false, "c_2");
		System.out.println(newField);
		javatype.addFieldAfter(null, newField);
		
		ParsedMethod newGetterMethod = ParsedMethod.newGetterInstance(javatype.getTypeContext(), newField);
		System.out.println(newGetterMethod);
		javatype.addMethodAfter(null, newGetterMethod);

		ParsedMethod newSetterMethod = ParsedMethod.newSetterInstance(javatype.getTypeContext(), newField);
		System.out.println(newSetterMethod);
		javatype.addMethodAfter(null, newSetterMethod);
		
		File srcFolder = new File("target/test-output");
		srcFolder.mkdirs();
		javatype.writeToSourceFolder(srcFolder);
	}

	@Test
	public void updatesWhenUsingLowLevelMethods() throws MalformedTreeException, BadLocationException {
		ParsedClass javatype = ParsedClass.of(getMarqueSource());
		System.out.println(javatype.toString());
		
		ColumnNameResolver columnNameResolver = new ColumnNameResolver();
		ParsedField newField = ParsedField.newDBColumnInstance(javatype.getTypeContext(),
				columnNameResolver.getPropertyNameFor("c_2"), DBInteger.class, false, "c_2");
		System.out.println(newField);
		javatype.addFieldAfter(null, newField);
		
		ParsedMethod newMethod = ParsedMethod.newGetterInstance(javatype.getTypeContext(), newField);
		System.out.println(newMethod);
		javatype.addMethodAfter(null, newMethod);
		
		File srcFolder = new File("target/test-output");
		srcFolder.mkdirs();
		javatype.writeToSourceFolder(srcFolder);
	}
	
}
