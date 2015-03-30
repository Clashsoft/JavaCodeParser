package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.LiteralValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class StringValue extends ASTNode implements IConstantValue
{
	public static final Type	STRING_CONVERTIBLE	= new Type(Package.dyvilLangLiteral.resolveClass("StringConvertible"));
	
	public String				value;
	
	public StringValue(String value)
	{
		this.value = value;
	}
	
	public StringValue(ICodePosition position, String value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public int getValueType()
	{
		return STRING;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return false;
	}
	
	@Override
	public Type getType()
	{
		return Types.STRING;
	}
	
	@Override
	public IValue withType(IType type)
	{
		if (type.isSuperTypeOf(Types.STRING))
		{
			return this;
		}
		if (STRING_CONVERTIBLE.isSuperTypeOf(type))
		{
			return new LiteralValue(type, this);
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type.isSuperTypeOf(Types.STRING) || STRING_CONVERTIBLE.isSuperTypeOf(type);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (type == Types.STRING)
		{
			return 3;
		}
		if (type.isSuperTypeOf(Types.STRING) || STRING_CONVERTIBLE.isSuperTypeOf(type))
		{
			return 2;
		}
		return 0;
	}
	
	@Override
	public String toObject()
	{
		return this.value;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		writer.writeLDC(this.value);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		writer.writeLDC(this.value);
		writer.writeInsn(Opcodes.ARETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		int len = this.value.length();
		buffer.ensureCapacity(buffer.length() + len + 3);
		buffer.append('"');
		for (int i = 0; i < len; i++)
		{
			char c = this.value.charAt(i);
			switch (c)
			{
			case '"':
				buffer.append("\\\"");
				continue;
			case '\\':
				buffer.append("\\\\");
				continue;
			case '\n':
				buffer.append("\\n");
				continue;
			case '\t':
				buffer.append("\\t");
				continue;
			case '\r':
				buffer.append("\\r");
				continue;
			case '\b':
				buffer.append("\\b");
				continue;
			case '\f':
				buffer.append("\\f");
				continue;
			}
			buffer.append(c);
		}
		buffer.append('"');
	}
}
