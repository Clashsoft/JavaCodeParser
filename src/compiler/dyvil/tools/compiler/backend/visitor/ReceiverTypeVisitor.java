package dyvil.tools.compiler.backend.visitor;

import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassFormat;

public class ReceiverTypeVisitor implements AnnotationVisitor
{
	private final IMethod method;

	public ReceiverTypeVisitor(IMethod method)
	{
		this.method = method;
	}

	@Override
	public void visit(String name, Object value)
	{
		if ("value".equals(name) && value instanceof String)
		{
			final IType receiverType = ClassFormat.extendedToType((String) value);
			this.method.setReceiverType(receiverType);
		}
	}

	@Override
	public void visitEnum(String name, String desc, String value)
	{
	}

	@Override
	public AnnotationVisitor visitAnnotation(String name, String desc)
	{
		return null;
	}

	@Override
	public AnnotationVisitor visitArray(String name)
	{
		return null;
	}

	@Override
	public void visitEnd()
	{
	}
}