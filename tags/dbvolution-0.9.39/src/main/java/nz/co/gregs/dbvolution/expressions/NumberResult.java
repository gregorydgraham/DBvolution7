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
package nz.co.gregs.dbvolution.expressions;

/**
 * Interface required to be implemented by all DBExpressions that produce
 * Number results
 *
 * <p>
 * DBvolution attempts to maintain type safety using the *Result interfaces.
 * Most operations requiring a number will not accept anything other than an
 * actual number or a NumberResult.
 *
 * <p>
 * Add {@code implements NumberResult} to your class and override the copy
 * method so that it returns your class type.
 *
 * @author greg
 * @see DBExpression
 */
public interface NumberResult extends DBExpression{
    
    @Override
    public NumberResult copy();

	/**
	 * Indicates whether the current search criteria requires special coverage for NULL values.
	 *
	 * @return TRUE if the search criteria requires NULLs to be handled, otherwise FALSE.
	 */
	public boolean getIncludesNull();
}