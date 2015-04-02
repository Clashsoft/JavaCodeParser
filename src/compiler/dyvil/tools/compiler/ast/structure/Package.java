package dyvil.tools.compiler.ast.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.imports.PackageDecl;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.CodePosition;
import dyvil.tools.compiler.library.Library;

public class Package implements INamed, IContext
{
	public static Package		rootPackage	= new RootPackage();
	
	public static Package		dyvil;
	public static Package		dyvilLang;
	public static Package		dyvilLangAnnotation;
	public static Package		dyvilLangFunction;
	public static Package		dyvilLangTuple;
	public static Package		dyvilLangLiteral;
	public static Package		java;
	public static Package		javaLang;
	public static Package		javaLangAnnotation;
	
	public Package				parent;
	public Name					name;
	public String				fullName;
	public String				internalName;
	
	public List<DyvilFile>		units		= new ArrayList();
	public List<IClass>			classes		= new ArrayList();
	public Map<String, Package>	subPackages	= new HashMap();
	
	protected Package()
	{
	}
	
	public Package(Package parent, Name name)
	{
		this.name = name;
		this.parent = parent;
		
		if (parent == null || parent.name == null)
		{
			this.fullName = name.qualified;
			this.internalName = ClassFormat.packageToInternal(name.qualified) + "/";
		}
		else
		{
			this.fullName = parent.fullName + "." + name.qualified;
			this.internalName = parent.internalName + name.qualified + "/";
		}
	}
	
	public static void init()
	{
		dyvil = Library.dyvilLibrary.resolvePackage("dyvil");
		dyvilLang = dyvil.resolvePackage("lang");
		dyvilLangAnnotation = dyvilLang.resolvePackage("annotation");
		dyvilLangFunction = dyvilLang.resolvePackage("function");
		dyvilLangTuple = dyvilLang.resolvePackage("tuple");
		dyvilLangLiteral = dyvilLang.resolvePackage("literal");
		java = Library.javaLibrary.resolvePackage("java");
		javaLang = java.resolvePackage("lang");
		javaLangAnnotation = javaLang.resolvePackage("annotation");
	}
	
	// Name
	
	@Override
	public void setName(Name name)
	{
		this.name = name;
	}
	
	@Override
	public Name getName()
	{
		return this.name;
	}
	
	// Units
	
	public void addCompilationUnit(DyvilFile unit)
	{
		this.units.add(unit);
	}
	
	public void addClass(IClass iclass)
	{
		this.classes.add(iclass);
	}
	
	public void addSubPackage(Package pack)
	{
		this.subPackages.put(pack.name.qualified, pack);
	}
	
	public Package createSubPackage(String name)
	{
		Package pack = this.subPackages.get(name);
		if (pack != null)
		{
			return pack;
		}
		
		pack = new Package(this, Name.getQualified(name));
		this.subPackages.put(name, pack);
		return pack;
	}
	
	public void check(PackageDecl packageDecl, CodeFile file, MarkerList markers)
	{
		if (packageDecl == null)
		{
			if (this.fullName != null)
			{
				markers.add(new CodePosition(0, 0, 1), "package.missing");
			}
			return;
		}
		
		if (this.fullName == null)
		{
			markers.add(packageDecl.getPosition(), "package.default");
			return;
		}
		
		if (!this.fullName.equals(packageDecl.thePackage))
		{
			markers.add(packageDecl.getPosition(), "package.invalid");
		}
	}
	
	@Override
	public boolean isStatic()
	{
		return true;
	}
	
	@Override
	public Type getThisType()
	{
		return null;
	}
	
	@Override
	public final Package resolvePackage(Name name)
	{
		return this.resolvePackage(name.qualified);
	}
	
	public Package resolvePackage(String name)
	{
		return this.subPackages.get(name);
	}
	
	@Override
	public final IClass resolveClass(Name name)
	{
		return this.resolveClass(name.qualified);
	}
	
	public IClass resolveClass(String name)
	{
		for (IClass c : this.classes)
		{
			if (c.getName().equals(name))
			{
				return c;
			}
		}
		
		return null;
	}
	
	@Override
	public ITypeVariable resolveTypeVariable(Name name)
	{
		return null;
	}
	
	@Override
	public IField resolveField(Name name)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toString()
	{
		return this.fullName;
	}
}
