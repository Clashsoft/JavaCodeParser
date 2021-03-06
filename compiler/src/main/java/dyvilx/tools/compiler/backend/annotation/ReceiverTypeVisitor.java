package dyvilx.tools.compiler.backend.annotation;

import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.compiler.ast.parameter.IParametric;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.ClassFormat;

public class ReceiverTypeVisitor implements AnnotationVisitor
{
	private final IParametric method;

	public ReceiverTypeVisitor(IParametric method)
	{
		this.method = method;
	}

	@Override
	public void visit(String name, Object value)
	{
		if ("value".equals(name) && value instanceof String)
		{
			final IType receiverType = ClassFormat.extendedToType((String) value);
			this.method.setThisType(receiverType);
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
