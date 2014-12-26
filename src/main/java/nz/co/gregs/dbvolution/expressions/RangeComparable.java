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
package nz.co.gregs.dbvolution.expressions;

/**
 * Indicates that the class can be compared to other instances of this class as if the instances were part of a range.
 * 
 * <p>
 * Methods appropriate to a range include Greater Than, Less Than, and Equals.
 *
 * @author Gregory Graham
 * @param <A>
 */
public interface RangeComparable<A> extends EqualComparable<A> {
	
}
