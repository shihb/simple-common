/*
 * Copyright 2011 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.simpleframework.lib.net.sf.cglib.proxy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.simpleframework.lib.net.sf.cglib.core.Signature;
import net.simpleframework.lib.org.objectweb.asm.AnnotationVisitor;
import net.simpleframework.lib.org.objectweb.asm.Attribute;
import net.simpleframework.lib.org.objectweb.asm.ClassReader;
import net.simpleframework.lib.org.objectweb.asm.ClassVisitor;
import net.simpleframework.lib.org.objectweb.asm.FieldVisitor;
import net.simpleframework.lib.org.objectweb.asm.Label;
import net.simpleframework.lib.org.objectweb.asm.MethodVisitor;
import net.simpleframework.lib.org.objectweb.asm.Opcodes;

/**
 * Uses bytecode reflection to figure out the targets of all bridge methods that
 * use invokespecial, so that we can later rewrite them to use invokevirtual.
 * 
 * @author sberlin@gmail.com (Sam Berlin)
 */
class BridgeMethodResolver {

	private final Map/* <Class, Set<Signature> */declToBridge;

	public BridgeMethodResolver(final Map declToBridge) {
		this.declToBridge = declToBridge;
	}

	/**
	 * Finds all bridge methods that are being called with invokespecial &
	 * returns them.
	 */
	public Map/* <Signature, Signature> */resolveAll() {
		final Map resolved = new HashMap();
		for (final Iterator entryIter = declToBridge.entrySet().iterator(); entryIter.hasNext();) {
			final Map.Entry entry = (Map.Entry) entryIter.next();
			final Class owner = (Class) entry.getKey();
			final Set bridges = (Set) entry.getValue();
			try {
				new ClassReader(owner.getName()).accept(new BridgedFinder(bridges, resolved),
						ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			} catch (final IOException ignored) {
			}
		}
		return resolved;
	}

	private static class BridgedFinder implements ClassVisitor, MethodVisitor {
		private final Map/* <Signature, Signature> */resolved;
		private final Set/* <Signature> */eligableMethods;

		private Signature currentMethod = null;

		BridgedFinder(final Set eligableMethods, final Map resolved) {
			this.resolved = resolved;
			this.eligableMethods = eligableMethods;
		}

		@Override
		public void visit(final int version, final int access, final String name,
				final String signature, final String superName, final String[] interfaces) {
		}

		@Override
		public MethodVisitor visitMethod(final int access, final String name, final String desc,
				final String signature, final String[] exceptions) {
			final Signature sig = new Signature(name, desc);
			if (eligableMethods.remove(sig)) {
				currentMethod = sig;
				return this;
			} else {
				return null;
			}
		}

		@Override
		public void visitSource(final String source, final String debug) {
		}

		@Override
		public void visitLineNumber(final int line, final Label start) {
		}

		@Override
		public void visitFieldInsn(final int opcode, final String owner, final String name,
				final String desc) {
		}

		@Override
		public void visitEnd() {
		}

		@Override
		public void visitInnerClass(final String name, final String outerName,
				final String innerName, final int access) {
		}

		@Override
		public void visitOuterClass(final String owner, final String name, final String desc) {
		}

		@Override
		public void visitAttribute(final Attribute attr) {
		}

		@Override
		public FieldVisitor visitField(final int access, final String name, final String desc,
				final String signature, final Object value) {
			return null;
		}

		@Override
		public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
			return null;
		}

		@Override
		public AnnotationVisitor visitAnnotationDefault() {
			return null;
		}

		@Override
		public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc,
				final boolean visible) {
			return null;
		}

		@Override
		public void visitCode() {
		}

		@Override
		public void visitFrame(final int type, final int nLocal, final Object[] local,
				final int nStack, final Object[] stack) {
		}

		@Override
		public void visitIincInsn(final int var, final int increment) {
		}

		@Override
		public void visitInsn(final int opcode) {
		}

		@Override
		public void visitIntInsn(final int opcode, final int operand) {
		}

		@Override
		public void visitJumpInsn(final int opcode, final Label label) {
		}

		@Override
		public void visitLabel(final Label label) {
		}

		@Override
		public void visitLdcInsn(final Object cst) {
		}

		@Override
		public void visitLocalVariable(final String name, final String desc, final String signature,
				final Label start, final Label end, final int index) {
		}

		@Override
		public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
		}

		@Override
		public void visitMaxs(final int maxStack, final int maxLocals) {
		}

		@Override
		public void visitMethodInsn(final int opcode, final String owner, final String name,
				final String desc) {
			if (opcode == Opcodes.INVOKESPECIAL && currentMethod != null) {
				final Signature target = new Signature(name, desc);
				// If the target signature is the same as the current,
				// we shouldn't change our bridge becaues invokespecial
				// is the only way to make progress (otherwise we'll
				// get infinite recursion). This would typically
				// only happen when a bridge method is created to widen
				// the visibility of a superclass' method.
				if (!target.equals(currentMethod)) {
					resolved.put(currentMethod, target);
				}
				currentMethod = null;
			}
		}

		@Override
		public void visitMultiANewArrayInsn(final String desc, final int dims) {
		}

		@Override
		public void visitTableSwitchInsn(final int min, final int max, final Label dflt,
				final Label[] labels) {
		}

		@Override
		public void visitTryCatchBlock(final Label start, final Label end, final Label handler,
				final String type) {
		}

		@Override
		public void visitTypeInsn(final int opcode, final String desc) {
		}

		@Override
		public void visitVarInsn(final int opcode, final int var) {
		}
	}

}