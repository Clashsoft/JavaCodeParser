package dyvil.tools.compiler.ast.operator;

import dyvil.tools.parsing.Name;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class Operator implements IOperator
{
	public static final Operator DEFAULT       = new Operator(null, LEFT, 100000);
	public static final Operator DEFAULT_RIGHT = new Operator(null, RIGHT, 100000);

	protected static final int ID_PREFIX      = 0;
	protected static final int ID_INFIX_LEFT  = 1;
	protected static final int ID_INFIX_NONE  = 2;
	protected static final int ID_INFIX_RIGHT = 3;
	protected static final int ID_POSTFIX     = 4;
	protected static final int ID_TERNARY     = 5;

	protected Name name;
	protected Name ternaryName;
	protected int  precedence;
	protected byte id;

	public Operator()
	{
	}

	public Operator(Name name)
	{
		this.name = name;
	}

	public Operator(Name name, byte type)
	{
		this.name = name;
		this.setType(type);
	}

	public Operator(Name name, byte associativity, int precedence)
	{
		this.name = name;
		this.precedence = precedence;

		switch (associativity)
		{
		case LEFT:
			this.id = ID_INFIX_LEFT;
			return;
		case RIGHT:
			this.id = ID_INFIX_RIGHT;
			return;
		default:
			this.id = ID_INFIX_NONE;
		}
	}

	@Override
	public Name getName()
	{
		return this.name;
	}

	@Override
	public void setName(Name name)
	{
		this.name = name;
	}

	@Override
	public Name getTernaryName()
	{
		return this.ternaryName;
	}

	@Override
	public void setTernaryName(Name ternaryName)
	{
		this.ternaryName = ternaryName;
	}

	@Override
	public byte getType()
	{
		switch (this.id)
		{
		case ID_PREFIX:
			return PREFIX;
		case ID_POSTFIX:
			return POSTFIX;
		case ID_TERNARY:
			return TERNARY;
		}
		return INFIX;
	}

	@Override
	public void setType(byte type)
	{
		switch (type)
		{
		case PREFIX:
			this.id = ID_PREFIX;
			return;
		case POSTFIX:
			this.id = ID_POSTFIX;
			return;
		case INFIX:
			this.id = ID_INFIX_NONE;
			return;
		case TERNARY:
			this.id = ID_TERNARY;
		}
	}

	@Override
	public byte getAssociativity()
	{
		switch (this.id)
		{
		case ID_INFIX_LEFT:
			return LEFT;
		case ID_INFIX_RIGHT:
			return RIGHT;
		}
		return NONE;
	}

	@Override
	public void setAssociativity(byte associativity)
	{
		switch (associativity)
		{
		case LEFT:
			this.id = ID_INFIX_LEFT;
			return;
		case RIGHT:
			this.id = ID_INFIX_RIGHT;
			return;
		case NONE:
			this.id = ID_INFIX_NONE;
		}
	}

	@Override
	public int getPrecedence()
	{
		return this.precedence;
	}

	@Override
	public void setPrecedence(int precedence)
	{
		this.precedence = precedence;
	}

	@Override
	public int comparePrecedence(IOperator other)
	{
		return Integer.compare(this.precedence, other.getPrecedence());
	}

	@Override
	public void writeData(DataOutput dos) throws IOException
	{
		dos.writeUTF(this.name.unqualified);
		dos.writeByte(this.id);
		if (this.id != ID_PREFIX && this.id != ID_POSTFIX)
		{
			dos.writeInt(this.precedence);
		}
		if (this.id == ID_TERNARY)
		{
			dos.writeUTF(this.ternaryName.unqualified);
		}
	}

	@Override
	public void readData(DataInput in) throws IOException
	{
		this.name = Name.from(in.readUTF());

		this.id = in.readByte();
		if (this.id != ID_PREFIX && this.id != ID_POSTFIX)
		{
			this.precedence = in.readInt();
		}
		if (this.id == ID_TERNARY)
		{
			this.ternaryName = Name.from(in.readUTF());
		}
	}

	public static Operator read(DataInput in) throws IOException
	{
		final Operator operator = new Operator();
		operator.readData(in);
		return operator;
	}

	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		this.toString(buf);
		return buf.toString();
	}

	@Override
	public void toString(StringBuilder buffer)
	{
		switch (this.id)
		{
		case ID_PREFIX:
			buffer.append("prefix operator ").append(this.name);
			return;
		case ID_POSTFIX:
			buffer.append("postfix operator ").append(this.name);
			return;
		case ID_INFIX_NONE:
			buffer.append("infix operator ").append(this.name).append(" { precedence ").append(this.precedence)
			      .append(" }");
			return;
		case ID_INFIX_LEFT:
			buffer.append("infix operator ").append(this.name).append(" { associativity left, precedence ")
			      .append(this.precedence).append(" }");
			return;
		case ID_INFIX_RIGHT:
			buffer.append("infix operator ").append(this.name).append(" { associativity right, precedence ")
			      .append(this.precedence).append(" }");
			return;
		case ID_TERNARY:
			buffer.append("infix operator ").append(this.name).append(' ').append(this.ternaryName)
			      .append(" { precedence ").append(this.precedence).append(" }");
		}
	}
}
