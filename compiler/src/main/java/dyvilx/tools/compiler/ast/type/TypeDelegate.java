package dyvilx.tools.compiler.ast.type;

import dyvil.lang.Formattable;
import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.TypeAnnotatableVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.constant.IConstantValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class TypeDelegate implements IType, Typed
{
	protected IType type;

	protected abstract IType wrap(IType type);

	@Override
	public SourcePosition getPosition()
	{
		return this.type.getPosition();
	}

	@Override
	public boolean isPrimitive()
	{
		return this.type.isPrimitive();
	}

	@Override
	public int getTypecode()
	{
		return this.type.getTypecode();
	}

	@Override
	public boolean isGenericType()
	{
		return this.type.isGenericType();
	}

	@Override
	public boolean useNonNullAnnotation()
	{
		return this.type.useNonNullAnnotation();
	}

	@Override
	public boolean hasTag(int tag)
	{
		return IType.super.hasTag(tag) || this.type.hasTag(tag);
	}

	@Override
	public boolean canExtract(Class<? extends IType> type)
	{
		return IType.super.canExtract(type) || this.type.canExtract(type);
	}

	@Override
	public <T extends IType> T extract(Class<T> type)
	{
		if (IType.super.canExtract(type))
		{
			return (T) this;
		}
		return this.type.extract(type);
	}

	@Override
	public IType getType()
	{
		return this.type;
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}

	@Override
	public Name getName()
	{
		return this.type.getName();
	}

	@Override
	public IClass getTheClass()
	{
		return this.type.getTheClass();
	}

	@Override
	public IType getObjectType()
	{
		final IType objectType = this.type.getObjectType();
		return objectType != this.type ? this.wrap(objectType) : this;
	}

	@Override
	public IType asParameterType()
	{
		final IType type = this.type.asParameterType();
		return type == this.type ? this : this.wrap(type);
	}

	@Override
	public String getTypePrefix()
	{
		return this.type.getTypePrefix();
	}

	@Override
	public IClass getRefClass()
	{
		return this.type.getRefClass();
	}

	@Override
	public IType getSimpleRefType()
	{
		return this.type.getSimpleRefType();
	}

	@Override
	public IClass getArrayClass()
	{
		return this.type.getArrayClass();
	}

	@Override
	public Mutability getMutability()
	{
		final Mutability mutability = IType.super.getMutability();
		if (mutability != Mutability.UNDEFINED)
		{
			return mutability;
		}
		return this.type.getMutability();
	}

	@Override
	public IMethod getBoxMethod()
	{
		return this.type.getBoxMethod();
	}

	@Override
	public IMethod getUnboxMethod()
	{
		return this.type.getUnboxMethod();
	}

	// Subtyping

	@Override
	public int subTypeCheckLevel()
	{
		return this.type.subTypeCheckLevel();
	}

	@Override
	public boolean isSameType(IType type)
	{
		return this.type.isSameType(type);
	}

	@Override
	public boolean isSameClass(IType type)
	{
		return this.type.isSameClass(type);
	}

	@Override
	public boolean isSuperTypeOf(IType subType)
	{
		return this.type.isSuperTypeOf(subType);
	}

	@Override
	public boolean isSuperClassOf(IType subType)
	{
		return this.type.isSuperClassOf(subType);
	}

	@Override
	public boolean isSubTypeOf(IType superType)
	{
		return this.type.isSubTypeOf(superType);
	}

	@Override
	public boolean isSubClassOf(IType superType)
	{
		return this.type.isSubClassOf(superType);
	}

	@Override
	public boolean isConvertibleFrom(IType type)
	{
		return this.type.isConvertibleFrom(type);
	}

	@Override
	public boolean isConvertibleTo(IType type)
	{
		return this.type.isConvertibleTo(type);
	}

	@Override
	public IValue convertFrom(IValue value, IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return this.type.convertFrom(value, type, typeContext, markers, context);
	}

	@Override
	public IValue convertTo(IValue value, IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return this.type.convertTo(value, type, typeContext, markers, context);
	}

	// Generics

	@Override
	public IType resolveType(ITypeParameter typeParameter)
	{
		return this.type.resolveType(typeParameter);
	}

	@Override
	public boolean hasTypeVariables()
	{
		return this.type.hasTypeVariables();
	}

	@Override
	public boolean isUninferred()
	{
		return this.type.isUninferred();
	}

	@Override
	public IType getConcreteType(ITypeContext context)
	{
		final IType concreteType = this.type.getConcreteType(context);
		return concreteType != this.type ? this.wrap(concreteType) : this;
	}

	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		this.type.inferTypes(concrete, typeContext);
	}

	@Override
	public boolean isResolved()
	{
		return this.type.isResolved();
	}

	@Override
	public Annotation getAnnotation(IClass type)
	{
		return this.type.getAnnotation(type);
	}

	@Override
	public Package resolvePackage(Name name)
	{
		return this.type.resolvePackage(name);
	}

	@Override
	public IClass resolveClass(Name name)
	{
		return this.type.resolveClass(name);
	}

	@Override
	public ITypeParameter resolveTypeParameter(Name name)
	{
		return this.type.resolveTypeParameter(name);
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return this.type.resolveField(name);
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		this.type.getMethodMatches(list, receiver, name, arguments);
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		this.type.getImplicitMatches(list, value, targetType);
	}

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, ArgumentList arguments)
	{
		this.type.getConstructorMatches(list, arguments);
	}

	@Override
	public IMethod getFunctionalMethod()
	{
		return this.type.getFunctionalMethod();
	}

	@Override
	public String getInternalName()
	{
		return this.type.getInternalName();
	}

	@Override
	public int getDescriptorKind()
	{
		return this.type.getDescriptorKind();
	}

	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		this.type.appendExtendedName(buffer);
	}

	@Override
	public String getSignature()
	{
		return this.type.getSignature();
	}

	@Override
	public void appendSignature(StringBuilder buffer, boolean genericArg)
	{
		this.type.appendSignature(buffer, genericArg);
	}

	@Override
	public void appendDescriptor(StringBuilder buffer, int type)
	{
		this.type.appendDescriptor(buffer, type);
	}

	@Override
	public int getLoadOpcode()
	{
		return this.type.getLoadOpcode();
	}

	@Override
	public int getArrayLoadOpcode()
	{
		return this.type.getArrayLoadOpcode();
	}

	@Override
	public int getStoreOpcode()
	{
		return this.type.getStoreOpcode();
	}

	@Override
	public int getArrayStoreOpcode()
	{
		return this.type.getArrayStoreOpcode();
	}

	@Override
	public int getReturnOpcode()
	{
		return this.type.getReturnOpcode();
	}

	@Override
	public Object getFrameType()
	{
		return this.type.getFrameType();
	}

	@Override
	public int getLocalSlots()
	{
		return this.type.getLocalSlots();
	}

	@Override
	public void writeCast(MethodWriter writer, IType target, int lineNumber) throws BytecodeException
	{
		this.type.writeCast(writer, target, lineNumber);
	}

	@Override
	public void writeClassExpression(MethodWriter writer, boolean wrapPrimitives) throws BytecodeException
	{
		this.type.writeClassExpression(writer, wrapPrimitives);
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		this.type.writeTypeExpression(writer);
	}

	@Override
	public void writeDefaultValue(MethodWriter writer) throws BytecodeException
	{
		this.type.writeDefaultValue(writer);
	}

	@Override
	public IConstantValue getDefaultValue()
	{
		return this.type.getDefaultValue();
	}

	@Override
	public void addAnnotation(Annotation annotation, TypePath typePath, int step, int steps)
	{
		this.type = IType.withAnnotation(this.type, annotation, typePath, step, steps);
	}

	@Override
	public void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
		this.type.writeAnnotations(visitor, typeRef, typePath);
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		IType.writeType(this.type, out);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.type = IType.readType(in);
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}
}
