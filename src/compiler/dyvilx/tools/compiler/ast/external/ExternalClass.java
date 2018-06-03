package dyvilx.tools.compiler.ast.external;

import dyvil.collection.Entry;
import dyvil.collection.Map;
import dyvil.collection.Set;
import dyvil.collection.immutable.ArraySet;
import dyvil.collection.mutable.HashMap;
import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.*;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.annotation.AnnotationUtil;
import dyvilx.tools.compiler.ast.attribute.annotation.ExternalAnnotation;
import dyvilx.tools.compiler.ast.attribute.modifiers.ModifierUtil;
import dyvilx.tools.compiler.ast.classes.AbstractClass;
import dyvilx.tools.compiler.ast.classes.ClassBody;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.classes.metadata.IClassMetadata;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.context.CombiningContext;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.generic.TypeParameterList;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.header.IHeaderUnit;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.ClassParameter;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.parameter.ParameterList;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.structure.RootPackage;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.backend.ClassFormat;
import dyvilx.tools.compiler.backend.ClassWriter;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.visitor.*;
import dyvilx.tools.compiler.sources.DyvilFileType;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static dyvilx.tools.compiler.backend.ClassFormat.*;

public final class ExternalClass extends AbstractClass
{
	private static final int METADATA            = 1;
	private static final int SUPER_TYPES         = 1 << 1;
	private static final int GENERICS            = 1 << 2;
	private static final int ANNOTATIONS         = 1 << 5;
	private static final int MEMBER_CLASSES      = 1 << 6;

	protected Package thePackage;

	private byte                resolved;
	private Map<String, String> innerTypes; // inner name -> full internal name
	private Set<String>         classParameters;

	public ExternalClass(Name name)
	{
		this.name = name;
	}

	@Override
	public SourcePosition getPosition()
	{
		return null;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
	}

	private IContext getCombiningContext()
	{
		return new CombiningContext(this, Package.rootPackage);
	}

	private void resolveMetadata()
	{
		if ((this.resolved & METADATA) != 0)
		{
			return;
		}

		this.resolved |= METADATA;

		final IContext context = this.getCombiningContext();

		this.metadata = IClass.getClassMetadata(this, this.attributes.flags());
		this.metadata.resolveTypesHeader(null, context);
		this.metadata.resolveTypesBody(null, context);
	}

	private void resolveGenerics()
	{
		if ((this.resolved & GENERICS) != 0)
		{
			return;
		}

		this.resolved |= GENERICS;

		final int typeParams;
		if (this.typeParameters == null || (typeParams = this.typeParameters.size()) <= 0)
		{
			return;
		}

		final IContext context = this.getCombiningContext();

		for (int i = 0; i < typeParams; i++)
		{
			final ITypeParameter typeParameter = this.typeParameters.get(i);
			typeParameter.resolveTypes(null, context);
		}
	}

	private void resolveSuperTypes()
	{
		if ((this.resolved & SUPER_TYPES) != 0)
		{
			return;
		}

		this.resolved |= SUPER_TYPES;

		final IContext context = this.getCombiningContext();
		if (this.superType != null)
		{
			this.superType = this.superType.resolveType(null, context);
		}

		if (this.interfaces != null)
		{
			this.interfaces.resolveTypes(null, context);
		}
	}

	private void resolveAnnotations()
	{
		if ((this.resolved & ANNOTATIONS) != 0)
		{
			return;
		}

		this.resolved |= ANNOTATIONS;
		this.attributes.resolveTypes(null, RootPackage.rootPackage, this);
	}

	private void resolveMemberClasses()
	{
		if ((this.resolved & MEMBER_CLASSES) != 0)
		{
			return;
		}

		this.resolved |= MEMBER_CLASSES;
		if (this.innerTypes == null)
		{
			return;
		}

		for (Entry<String, String> entry : this.innerTypes)
		{
			final String innerName = entry.getKey();
			this.resolveClass(Name.fromRaw(innerName)); // adds the class to the body
		}
		this.innerTypes.clear(); // we no longer need this
		this.innerTypes = null;
	}

	public void setClassParameters(String[] classParameters)
	{
		this.classParameters = ArraySet.apply(classParameters);
	}

	@Override
	public String getFullName()
	{
		if (this.fullName != null)
		{
			return this.fullName;
		}
		if (this.enclosingClass != null)
		{
			return this.fullName = this.enclosingClass.getFullName() + '.' + this.getName();
		}
		if (this.thePackage != null)
		{
			return this.fullName = this.thePackage.getFullName() + '.' + this.getName();
		}
		return this.fullName = this.getName().toString();
	}

	@Override
	public IHeaderUnit getHeader()
	{
		return null;
	}

	@Override
	public void setHeader(IHeaderUnit unit)
	{
	}

	@Override
	public IClass getThisClass()
	{
		this.resolveGenerics();
		return this;
	}

	@Override
	public IType getThisType()
	{
		this.resolveGenerics();
		return super.getThisType();
	}

	@Override
	public IType getSuperType()
	{
		this.resolveSuperTypes();
		return this.superType;
	}

	@Override
	public boolean isSubClassOf(IType type)
	{
		this.resolveSuperTypes();
		return super.isSubClassOf(type);
	}

	@Override
	public TypeParameterList getTypeParameters()
	{
		this.resolveGenerics();
		return super.getTypeParameters();
	}

	@Override
	public AttributeList getAttributes()
	{
		this.resolveAnnotations();
		return super.getAttributes();
	}

	@Override
	public IType resolveType(ITypeParameter typeParameter, IType concrete)
	{
		this.resolveGenerics();
		this.resolveSuperTypes();
		return super.resolveType(typeParameter, concrete);
	}

	@Override
	public IClassMetadata getMetadata()
	{
		this.resolveMetadata();
		return this.metadata;
	}

	@Override
	public IMethod getFunctionalMethod()
	{
		this.resolveMetadata();
		return super.getFunctionalMethod();
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public boolean checkImplements(IMethod candidate, ITypeContext typeContext)
	{
		this.resolveMetadata();
		this.resolveGenerics();
		this.resolveSuperTypes();
		return super.checkImplements(candidate, typeContext);
	}

	@Override
	public void checkMethods(MarkerList markers, IClass checkedClass, ITypeContext typeContext,
		                        Set<IClass> checkedClasses)
	{
		this.resolveGenerics();
		this.resolveSuperTypes();
		super.checkMethods(markers, checkedClass, typeContext, checkedClasses);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
	}

	@Override
	public void foldConstants()
	{
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
	}

	@Override
	public IClass resolveClass(Name name)
	{
		final IClass bodyClass = this.body.getClass(name);
		if (bodyClass != null)
		{
			return bodyClass;
		}

		if (this.innerTypes == null)
		{
			return null;
		}

		final String internal = this.innerTypes.get(name.qualified);
		if (internal == null)
		{
			return null;
		}

		// Resolve the class name and add it to the body
		final String fileName = internal + DyvilFileType.CLASS_EXTENSION;
		return Package.loadClass(fileName, name, this.body);
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		final IParameter parameter = this.resolveClassParameter(name);
		if (parameter != null)
		{
			return parameter;
		}

		// Own fields
		IDataMember field = this.body.getField(name);
		if (field != null)
		{
			return field;
		}

		this.resolveSuperTypes();

		// Inherited Fields
		if (this.superType != null)
		{
			field = this.superType.resolveField(name);
			if (field != null)
			{
				return field;
			}
		}
		return null;
	}

	@Override
	public IValue resolveImplicit(IType type)
	{
		this.resolveMemberClasses();
		return super.resolveImplicit(type);
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		this.resolveGenerics();

		/*
		Note: unlike AbstractClass.getMethodMatches, this does not check the Class Parameter Properties, because
		External classes do not have any class parameters with associated properties
		*/
		this.body.getMethodMatches(list, receiver, name, arguments);
		// The same applies for the Metadata

		if (list.hasCandidate())
		{
			return;
		}

		this.resolveSuperTypes();

		if (this.superType != null)
		{
			this.superType.getMethodMatches(list, receiver, name, arguments);
		}

		if (list.hasCandidate() || this.interfaces == null)
		{
			return;
		}

		for (IType type : this.interfaces)
		{
			type.getMethodMatches(list, receiver, name, arguments);
		}
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		this.resolveGenerics();
		this.body.getImplicitMatches(list, value, targetType);
	}

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, ArgumentList arguments)
	{
		this.resolveSuperTypes();
		this.resolveGenerics();

		this.body.getConstructorMatches(list, arguments);
	}

	public void visit(int access, String name, String signature, String superName, String[] interfaces)
	{
		this.attributes = readModifiers(access);
		this.internalName = name;

		this.body = new ClassBody(this);
		if (interfaces != null)
		{
			this.interfaces = new TypeList(interfaces.length);
		}

		int index = name.lastIndexOf('$');
		if (index == -1)
		{
			index = name.lastIndexOf('/');
		}
		if (index == -1)
		{
			this.name = Name.fromQualified(name);
			this.thePackage = Package.rootPackage;
			this.fullName = name;
		}
		else
		{
			this.name = Name.fromQualified(name.substring(index + 1));
			// Do not set 'fullName' here
			this.thePackage = Package.rootPackage.resolveInternalPackage(name.substring(0, index));
		}

		if (signature != null)
		{
			ClassFormat.readClassSignature(signature, this);
		}
		else
		{
			this.superType = superName != null ? ClassFormat.internalToType(superName) : null;

			if (interfaces != null)
			{
				for (String internal : interfaces)
				{
					this.interfaces.add(ClassFormat.internalToType(internal));
				}
			}
		}
	}

	public AnnotationVisitor visitAnnotation(String type)
	{
		switch (type)
		{
		case ModifierUtil.DYVIL_MODIFIERS:
			return new ModifierVisitor(this.attributes);
		case AnnotationUtil.CLASS_PARAMETERS:
			return new ClassParameterAnnotationVisitor(this);
		}

		String internal = ClassFormat.extendedToInternal(type);
		if (!this.skipAnnotation(internal, null))
		{
			Annotation annotation = new ExternalAnnotation(ClassFormat.internalToType(internal));
			return new AnnotationReader(this, annotation);
		}
		return null;
	}

	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc)
	{
		Annotation annotation = new ExternalAnnotation(ClassFormat.extendedToType(desc));
		switch (TypeReference.getSort(typeRef))
		{
		case TypeReference.CLASS_EXTENDS:
		{
			final int index = TypeReference.getSuperTypeIndex(typeRef);
			if (index < 0)
			{
				this.superType = IType.withAnnotation(this.superType, annotation, typePath);
			}
			else
			{
				this.interfaces.set(index, IType.withAnnotation(this.interfaces.get(index), annotation, typePath));
			}
			break;
		}
		case TypeReference.CLASS_TYPE_PARAMETER:
		{
			ITypeParameter typeVar = this.typeParameters.get(TypeReference.getTypeParameterIndex(typeRef));
			if (!typeVar.skipAnnotation(annotation.getTypeDescriptor(), annotation))
			{
				return null;
			}

			typeVar.getAttributes().add(annotation);
			break;
		}
		case TypeReference.CLASS_TYPE_PARAMETER_BOUND:
		{
			ITypeParameter typeVar = this.typeParameters.get(TypeReference.getTypeParameterIndex(typeRef));
			typeVar.addBoundAnnotation(annotation, TypeReference.getTypeParameterBoundIndex(typeRef), typePath);
			break;
		}
		}
		return new AnnotationReader(null, annotation);
	}

	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
	{
		IType type = ClassFormat.readFieldType(signature == null ? desc : signature);

		if (this.classParameters != null && this.classParameters.contains(name))
		{
			final ClassParameter param = new ExternalClassParameter(this, Name.fromQualified(name), desc, type,
			                                                        readModifiers(access));
			this.parameters.add(param);
			return new SimpleFieldVisitor(param);
		}

		final ExternalField field = new ExternalField(this, Name.fromQualified(name), desc, type,
		                                              readModifiers(access));

		if (value != null)
		{
			field.setConstantValue(value);
		}

		this.body.addDataMember(field);

		return new SimpleFieldVisitor(field);
	}

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
	{
		if ((access & Modifiers.SYNTHETIC) != 0)
		{
			return null;
		}

		switch (name)
		{
		case "<clinit>":
			return null;
		case "<init>":
			if (this.hasModifier(Modifiers.ENUM))
			{
				return null;
			}

			final ExternalConstructor ctor = new ExternalConstructor(this, readModifiers(access));

			if (signature != null)
			{
				readConstructorType(signature, ctor);
			}
			else
			{
				readConstructorType(desc, ctor);

				if (exceptions != null)
				{
					readExceptions(exceptions, ctor.getExceptions());
				}
			}

			if ((access & Modifiers.ACC_VARARGS) != 0)
			{
				final ParameterList parameterList = ctor.getExternalParameterList();
				parameterList.get(parameterList.size() - 1).setVarargs();
			}

			this.body.addConstructor(ctor);

			return new SimpleMethodVisitor(ctor);
		}

		if (this.isAnnotation() && (access & Modifiers.STATIC) == 0)
		{
			final ClassParameter param = new ExternalClassParameter(this, Name.fromQualified(name), desc.substring(2),
			                                                        readReturnType(desc), readModifiers(access));
			this.parameters.add(param);
			return new AnnotationClassVisitor(param);
		}

		final ExternalMethod method = new ExternalMethod(this, name, desc, signature, readModifiers(access));

		if (signature != null)
		{
			readMethodType(signature, method);
		}
		else
		{
			readMethodType(desc, method);

			if (exceptions != null)
			{
				readExceptions(exceptions, method.getExceptions());
			}
		}

		if ((access & Modifiers.ACC_VARARGS) != 0)
		{
			final ParameterList parameterList = method.getExternalParameterList();
			parameterList.get(parameterList.size() - 1).setVarargs();
		}

		this.body.addMethod(method);
		return new SimpleMethodVisitor(method);
	}

	public void visitInnerClass(String name, String outerName, String innerName)
	{
		if (innerName == null || !this.internalName.equals(outerName))
		{
			return;
		}

		if (this.innerTypes == null)
		{
			this.innerTypes = new HashMap<>();
		}

		this.innerTypes.put(innerName, name);
	}

	public void visitEnd()
	{
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
	}

	@Override
	public void writeClassInit(MethodWriter writer) throws BytecodeException
	{
	}

	@Override
	public void writeStaticInit(MethodWriter writer) throws BytecodeException
	{
	}

	@Override
	public void writeSignature(DataOutput out) throws IOException
	{
	}

	@Override
	public void readSignature(DataInput in) throws IOException
	{
	}
}