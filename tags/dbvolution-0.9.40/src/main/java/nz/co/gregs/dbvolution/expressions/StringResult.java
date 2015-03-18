/*
 * Copyright 2013 greg.
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
package nz.co.gregs.dbvolution.expressions;

/**
 * Interface required to be implemented by all DBExpressions that produce
 * String results
 *
 * <p>
 * DBvolution attempts to maintain type safety using the *Result interfaces.
 * Most operations requiring a String will not accept anything other than an
 * actual String or a StringResult.
 *
 * <p>
 * Add {@code implements StringResult} to your class and override the copy
 * method so that it returns your class type.
 *
 * @author Gregory Graham
 * @see DBExpression
 */
public interface StringResult extends DBExpression, RangeComparable{
    
    @Override
    public StringResult copy();

	/**
	 * Returns TRUE if this expression requires support for possible NULL database values.
	 *
	 * @return TRUE if the expression should check for NULLs, FALSE otherwise. 
	 */
	public boolean getIncludesNull();
    
}