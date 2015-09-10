package doser.lucene.analysis;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.AttributeFactory;

public final class DoserStandardTokenizer extends CharTokenizer {

	/**
	 * Construct a new WhitespaceTokenizer using a given
	 * {@link org.apache.lucene.util.AttributeSource.AttributeFactory}.
	 * 
	 * @param factory
	 *            the attribute factory to use for this {@link Tokenizer}
	 * @param in
	 *            the input to split up into tokens
	 */
	public DoserStandardTokenizer(AttributeFactory factory, Reader in) {
		super(factory, in);
	}

	/**
	 * Construct a new WhitespaceTokenizer. * @param matchVersion Lucene version
	 * to match See {@link <a href="#version">above</a>}
	 * 
	 * @param in
	 *            the input to split up into tokens
	 */
	public DoserStandardTokenizer(Reader in) {
		super(in);
	}

	/**
	 * Collects only characters which do not satisfy
	 * {@link Character#isWhitespace(int)}.
	 */
	@Override
	protected boolean isTokenChar(int c) {
		boolean check = true;
		if (Character.isWhitespace(c) || c == 46) {
			check = false;
		}
		return check;
	}
}