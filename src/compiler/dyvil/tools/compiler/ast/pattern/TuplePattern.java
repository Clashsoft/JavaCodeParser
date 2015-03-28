package dyvil.tools.compiler.ast.pattern;

import org.objectweb.asm.Label;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.TupleType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public final class TuplePattern extends ASTNode implements IPattern, IPatternList
{
	private IPattern[]	patterns	= new IPattern[3];
	private int			patternCount;
	private TupleType	tupleType;
	
	public TuplePattern(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int getPatternType()
	{
		return TUPLE;
	}
	
	@Override
	public TupleType getType()
	{
		if (this.tupleType != null)
		{
			return this.tupleType;
		}
		
		TupleType t = new TupleType(this.patternCount);
		for (int i = 0; i < this.patternCount; i++)
		{
			t.addType(this.patterns[i].getType());
		}
		return this.tupleType = t;
	}
	
	@Override
	public IPattern withType(IType type)
	{
		if (TupleType.isSuperType(type, this.patterns, this.patternCount))
		{
			this.getType();
			return this;
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (TupleType.isSuperType(type, this.patterns, this.patternCount))
		{
			this.getType();
			return true;
		}
		return false;
	}
	
	@Override
	public int patternCount()
	{
		return this.patternCount;
	}
	
	@Override
	public void setPattern(int index, IPattern pattern)
	{
		this.patterns[index] = pattern;
	}
	
	@Override
	public void addPattern(IPattern pattern)
	{
		int index = this.patternCount++;
		if (this.patternCount > this.patterns.length)
		{
			IPattern[] temp = new IPattern[this.patternCount];
			System.arraycopy(this.patterns, 0, temp, 0, index);
			this.patterns = temp;
		}
		this.patterns[index] = pattern;
	}
	
	@Override
	public IPattern getPattern(int index)
	{
		return this.patterns[index];
	}
	
	@Override
	public IField resolveField(Name name)
	{
		for (int i = 0; i < this.patternCount; i++)
		{
			IField f = this.patterns[i].resolveField(name);
			if (f != null)
			{
				return f;
			}
		}
		
		return null;
	}
	
	// TODO Re-implement Tuple Pattern Compilation
	
	@Override
	public void writeJump(MethodWriter writer, int varIndex, Label elseLabel)
	{
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel)
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Expression.tupleStart);
		Util.astToString(prefix, this.patterns, this.patternCount, Formatting.Expression.tupleSeperator, buffer);
		buffer.append(Formatting.Expression.tupleEnd);
	}
}
