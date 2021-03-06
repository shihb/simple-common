/**
 * MVEL 2.0
 * Copyright (C) 2007 The Codehaus
 * Mike Brock, Dhanji Prasanna, John Graham, Mark Proctor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.simpleframework.lib.org.mvel2.conversion;

import static java.lang.reflect.Array.get;
import static java.lang.reflect.Array.getLength;
import static java.lang.reflect.Array.newInstance;
import static java.lang.reflect.Array.set;
import static net.simpleframework.lib.org.mvel2.DataConversion.convert;

import java.lang.reflect.Array;
import java.util.Collection;

import net.simpleframework.lib.org.mvel2.ConversionHandler;

public class ArrayHandler implements ConversionHandler {
	private final Class type;

	public ArrayHandler(final Class type) {
		this.type = type;
	}

	@Override
	public Object convertFrom(final Object in) {
		return handleLooseTypeConversion(in.getClass(), in, type);
	}

	@Override
	public boolean canConvertFrom(final Class cls) {
		return cls.isArray() || Collection.class.isAssignableFrom(cls);
	}

	/**
	 * Messy method to handle primitive boxing for conversion. If someone can
	 * re-write this more elegantly, be my guest.
	 * 
	 * @param sourceType
	 * @param input
	 * @param targetType
	 * @return
	 */
	private static Object handleLooseTypeConversion(final Class sourceType, final Object input,
			final Class targetType) {
		final Class targType = targetType.getComponentType();
		if (Collection.class.isAssignableFrom(sourceType)) {
			final Object newArray = newInstance(targType, ((Collection) input).size());

			int i = 0;
			for (final Object o : ((Collection) input)) {
				Array.set(newArray, i++, convert(o, targType));
			}

			return newArray;
		}

		if (!input.getClass().isArray()) {
			// if the input isn't an array converts it in an array with lenght = 1
			// having has its single item the input itself
			final Object target = newInstance(targType, 1);
			set(target, 0, input);
			return target;
		}

		final int len = getLength(input);
		final Object target = newInstance(targType, len);

		for (int i = 0; i < len; i++) {
			set(target, i, convert(get(input, i), targType));
		}

		return target;
	}
}
