package dyvilx.tools.compiler.ast.classes;

import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.classes.metadata.IClassMetadata;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.field.*;
import dyvilx.tools.compiler.ast.field.capture.CaptureField;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.ParameterList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.method.MethodWriterImpl;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.ast.field.capture.CaptureHelper;
import dyvilx.tools.parsing.marker.MarkerList;

public class AnonymousClass extends CodeClass
{
	protected CaptureHelper<CaptureField> captureHelper = new CaptureHelper<>(CaptureField.factory(this));

	protected FieldThis    thisField;
	protected IConstructor constructor;
	protected String       constructorDesc;

	public AnonymousClass(SourcePosition position)
	{
		super(null, null, AttributeList.of(Modifiers.STATIC));
		this.metadata = new AnonymousClassMetadata(this);
		this.body = new ClassBody(this);
		this.position = position;
	}

	@Override
	public boolean isAnonymous()
	{
		return true;
	}

	public IConstructor getConstructor()
	{
		return this.constructor;
	}

	public void setConstructor(IConstructor constructor)
	{
		this.constructor = constructor;
	}

	@Override
	protected void checkDuplicate(MarkerList markers)
	{
		// don't check anonymous classes for duplicates, the user can't influence their descriptor anyway
		// TODO make sure the descriptor is always unique
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		final String outerName = this.enclosingClass.getName().qualified;
		final String indexString = Integer.toString(compilableList.compilableCount());

		this.name = Name.fromRaw(outerName + '$' + indexString);
		this.fullName = this.enclosingClass.getFullName() + '$' + indexString;
		this.internalName = this.enclosingClass.getInternalName() + '$' + indexString;

		compilableList.addCompilable(this);

		super.cleanup(compilableList, classCompilableList);
	}

	@Override
	public boolean isMember(IVariable variable)
	{
		return super.isMember(variable) || this.captureHelper.isMember(variable);
	}

	@Override
	public IDataMember capture(IVariable variable)
	{
		if (this.isMember(variable))
		{
			return variable;
		}

		return this.captureHelper.capture(variable);
	}

	@Override
	public IAccessible getAccessibleThis(IType type)
	{
		if (Types.isSuperType(type, this.getThisType()))
		{
			return VariableThis.DEFAULT;
		}

		IAccessible outer = this.enclosingClass.getAccessibleThis(type);
		if (outer == null)
		{
			return null;
		}

		if (this.thisField == null)
		{
			return this.thisField = new FieldThis(this, outer, type);
		}
		return this.thisField;
	}

	protected String getConstructorDesc()
	{
		if (this.constructorDesc != null)
		{
			return this.constructorDesc;
		}

		final ParameterList parameterList = this.constructor.getParameters();
		final StringBuilder buf = new StringBuilder();

		buf.append('(');
		for (int i = 0, count = parameterList.size(); i < count; i++)
		{
			parameterList.get(i).getType().appendExtendedName(buf);
		}

		FieldThis thisField = this.thisField;
		if (thisField != null)
		{
			buf.append(thisField.getDescriptor());
		}

		this.captureHelper.appendCaptureTypes(buf);

		return this.constructorDesc = buf.append(")V").toString();
	}

	public void writeConstructorCall(MethodWriter writer, ArgumentList arguments, int lineNumber) throws BytecodeException
	{
		String owner = this.getInternalName();
		String name = "<init>";
		writer.visitTypeInsn(Opcodes.NEW, owner);
		writer.visitInsn(Opcodes.DUP);

		this.constructor.writeArguments(writer, arguments);

		final FieldThis thisField = this.thisField;
		if (thisField != null)
		{
			thisField.getTargetAccess().writeGet(writer);
		}

		this.captureHelper.writeCaptures(writer, lineNumber);

		writer.visitMethodInsn(Opcodes.INVOKESPECIAL, owner, name, this.getConstructorDesc(), false);
	}
}

class AnonymousClassMetadata implements IClassMetadata
{
	private final AnonymousClass theClass;

	public AnonymousClassMetadata(AnonymousClass theClass)
	{
		this.theClass = theClass;
	}

	@Override
	public MemberKind getKind()
	{
		return MemberKind.OBJECT;
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		final CaptureHelper<CaptureField> captureHelper = this.theClass.captureHelper;
		final FieldThis thisField = this.theClass.thisField;
		final IConstructor constructor = this.theClass.constructor;

		captureHelper.writeCaptureFields(writer);

		final MethodWriter initWriter = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.MANDATED, "<init>",
		                                                                                this.theClass
			                                                                                .getConstructorDesc(), null,
		                                                                                null));
		final ParameterList parameterList = constructor.getParameters();
		final int parameterCount = parameterList.size();

		// Signature & Parameter Data

		initWriter.setLocalType(0, this.theClass.getInternalName());

		parameterList.write(initWriter);

		int index = initWriter.localCount();
		int thisIndex = index;

		if (thisField != null)
		{
			thisField.writeField(writer);
			index = initWriter.visitParameter(index, thisField.getName(), thisField.getType(), Modifiers.MANDATED);
		}

		captureHelper.writeCaptureParameters(initWriter, index);

		// Constructor Body

		initWriter.visitCode();
		initWriter.visitVarInsn(Opcodes.ALOAD, 0);
		for (int i = 0; i < parameterCount; i++)
		{
			parameterList.get(i).writeGet(initWriter);
		}
		constructor.writeInvoke(initWriter, 0);

		if (thisField != null)
		{
			initWriter.visitVarInsn(Opcodes.ALOAD, 0);
			initWriter.visitVarInsn(Opcodes.ALOAD, thisIndex);
			initWriter.visitFieldInsn(Opcodes.PUTFIELD, this.theClass.getInternalName(), thisField.getName(),
			                          thisField.getDescriptor());
		}

		captureHelper.writeFieldAssignments(initWriter);

		this.theClass.writeClassInit(initWriter);

		initWriter.visitEnd(Types.VOID);
	}
}

