package dyvil.tools.compiler.ast.parameter;

import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.method.ICallableMember;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.raw.InternalType;
import dyvil.tools.compiler.backend.visitor.AnnotationReader;

public interface IParameter extends IVariable, IClassMember
{
	@Override
	IType getInternalType();

	@Override
	default IClass getEnclosingClass()
	{
		return null;
	}

	@Override
	default void setEnclosingClass(IClass enclosingClass)
	{
	}

	default ICallableMember getMethod()
	{
		return null;
	}

	default void setMethod(ICallableMember method)
	{
	}

	int getIndex();

	void setIndex(int index);

	@Override
	boolean isField();

	@Override
	boolean isVariable();

	default boolean isVarargs()
	{
		return false;
	}

	default void setVarargs(boolean varargs)
	{
	}

	default AnnotationVisitor visitAnnotation(String internalType)
	{
		if (!this.addRawAnnotation(internalType, null))
		{
			return null;
		}

		IType type = new InternalType(internalType);
		Annotation annotation = new Annotation(type);
		return new AnnotationReader(this, annotation);
	}
}
