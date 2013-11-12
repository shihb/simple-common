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

package net.simpleframework.lib.org.mvel2.templates.res;

import static java.lang.String.valueOf;
import net.simpleframework.lib.org.mvel2.MVEL;
import net.simpleframework.lib.org.mvel2.integration.VariableResolverFactory;
import net.simpleframework.lib.org.mvel2.templates.TemplateRuntime;
import net.simpleframework.lib.org.mvel2.templates.util.TemplateOutputStream;

public class ExpressionNode extends Node {
	public ExpressionNode() {
	}

	public ExpressionNode(final int begin, final String name, final char[] template,
			final int start, final int end) {
		this.begin = begin;
		this.name = name;
		this.contents = template;
		this.cStart = start;
		this.cEnd = end - 1;
		this.end = end;
		// this.contents = subset(template, this.cStart = start, (this.end =
		// this.cEnd = end) - start - 1);
	}

	public ExpressionNode(final int begin, final String name, final char[] template,
			final int start, final int end, final Node next) {
		this.name = name;
		this.begin = begin;
		this.contents = template;
		this.cStart = start;
		this.cEnd = end - 1;
		this.end = end;
		// this.contents = subset(template, this.cStart = start, (this.end =
		// this.cEnd = end) - start - 1);
		this.next = next;
	}

	@Override
	public Object eval(final TemplateRuntime runtime, final TemplateOutputStream appender,
			final Object ctx, final VariableResolverFactory factory) {
		appender.append(valueOf(MVEL.eval(contents, cStart, cEnd - cStart, ctx, factory)));
		return next != null ? next.eval(runtime, appender, ctx, factory) : null;
	}

	@Override
	public boolean demarcate(final Node terminatingNode, final char[] template) {
		return false;
	}

	@Override
	public String toString() {
		return "ExpressionNode:" + name + "{"
				+ (contents == null ? "" : new String(contents, cStart, cEnd - cStart)) + "} (start="
				+ begin + ";end=" + end + ")";
	}
}