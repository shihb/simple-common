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

package net.simpleframework.lib.org.mvel2.integration.impl;

import net.simpleframework.lib.org.mvel2.integration.VariableResolver;
import net.simpleframework.lib.org.mvel2.util.MethodStub;

/**
 * @author Christopher Brock
 */
public class StaticMethodImportResolver implements VariableResolver {
	private final String name;
	private MethodStub method;

	public StaticMethodImportResolver(final String name, final MethodStub method) {
		this.name = name;
		this.method = method;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class getType() {
		return null;
	}

	@Override
	public void setStaticType(final Class type) {
	}

	@Override
	public int getFlags() {
		return 0;
	}

	@Override
	public MethodStub getValue() {
		return method;
	}

	@Override
	public void setValue(final Object value) {
		this.method = (MethodStub) value;
	}
}
