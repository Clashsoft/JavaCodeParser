package dyvil.tools.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.access.MethodCall;
import dyvil.tools.compiler.ast.field.*;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.generic.WildcardType;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.*;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.Parameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.GenericType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.visitor.AnnotationClassVisitor;
import dyvil.tools.compiler.backend.visitor.SimpleFieldVisitor;
import dyvil.tools.compiler.backend.visitor.SimpleMethodVisitor;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class BytecodeClass extends CodeClass
{
	public Package		thePackage;
	public boolean		typesResolved;
	
	private IType		outerType;
	private List<IType>	innerTypes;
	
	public BytecodeClass(Name name)
	{
		this.name = name;
	}
	
	@Override
	public boolean isSubTypeOf(IType type)
	{
		if (!this.typesResolved)
		{
			this.resolveTypes(null, Package.rootPackage);
		}
		return super.isSubTypeOf(type);
	}
	
	@Override
	public IClassBody getBody()
	{
		if (!this.typesResolved)
		{
			this.resolveTypes(null, Package.rootPackage);
		}
		return this.body;
	}
	
	@Override
	public ITypeVariable getTypeVariable(int index)
	{
		if (!this.typesResolved)
		{
			this.resolveTypes(null, Package.rootPackage);
		}
		return super.getTypeVariable(index);
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.typesResolved = true;
		
		if (this.genericCount > 0)
		{
			GenericType type = new GenericType(this);
			
			for (int i = 0; i < this.genericCount; i++)
			{
				ITypeVariable var = this.generics[i];
				var.resolveTypes(markers, context);
				type.addType(new WildcardType(null, 0, var.getCaptureClass()));
			}
			
			this.type = type;
		}
		
		if (this.superType != null)
		{
			this.superType = this.superType.resolve(markers, context);
		}
		
		for (int i = 0; i < this.interfaceCount; i++)
		{
			this.interfaces[i] = this.interfaces[i].resolve(markers, context);
		}
		
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].resolveTypes(markers, context);
		}
		
		this.body.resolveTypes(markers, this);
		
		if (this.outerType != null)
		{
			this.outerClass = this.outerType.resolve(markers, context).getTheClass();
		}
		
		if (this.innerTypes != null)
		{
			for (IType t : this.innerTypes)
			{
				IClass iclass = t.resolve(markers, context).getTheClass();
				this.body.addClass(iclass);
			}
			this.innerTypes = null;
		}
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
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
	public IClass resolveClass(Name name)
	{
		if (!this.typesResolved)
		{
			this.resolveTypes(null, Package.rootPackage);
		}
		
		for (int i = 0; i < this.genericCount; i++)
		{
			ITypeVariable var = this.generics[i];
			if (var.getName() == name)
			{
				return var.getCaptureClass();
			}
		}
		
		IClass clazz = this.body.getClass(name);
		if (clazz != null)
		{
			return clazz;
		}
		
		return null;
	}
	
	@Override
	public FieldMatch resolveField(Name name)
	{
		if (!this.typesResolved)
		{
			this.resolveTypes(null, Package.rootPackage);
		}
		
		// Own properties
		IField field = this.body.getProperty(name);
		if (field != null)
		{
			return new FieldMatch(field, 1);
		}
		
		// Own fields
		field = this.body.getField(name);
		if (field != null)
		{
			return new FieldMatch(field, 1);
		}
		
		if (this.instanceField != null && "instance".equals(name))
		{
			return new FieldMatch(this.instanceField, 1);
		}
		
		FieldMatch match;
		
		// Inherited Fields
		if (this.superType != null)
		{
			match = this.superType.resolveField(name);
			if (match != null)
			{
				return match;
			}
		}
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		if (!this.typesResolved)
		{
			this.resolveTypes(null, Package.rootPackage);
		}
		
		this.body.getMethodMatches(list, instance, name, arguments);
		
		if (!list.isEmpty())
		{
			return;
		}
		
		if (this.superType != null)
		{
			this.superType.getMethodMatches(list, instance, name, arguments);
		}
		for (int i = 0; i < this.interfaceCount; i++)
		{
			this.interfaces[i].getMethodMatches(list, instance, name, arguments);
		}
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		if (!this.typesResolved)
		{
			this.resolveTypes(null, Package.rootPackage);
		}
		
		this.body.getConstructorMatches(list, arguments);
	}
	
	public boolean addSpecialMethod(String specialType, String name, IMethod method)
	{
		if ("get".equals(specialType) || "set".equals(specialType))
		{
			Name name1 = Name.getQualified(name);
			IProperty property = this.body.getProperty(name1);
			if (property == null)
			{
				Property prop = new Property(this, name1, method.getType());
				prop.modifiers = method.getModifiers() & ~Modifiers.SYNTHETIC;
				this.body.addProperty(prop);
			}
		}
		if ("parDefault".equals(specialType))
		{
			int i = name.indexOf('$');
			Name name1 = Name.getQualified(name.substring(0, i));
			IMethod method1 = this.body.getMethod(name1);
			int parIndex = Integer.parseInt(name.substring(i + 1));
			
			MethodCall call = new MethodCall(null);
			call.method = method;
			call.name = name1;
			method1.getParameter(parIndex).defaultValue = call;
			return false;
		}
		return true;
	}
	
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
	{
		this.modifiers = access;
		this.internalName = name;
		
		this.body = new ClassBody(this);
		if (interfaces != null)
		{
			this.interfaces = new IType[interfaces.length];
		}
		
		int index = name.lastIndexOf('$');
		if (index == -1)
		{
			index = name.lastIndexOf('/');
		}
		if (index == -1)
		{
			this.name = Name.getQualified(name);
			this.thePackage = Package.rootPackage;
			this.fullName = name;
		}
		else
		{
			this.name = Name.getQualified(name.substring(index + 1));
			this.fullName = name.replace('/', '.');
			this.thePackage = Package.rootPackage.resolvePackage(Name.getQualified(this.fullName.substring(0, index)));
		}
		
		if (signature != null)
		{
			this.generics = new ITypeVariable[2];
			ClassFormat.readClassSignature(signature, this);
		}
		else
		{
			if (superName != null)
			{
				this.superType = ClassFormat.internalToType(superName);
			}
			else
			{
				this.superType = null;
			}
			
			if (interfaces != null)
			{
				this.interfaceCount = interfaces.length;
				this.interfaces = new IType[this.interfaceCount];
				for (int i = 0; i < this.interfaceCount; i++)
				{
					this.interfaces[i] = ClassFormat.internalToType(interfaces[i]);
				}
			}
		}
		
		this.type = new dyvil.tools.compiler.ast.type.Type(this);
	}
	
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
	{
		Field field = new Field(this);
		field.setName(Name.get(name));
		field.setModifiers(access);
		field.setType(ClassFormat.internalToType(desc));
		
		if (value != null)
		{
			field.setValue(IValue.fromObject(value));
		}
		
		if ((this.modifiers & Modifiers.OBJECT_CLASS) == 0 || (access & Modifiers.SYNTHETIC) == 0)
		{
			this.body.addField(field);
		}
		else
		{
			// This is the instance field of a singleton object class, ignore
			// annotations as it shouldn't have any
			this.instanceField = field;
			return null;
		}
		
		return new SimpleFieldVisitor(field);
	}
	
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
	{
		Name name1 = Name.get(name);
		
		if ((this.modifiers & Modifiers.ANNOTATION) != 0)
		{
			Parameter param = new Parameter();
			param.modifiers = access;
			param.name = name1;
			param.type = ClassFormat.internalToType(desc.substring(desc.lastIndexOf(')') + 1));
			this.addParameter(param);
			return new AnnotationClassVisitor(param);
		}
		
		if ("<init>".equals(name))
		{
			Constructor constructor = new Constructor(this);
			constructor.setModifiers(access);
			
			ClassFormat.readConstructorType(desc, constructor);
			
			if ((access & Modifiers.VARARGS) != 0)
			{
				Parameter param = constructor.getParameter(constructor.parameterCount() - 1);
				param.setVarargs2();
			}
			
			this.body.addConstructor(constructor);
			
			return new SimpleMethodVisitor(constructor);
		}
		
		Method method = new Method(this);
		method.name = name1;
		method.modifiers = access;
		
		if (signature != null)
		{
			method.setGeneric();
			ClassFormat.readMethodType(signature, method);
		}
		else
		{
			ClassFormat.readMethodType(desc, method);
		}
		
		if ((access & Modifiers.VARARGS) != 0)
		{
			Parameter param = method.getParameter(method.parameterCount() - 1);
			param.setVarargs2();
		}
		
		boolean flag = true;
		if ((access & Modifiers.SYNTHETIC) != 0)
		{
			int index = name.indexOf('$');
			if (index != -1)
			{
				flag = this.addSpecialMethod(name.substring(0, index), name.substring(index + 1), method);
			}
		}
		
		if (flag)
		{
			this.body.addMethod(method);
		}
		
		return new SimpleMethodVisitor(method);
	}
	
	public void visitOuterClass(String owner, String name, String desc)
	{
		this.outerType = ClassFormat.internalToType(owner);
	}
	
	public void visitInnerClass(String name, String outerName, String innerName, int access)
	{
		if (this.innerTypes == null)
		{
			this.innerTypes = new ArrayList(1);
		}
		
		IType type = ClassFormat.internalToType(name);
		this.innerTypes.add(type);
	}
}
