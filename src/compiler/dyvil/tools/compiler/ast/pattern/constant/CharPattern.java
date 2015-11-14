package dyvil.tools.compiler.ast.pattern.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.pattern.IPattern;
import dyvil.tools.compiler.ast.pattern.Pattern;
import dyvil.tools.compiler.ast.pattern.TypeCheckPattern;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.lexer.LexerUtil;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class CharPattern extends Pattern
{
	private String value;
	
	private byte type;
	
	public CharPattern(ICodePosition position, String value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public int getPatternType()
	{
		return CHAR;
	}
	
	@Override
	public IType getType()
	{
		return Types.CHAR;
	}
	
	@Override
	public IPattern withType(IType type, MarkerList markers)
	{
		if (this.value.length() == 1 && this.type != STRING)
		{
			IPattern v = IPattern.primitiveWithType(this, type, Types.CHAR);
			if (this.type == CHAR || v != null)
			{
				return v;
			}
		}
		if (this.type == CHAR)
		{
			return null;
		}
		
		if (type == Types.STRING || type.classEquals(Types.STRING))
		{
			return this;
		}
		if (type.isSuperTypeOf(Types.STRING))
		{
			return new TypeCheckPattern(this, Types.STRING);
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (this.value.length() == 1 && this.type != STRING)
		{
			if (type == Types.CHAR || type.isSuperTypeOf(Types.CHAR))
			{
				return true;
			}
		}
		if (this.type == CHAR)
		{
			return false;
		}
		return type == Types.STRING || type.isSuperTypeOf(Types.STRING);
	}
	
	@Override
	public boolean isSwitchable()
	{
		return true;
	}
	
	@Override
	public int switchCases()
	{
		return 1;
	}
	
	@Override
	public int switchValue(int index)
	{
		if (this.type == CHAR)
		{
			return this.value.charAt(0);
		}
		return this.value.hashCode();
	}
	
	@Override
	public int minValue()
	{
		return this.switchValue(0);
	}
	
	@Override
	public int maxValue()
	{
		return this.switchValue(0);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		if (this.type == STRING)
		{
			StringPattern.writeStringInvJump(writer, varIndex, elseLabel, this.value);
			return;
		}
		if (varIndex >= 0)
		{
			writer.writeVarInsn(Opcodes.ILOAD, varIndex);
		}
		writer.writeLDC(this.value.charAt(0));
		writer.writeJumpInsn(Opcodes.IF_ICMPNE, elseLabel);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		LexerUtil.appendCharLiteral(this.value, buffer);
	}
}