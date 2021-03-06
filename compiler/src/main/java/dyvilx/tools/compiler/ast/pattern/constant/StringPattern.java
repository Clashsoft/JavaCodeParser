package dyvilx.tools.compiler.ast.pattern.constant;

import dyvil.lang.Formattable;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.pattern.AbstractPattern;
import dyvilx.tools.compiler.ast.pattern.Pattern;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.lexer.StringLiterals;
import dyvilx.tools.parsing.marker.MarkerList;

public final class StringPattern extends AbstractPattern
{
	private String value;

	public StringPattern(SourcePosition position, String value)
	{
		this.position = position;
		this.value = value;
	}

	@Override
	public int getPatternType()
	{
		return STRING;
	}

	@Override
	public IType getType()
	{
		return Types.STRING;
	}

	@Override
	public Pattern withType(IType type, MarkerList markers)
	{
		if (Types.isSuperType(type, Types.STRING))
		{
			// also accepts String! or String?
			// Strings don't need type checks because the match is performed via "literal".equals(value)
			// thus value can have any type (that is a super-type of String)
			return this;
		}
		return null;
	}

	@Override
	public Object getConstantValue()
	{
		return this.value;
	}

	// Switch Resolution

	@Override
	public boolean hasSwitchHash()
	{
		return true;
	}

	@Override
	public boolean isSwitchHashInjective()
	{
		return false;
	}

	@Override
	public int getSwitchHashValue()
	{
		return this.value.hashCode();
	}

	// Compilation

	@Override
	public void writeJumpOnMismatch(MethodWriter writer, int varIndex, Label target) throws BytecodeException
	{
		writeJumpOnMismatch(writer, varIndex, target, this.value);
	}

	protected static void writeJumpOnMismatch(MethodWriter writer, int varIndex, Label elseLabel, String value)
		throws BytecodeException
	{
		writer.visitLdcInsn(value);
		if (varIndex >= 0)
		{
			writer.visitVarInsn(Opcodes.ALOAD, varIndex);
		}
		else
		{
			writer.visitInsn(Opcodes.SWAP);
		}
		writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
		writer.visitJumpInsn(Opcodes.IFEQ, elseLabel);
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		StringLiterals.appendStringLiteral(this.value, buffer);
	}
}
