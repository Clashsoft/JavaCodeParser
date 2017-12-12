package dyvilx.tools.compiler.ast.parameter;

import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvilx.tools.asm.AnnotatableVisitor;
import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.asm.TypeReference;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.annotation.ExternalAnnotation;
import dyvilx.tools.compiler.ast.attribute.modifiers.ModifierUtil;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.field.Variable;
import dyvilx.tools.compiler.ast.member.IClassMember;
import dyvilx.tools.compiler.ast.method.ICallableMember;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.raw.InternalType;
import dyvilx.tools.compiler.backend.ClassWriter;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.MethodWriterImpl;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.visitor.AnnotationReader;

public interface IParameter extends IVariable, IClassMember
{
	String DEFAULT_PREFIX_INIT = "init$paramDefault$";
	String DEFAULT_PREFIX      = "$paramDefault$";

	@Override
	Name getName();

	@Override
	void setName(Name name);

	@Override
	String getInternalName();

	Name getLabel();

	void setLabel(Name name);

	String getQualifiedLabel();

	IType getCovariantType();

	@Override
	default IClass getEnclosingClass()
	{
		return null;
	}

	@Override
	default void setEnclosingClass(IClass enclosingClass)
	{
	}

	ICallableMember getMethod();

	void setMethod(ICallableMember method);

	int getIndex();

	void setIndex(int index);

	@Override
	boolean isLocal();

	default void setVarargs()
	{
		this.getAttributes().addFlag(Modifiers.VARARGS);
	}

	@Override
	default void writeInit(MethodWriter writer) throws BytecodeException
	{
		if (!this.isReferenceType())
		{
			return;
		}

		writer.visitVarInsn(this.getType().getLoadOpcode(), this.getLocalIndex());

		Variable.writeRefInit(this, writer, null);
	}

	@Override
	default void writeInit(MethodWriter writer, IValue value) throws BytecodeException
	{
		this.writeInit(writer);
	}

	default void writeParameter(MethodWriter writer)
	{
		final AttributeList annotations = this.getAttributes();
		final IType type = this.getType();
		final long flags = ModifierUtil.getFlags(this);

		final int index = this.getIndex();
		final int localIndex = writer.localCount();

		this.setLocalIndex(localIndex);

		// Add the ACC_VARARGS modifier if necessary
		final int javaModifiers = ModifierUtil.getJavaModifiers(flags) | (this.isVarargs() ? Modifiers.ACC_VARARGS : 0);
		writer.visitParameter(localIndex, this.getQualifiedLabel(), type, javaModifiers);

		// Annotations
		final AnnotatableVisitor visitor = (desc, visible) -> writer.visitParameterAnnotation(index, desc, visible);

		if (annotations != null)
		{
			annotations.write(visitor);
		}

		ModifierUtil.writeModifiers(visitor, flags);

		IType.writeAnnotations(type, writer, TypeReference.newFormalParameterReference(index), "");
	}

	default void writeDefaultValue(ClassWriter writer)
	{
		final IValue value = this.getValue();
		assert value != null;

		final ICallableMember method = this.getMethod();
		final IType type = this.getType();

		final String name;
		final int access;
		if (method == null)
		{
			name = "init$paramDefault$" + this.getInternalName();
			access = Modifiers.STATIC;
		}
		else
		{
			name = method.getInternalName() + "$paramDefault$" + this.getInternalName();
			access = (method.getAttributes().flags() & Modifiers.MEMBER_MODIFIERS) | Modifiers.STATIC;
		}

		final String desc = "()" + this.getDescriptor();
		final String signature = "()" + this.getSignature();
		final MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(access, name, desc, signature, null));

		mw.visitCode();
		value.writeExpression(mw, type);
		mw.visitEnd(type);
	}

	default void writeGetDefaultValue(MethodWriter writer)
	{
		final ICallableMember method = this.getMethod();
		final IClass enclosingClass = this.getEnclosingClass();

		final String name =
			(method == null ? DEFAULT_PREFIX_INIT : method.getInternalName() + DEFAULT_PREFIX) + this.getInternalName();
		final String desc = "()" + this.getDescriptor();
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, enclosingClass.getInternalName(), name, desc,
		                       enclosingClass.isInterface());
	}

	default AnnotationVisitor visitAnnotation(String internalType)
	{
		if (this.skipAnnotation(internalType, null))
		{
			return null;
		}

		IType type = new InternalType(internalType);
		Annotation annotation = new ExternalAnnotation(type);
		return new AnnotationReader(this, annotation);
	}
}
