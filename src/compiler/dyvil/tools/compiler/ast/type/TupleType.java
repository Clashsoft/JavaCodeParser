package dyvil.tools.compiler.ast.type;

import dyvil.lang.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.util.Util;

public final class TupleType implements IType, ITypeList
{
	public static final IClass[]	tupleClasses	= new IClass[22];
	public static final String[]	descriptors		= new String[22];
	
	protected IType[]				types;
	protected int					typeCount;
	
	public TupleType()
	{
		this.types = new IType[2];
	}
	
	public TupleType(int size)
	{
		this.types = new IType[size];
	}
	
	// ITypeList Overrides
	
	public static boolean isSuperType(IType type, ITyped[] typedArray, int count)
	{
		if (!tupleClasses[count].isSubTypeOf(type))
		{
			return false;
		}
		int typeTag = type.typeTag();
		if (typeTag != GENERIC && typeTag != TUPLE)
		{
			return false;
		}
		
		ITypeList typeList = (ITypeList) type;
		
		for (int i = 0; i < count; i++)
		{
			if (!typedArray[i].isType(typeList.getType(i)))
			{
				return false;
			}
		}
		return true;
	}
	
	public static String getConstructorDescriptor(int typeCount)
	{
		if (typeCount < 22)
		{
			String s = descriptors[typeCount];
			if (s != null)
			{
				return s;
			}
		}
		
		StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		for (int i = 0; i < typeCount; i++)
		{
			buffer.append("Ljava/lang/Object;");
		}
		buffer.append(")V");
		
		String s = buffer.toString();
		if (typeCount < 22)
		{
			descriptors[typeCount] = s;
		}
		return s;
	}
	
	public static IClass getTupleClass(int count)
	{
		IClass iclass = tupleClasses[count];
		if (iclass != null)
		{
			return iclass;
		}
		
		iclass = Package.dyvilTuple.resolveClass("Tuple" + count);
		tupleClasses[count] = iclass;
		return iclass;
	}
	
	@Override
	public int typeTag()
	{
		return TUPLE;
	}
	
	@Override
	public IClass getTheClass()
	{
		return getTupleClass(this.typeCount);
	}
	
	@Override
	public Name getName()
	{
		return null;
	}
	
	@Override
	public int typeCount()
	{
		return this.typeCount;
	}
	
	@Override
	public void setType(int index, IType type)
	{
		this.types[index] = type;
	}
	
	@Override
	public void addType(IType type)
	{
		int index = this.typeCount++;
		if (this.typeCount > this.types.length)
		{
			IType[] temp = new IType[this.typeCount];
			System.arraycopy(this.types, 0, temp, 0, index);
			this.types = temp;
		}
		this.types[index] = type;
	}
	
	@Override
	public IType getType(int index)
	{
		return this.types[index];
	}
	
	// IType Overrides
	
	@Override
	public boolean isSuperTypeOf(IType type)
	{
		if (!tupleClasses[this.typeCount].isSubTypeOf(type))
		{
			return false;
		}
		int typeTag = type.typeTag();
		if (typeTag != GENERIC && typeTag != TUPLE)
		{
			return false;
		}
		
		ITypeList typeList = (ITypeList) type;
		
		for (int i = 0; i < this.typeCount; i++)
		{
			if (typeList.getType(i).isSuperTypeOf(this.types[i]))
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public IType getSuperType()
	{
		return Types.OBJECT;
	}
	
	@Override
	public boolean classEquals(IType type)
	{
		return this.isSuperTypeOf(type);
	}
	
	@Override
	public boolean hasTypeVariables()
	{
		return false;
	}
	
	@Override
	public IType getConcreteType(ITypeContext context)
	{
		TupleType tt = new TupleType(this.typeCount);
		tt.typeCount = this.typeCount;
		for (int i = 0; i < this.typeCount; i++)
		{
			tt.types[i] = this.types[i].getConcreteType(context);
		}
		return tt;
	}
	
	@Override
	public IType resolveType(ITypeVariable typeVar)
	{
		return Types.ANY; // FIXME
	}
	
	@Override
	public IType resolveType(ITypeVariable typeVar, IType concrete)
	{
		return Types.ANY; // FIXME
	}
	
	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		int typeTag = concrete.typeTag();
		if (typeTag != GENERIC && typeTag != TUPLE)
		{
			return;
		}
		
		ITypeList typeList = (ITypeList) concrete;
		if (typeList.typeCount() != this.typeCount)
		{
			return;
		}
		
		for (int i = 0; i < this.typeCount; i++)
		{
			this.types[i].inferTypes(typeList.getType(i), typeContext);
		}
	}
	
	@Override
	public boolean isResolved()
	{
		return false;
	}
	
	@Override
	public IType resolve(MarkerList markers, IContext context)
	{
		if (this.typeCount == 0)
		{
			return Types.VOID;
		}
		if (this.typeCount == 1)
		{
			return this.types[0].resolve(markers, context);
		}
		
		for (int i = 0; i < this.typeCount; i++)
		{
			IType t = this.types[i].resolve(markers, context);
			
			// Tuple Value Boxing
			if (t.isPrimitive())
			{
				this.types[i] = t.getReferenceType();
			}
			else
			{
				this.types[i] = t;
			}
		}
		return this;
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		return this.getTheClass().resolveField(name);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		this.getTheClass().getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		this.getTheClass().getConstructorMatches(list, arguments);
	}
	
	@Override
	public byte getVisibility(IClassMember member)
	{
		return this.getTheClass().getVisibility(member);
	}
	
	@Override
	public IMethod getFunctionalMethod()
	{
		return null;
	}
	
	@Override
	public String getInternalName()
	{
		return this.getTheClass().getInternalName();
	}
	
	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		buffer.append('L').append(this.getInternalName()).append(';');
	}
	
	@Override
	public String getSignature()
	{
		StringBuilder buf = new StringBuilder();
		this.appendSignature(buf);
		return buf.toString();
	}
	
	@Override
	public void appendSignature(StringBuilder buf)
	{
		buf.append('L').append(this.getInternalName());
		buf.append('<');
		for (IType t : this.types)
		{
			t.appendSignature(buf);
		}
		buf.append('>').append(';');
	}
	
	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.writeLDC(this.typeCount);
		writer.writeNewArray("dyvil/lang/Type", 1);
		for (int i = 0; i < this.typeCount; i++)
		{
			writer.writeInsn(Opcodes.DUP);
			writer.writeLDC(i);
			this.types[i].writeTypeExpression(writer);
			writer.writeInsn(Opcodes.AASTORE);
		}
		
		writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/reflect/type/TupleType", "apply", "([Ldyvil/lang/Type;)Ldyvil/reflect/type/TupleType;", false);
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder("(");
		builder.append(this.types[0]);
		for (int i = 1; i < this.typeCount; i++)
		{
			builder.append(", ").append(this.types[i]);
		}
		return builder.append(")").toString();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Expression.tupleStart);
		Util.astToString(prefix, this.types, this.typeCount, Formatting.Expression.tupleSeperator, buffer);
		buffer.append(Formatting.Expression.tupleEnd);
	}
	
	@Override
	public IType clone()
	{
		TupleType tt = new TupleType(this.typeCount);
		tt.typeCount = this.typeCount;
		System.arraycopy(this.types, 0, tt.types, 0, this.typeCount);
		return tt;
	}
}
