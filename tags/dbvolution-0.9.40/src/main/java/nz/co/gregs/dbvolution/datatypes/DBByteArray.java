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
package nz.co.gregs.dbvolution.datatypes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import nz.co.gregs.dbvolution.DBDatabase;
import nz.co.gregs.dbvolution.DBRow;
import nz.co.gregs.dbvolution.databases.definitions.DBDefinition;
import nz.co.gregs.dbvolution.exceptions.DBRuntimeException;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * Implements the abstractions necessary to handle arbitrary byte streams and
 * files stored in the database.
 *
 * <p>
 * Use DBByteArray for files, streams, and exceptionally long text. Store Java
 * instances/objects as {@link DBJavaObject} for greater convenience.
 *
 * <p>
 * Generally DBBoolean is declared inside your DBRow sub-class as:
 * {@code @DBColumn public DBByteArray myBoolColumn = new DBByteArray();}
 *
 * <p>
 * DBByteArray is the standard type of {@link DBLargeObject BLOB columns}.
 *
 * @author Gregory Graham
 */
public class DBByteArray extends DBLargeObject {

	private static final long serialVersionUID = 1;
	InputStream byteStream = null;

	/**
	 * The Default constructor for a DBByteArray.
	 *
	 */
	public DBByteArray() {
		super();
	}

	/**
	 *
	 * @return the standard SQL datatype that corresponds to this QDT as a
	 * String
	 */
	@Override
	public String getSQLDatatype() {
		return "BLOB";
	}

	/**
	 * Sets the value of this DBByteArray to the byte array supplied.
	 *
	 * @param byteArray
	 */
	public void setValue(byte[] byteArray) {
		super.setLiteralValue(byteArray);
		byteStream = new BufferedInputStream(new ByteArrayInputStream(byteArray));
	}

	/**
	 * Sets the value of this DBByteArray to the InputStream supplied.
	 *
	 * <p>
	 * The input stream will not be read until the containing DBRow is
	 * saved/inserted.
	 *
	 * @param inputViaStream
	 */
	public void setValue(InputStream inputViaStream) {
		super.setLiteralValue(inputViaStream);
		byteStream = new BufferedInputStream(inputViaStream);
	}

	/**
	 * Sets the value of this DBByteArray to the file supplied.
	 *
	 * <p>
	 * Unlike {@link #setValue(java.io.InputStream) setting an InputStream}, the
	 * file is read immediately and stored internally. If you would prefer to
	 * delay the reading of the file, wrap the file in a
	 * {@link FileInputStream}.
	 *
	 * @param fileToRead
	 * @throws IOException
	 */
	public void setValue(File fileToRead) throws IOException {
		setValue(setFromFileSystem(fileToRead));
	}

	/**
	 * Set the value of the DBByteArray to the String provided.
	 *
	 * @param string
	 */
	public void setValue(String string) {
		setValue(string.getBytes());
	}

	@Override
	void setValue(Object newLiteralValue) {
		if (newLiteralValue instanceof byte[]) {
			setValue((byte[]) newLiteralValue);
		} else if (newLiteralValue instanceof DBByteArray) {
			final DBByteArray valBytes = (DBByteArray) newLiteralValue;
			setValue(valBytes.getValue());
		} else if (newLiteralValue instanceof String) {
			final String valBytes = (String) newLiteralValue;
			setValue(valBytes.getBytes());
		} else {
			throw new ClassCastException(this.getClass().getSimpleName() + ".setValue() Called With A Non-Byte[]: Use only byte arrays with this class");
		}
	}

//	@Override
//	public void setFromResultSet(DBDatabase database, ResultSet resultSet, String fullColumnName) throws SQLException {
//		blankQuery();
//		DBDefinition defn = database.getDefinition();
//		if (resultSet == null || fullColumnName == null) {
//			this.setToNull();
//		} else {
//			if (defn.prefersLargeObjectsReadAsBase64CharacterStream()) {
//				try {
//					setFromCharacterReader(resultSet, fullColumnName);
//				} catch (IOException ex) {
//					throw new DBRuntimeException("Unable To Set Value: " + ex.getMessage(), ex);
//				}
//			} else if (defn.prefersLargeObjectsReadAsBytes()) {
//				setFromGetBytes(resultSet, fullColumnName);
//			} else if (defn.prefersLargeObjectsReadAsCLOB()) {
//				setFromCLOB(resultSet, fullColumnName);
//			} else {
//				setFromBinaryStream(resultSet, fullColumnName);
//			}
//		}
//
//		setUnchanged();
//
//		setDefined(true);
//	}

	private byte[] getFromBinaryStream(ResultSet resultSet, String fullColumnName) throws SQLException {
		byte[] bytes = new byte[]{};
		InputStream inputStream;
		inputStream = resultSet.getBinaryStream(fullColumnName);
		if (resultSet.wasNull()) {
			inputStream = null;
		}
		if (inputStream == null) {
			this.setToNull();
		} else {
			InputStream input = new BufferedInputStream(inputStream);
			List<byte[]> byteArrays = new ArrayList<byte[]>();

			int totalBytesRead = 0;
			try {
				byte[] resultSetBytes;
				resultSetBytes = new byte[100000];
				int bytesRead = input.read(resultSetBytes);
				while (bytesRead > 0) {
					totalBytesRead += bytesRead;
					byteArrays.add(resultSetBytes);
					resultSetBytes = new byte[100000];
					bytesRead = input.read(resultSetBytes);
				}
			} catch (IOException ex) {
				Logger.getLogger(DBByteArray.class.getName()).log(Level.SEVERE, null, ex);
			}
			bytes = new byte[totalBytesRead];
			int bytesAdded = 0;
			for (byte[] someBytes : byteArrays) {
				System.arraycopy(someBytes, 0, bytes, bytesAdded, Math.min(someBytes.length, bytes.length - bytesAdded));
				bytesAdded += someBytes.length;
			}
//			this.setValue(bytes);
		}
		return bytes;
	}

	private byte[] getFromGetBytes(ResultSet resultSet, String fullColumnName) throws SQLException {
		byte[] bytes = resultSet.getBytes(fullColumnName);
//		this.setValue(bytes);
		return bytes;
	}

	private byte[] getFromCharacterReader(ResultSet resultSet, String fullColumnName) throws SQLException, IOException {
		byte[] decodeBuffer = new byte[]{};
		Reader inputReader = null;
		try {
			inputReader = resultSet.getCharacterStream(fullColumnName);
		} catch (NullPointerException nullEx) {
			;// NullPointerException is thrown by a SQLite-JDBC bug sometimes.
		}
		if (inputReader != null) {
			if (resultSet.wasNull()) {
				this.setToNull();
			} else {
				BufferedReader input = new BufferedReader(inputReader);
				List<byte[]> byteArrays = new ArrayList<byte[]>();

				int totalBytesRead = 0;
				try {
					char[] resultSetBytes;
					resultSetBytes = new char[100000];
					int bytesRead = input.read(resultSetBytes);
					while (bytesRead > 0) {
						totalBytesRead += bytesRead;
						byteArrays.add(String.valueOf(resultSetBytes).getBytes());
						resultSetBytes = new char[100000];
						bytesRead = input.read(resultSetBytes);
					}
				} catch (IOException ex) {
					Logger.getLogger(DBByteArray.class.getName()).log(Level.SEVERE, null, ex);
				}
				byte[] bytes = new byte[totalBytesRead];
				int bytesAdded = 0;
				for (byte[] someBytes : byteArrays) {
					System.arraycopy(someBytes, 0, bytes, bytesAdded, Math.min(someBytes.length, bytes.length - bytesAdded));
					bytesAdded += someBytes.length;
				}
				decodeBuffer = Base64.decodeBase64(bytes);
//				this.setValue(decodeBuffer);
			}
		}
		return decodeBuffer;
	}

	private byte[] getFromCLOB(ResultSet resultSet, String fullColumnName) throws SQLException {
		byte[] bytes = new byte[]{};
		Clob clob = resultSet.getClob(fullColumnName);
		if (resultSet.wasNull() || clob == null) {
			this.setToNull();
		} else {
			BufferedReader input = new BufferedReader(clob.getCharacterStream());
			List<byte[]> byteArrays = new ArrayList<byte[]>();

			int totalBytesRead = 0;
			try {
				char[] resultSetBytes;
				resultSetBytes = new char[100000];
				int bytesRead = input.read(resultSetBytes);
				while (bytesRead > 0) {
					totalBytesRead += bytesRead;
					byteArrays.add(String.valueOf(resultSetBytes).getBytes());
					resultSetBytes = new char[100000];
					bytesRead = input.read(resultSetBytes);
				}
			} catch (IOException ex) {
				Logger.getLogger(DBByteArray.class.getName()).log(Level.SEVERE, null, ex);
			}
			bytes = new byte[totalBytesRead];
			int bytesAdded = 0;
			for (byte[] someBytes : byteArrays) {
				System.arraycopy(someBytes, 0, bytes, bytesAdded, Math.min(someBytes.length, bytes.length - bytesAdded));
				bytesAdded += someBytes.length;
			}
//			this.setValue(bytes);
		}
		return bytes;
	}

	@Override
	public String formatValueForSQLStatement(DBDatabase db) {
		throw new UnsupportedOperationException("Binary datatypes like " + this.getClass().getSimpleName() + " do not have a simple SQL representation. Do not call getSQLValue(), use the getInputStream() method instead.");
	}

	/**
	 * Tries to set the DBDyteArray to the contents of the supplied file.
	 *
	 * <p>
	 * Convenience method for {@link #setFromFileSystem(java.io.File) }.
	 *
	 * @param originalFile
	 * @return the byte[] of the contents of the file.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public byte[] setFromFileSystem(String originalFile) throws FileNotFoundException, IOException {
		File file = new File(originalFile);
		return setFromFileSystem(file);
	}

	/**
	 * Tries to set the DBDyteArray to the contents of the supplied file.
	 *
	 * <p>
	 * Convenience method for {@link #setFromFileSystem(java.io.File) }.
	 *
	 * @param originalFile
	 * @return the byte[] of the contents of the file.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public byte[] setFromFileSystem(DBString originalFile) throws FileNotFoundException, IOException {
		File file = new File(originalFile.stringValue());
		return setFromFileSystem(file);
	}

	/**
	 * Tries to set the DBDyteArray to the contents of the supplied file.
	 *
	 * @param originalFile
	 * @return the byte[] of the contents of the file.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public byte[] setFromFileSystem(File originalFile) throws FileNotFoundException, IOException {
//		System.out.println("FILE: " + originalFile.getAbsolutePath());
		byte[] bytes = new byte[(int) originalFile.length()];
		InputStream input = null;
		try {
			int totalBytesRead = 0;
			input = new BufferedInputStream(new FileInputStream(originalFile));
			while (totalBytesRead < bytes.length) {
				int bytesRemaining = bytes.length - totalBytesRead;
				//input.read() returns -1, 0, or more :
				int bytesRead = input.read(bytes, totalBytesRead, bytesRemaining);
				if (bytesRead > 0) {
					totalBytesRead += bytesRead;
				}
			}
			/*
			 the above style is a bit tricky: it places bytes into the 'result' array;
			 'result' is an output parameter;
			 the while loop usually has a single iteration only.
			 */
		} finally {
			if (input != null) {
				input.close();
			}
		}
		setValue(bytes);
		return bytes;
	}

	/**
	 * Tries to write the contents of this DBByteArray to the file supplied.
	 *
	 * <p>
	 * Convenience method for {@link #writeToFileSystem(java.io.File) }.
	 *
	 * @param originalFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void writeToFileSystem(String originalFile) throws FileNotFoundException, IOException {
		File file = new File(originalFile);
		writeToFileSystem(file);
	}

	/**
	 * Tries to write the contents of this DBByteArray to the file supplied.
	 *
	 * <p>
	 * Convenience method for {@link #writeToFileSystem(java.io.File) }.
	 *
	 * @param originalFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void writeToFileSystem(DBString originalFile) throws FileNotFoundException, IOException {
		writeToFileSystem(originalFile.toString());
	}

	/**
	 * Tries to write the contents of this DBByteArray to the file supplied.
	 *
	 * <p>
	 * Convenience method for {@link #writeToFileSystem(java.io.File) }.
	 *
	 * @param originalFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void writeToFileSystem(File originalFile) throws FileNotFoundException, IOException {
		if (getLiteralValue() != null && originalFile != null) {
//			System.out.println("FILE: " + originalFile.getAbsolutePath());
			if (!originalFile.exists()) {
				originalFile.createNewFile();
			}
			OutputStream output = null;
			try {
				output = new BufferedOutputStream(new FileOutputStream(originalFile));
				output.write(getBytes());
				output.flush();
				output.close();
				output = null;
			} finally {
				if (output != null) {
					output.close();
				}
			}
		}
	}

	/**
	 * Returns the internal InputStream.
	 *
	 * @return an InputStream to read the bytes.
	 */
	@Override
	public InputStream getInputStream() {
		if (byteStream == null) {
			this.setValue(getBytes());
		}
		return byteStream;
	}

	/**
	 * Returns the byte[] used internally to store the value of this
	 * DBByteArray.
	 *
	 * @return the byte[] value of this DBByteArray.
	 */
	public byte[] getBytes() {
		return (byte[]) this.getLiteralValue();
	}

	@Override
	public String stringValue() {
		byte[] value = this.getValue();
		if (this.isNull()) {
			return super.stringValue();
		} else {
			return new String(value);
		}
	}

	@Override
	public int getSize() {
		return getBytes().length;
	}

	@Override
	public byte[] getValue() {
		return getBytes();
	}

	@Override
	public DBByteArray getQueryableDatatypeForExpressionValue() {
		return new DBByteArray();
	}

	@Override
	public boolean isAggregator() {
		return false;
	}

	@Override
	public Set<DBRow> getTablesInvolved() {
		return new HashSet<DBRow>();
	}

	@Override
	protected Object getFromResultSet(DBDatabase database, ResultSet resultSet, String fullColumnName) throws SQLException {
		byte[] bytes = new byte[]{};
		DBDefinition defn = database.getDefinition();
		if (defn.prefersLargeObjectsReadAsBase64CharacterStream()) {
			try {
				bytes = getFromCharacterReader(resultSet, fullColumnName);
			} catch (IOException ex) {
				throw new DBRuntimeException("Unable To Set Value: " + ex.getMessage(), ex);
			}
		} else if (defn.prefersLargeObjectsReadAsBytes()) {
			bytes = getFromGetBytes(resultSet, fullColumnName);
		} else if (defn.prefersLargeObjectsReadAsCLOB()) {
			bytes = getFromCLOB(resultSet, fullColumnName);
		} else {
			bytes = getFromBinaryStream(resultSet, fullColumnName);
		}
		return bytes;
	}

}