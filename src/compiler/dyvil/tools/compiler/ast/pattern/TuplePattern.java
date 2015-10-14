package dyvil.tools.compiler.ast.pattern;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.ast.type.TupleType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class TuplePattern extends Pattern implements IPatternList
{
	private IPattern[]	patterns	= new IPattern[3];
	private int			patternCount;
	private IType		tupleType;
	
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
	public IType getType()
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
	public IPattern withType(IType type, MarkerList markers)
	{
		IClass tupleClass = TupleType.getTupleClass(this.patternCount);
		if (!tupleClass.isSubTypeOf(type))
		{
			return null;
		}
		
		this.tupleType = type;
		for (int i = 0; i < this.patternCount; i++)
		{
			IType type1 = type.resolveTypeSafely(tupleClass.getTypeVariable(i));
			IPattern pattern = this.patterns[i];
			IPattern pattern1 = pattern.withType(type1, markers);
			if (pattern1 == null)
			{
				Marker m = I18n.createMarker(pattern.getPosition(), "tuple.pattern.type");
				m.addInfo("Pattern Type: " + pattern.getType());
				m.addInfo("Tuple Type: " + type1);
				markers.add(m);
			}
			else
			{
				this.patterns[i] = pattern1;
			}
		}
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return TupleType.isSuperType(type, this.patterns, this.patternCount);
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
	public IDataMember resolveField(Name name)
	{
		for (int i = 0; i < this.patternCount; i++)
		{
			IDataMember f = this.patterns[i].resolveField(name);
			if (f != null)
			{
				return f;
			}
		}
		
		return null;
	}
	
	@Override
	public IPattern resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.patternCount; i++)
		{
			this.patterns[i] = this.patterns[i].resolve(markers, context);
		}
		
		return this;
	}
	
	@Override
	public void writeJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		ITypeList typeList = (ITypeList) this.tupleType;
		String internal = this.tupleType.getInternalName();
		Label target = new Label();
		
		if (varIndex < 0)
		{
			varIndex = writer.localCount();
			writer.writeVarInsn(Opcodes.ASTORE, varIndex);
		}
		
		for (int i = 0; i < this.patternCount; i++)
		{
			if (this.patterns[i].getPatternType() == WILDCARD)
			{
				// Skip wildcard Patterns
				continue;
			}
			
			// Copy below
			writer.writeVarInsn(Opcodes.ALOAD, varIndex);
			writer.writeFieldInsn(Opcodes.GETFIELD, internal, "_" + (i + 1), "Ljava/lang/Object;");
			writer.writeTypeInsn(Opcodes.CHECKCAST, typeList.getType(i).getInternalName());
			this.patterns[i].writeInvJump(writer, -1, target);
		}
		
		writer.writeJumpInsn(Opcodes.GOTO, elseLabel);
		writer.writeLabel(target);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		ITypeList typeList = (ITypeList) this.tupleType;
		String internal = this.tupleType.getInternalName();
		
		if (varIndex < 0)
		{
			varIndex = writer.localCount();
			writer.writeVarInsn(Opcodes.ASTORE, varIndex);
		}
		
		for (int i = 0; i < this.patternCount; i++)
		{
			if (this.patterns[i].getPatternType() == WILDCARD)
			{
				// Skip wildcard patterns
				continue;
			}
			
			// Copy above
			writer.writeVarInsn(Opcodes.ALOAD, varIndex);
			writer.writeFieldInsn(Opcodes.GETFIELD, internal, "_" + (i + 1), "Ljava/lang/Object;");
			writer.writeTypeInsn(Opcodes.CHECKCAST, typeList.getType(i).getInternalName());
			this.patterns[i].writeInvJump(writer, -1, elseLabel);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Expression.tupleStart);
		Util.astToString(prefix, this.patterns, this.patternCount, Formatting.Expression.tupleSeperator, buffer);
		buffer.append(Formatting.Expression.tupleEnd);
	}
}
