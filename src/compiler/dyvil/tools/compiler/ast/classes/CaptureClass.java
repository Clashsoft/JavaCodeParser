package dyvil.tools.compiler.ast.classes;

import java.util.List;

import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.type.IType;

public class CaptureClass extends CodeClass
{
	public static int		captureID;
	
	public ITypeVariable	typeVariable;
	
	public CaptureClass(ITypeVariable typeVar, IType superType, List<IType> interfaces)
	{
		String name = "Capture$" + captureID++;
		this.typeVariable = typeVar;
		this.name = name;
		this.qualifiedName = name;
		this.internalName = superType.getInternalName();
		this.superType = superType;
		
		if (interfaces != null)
		{
			this.interfaces = interfaces;
		}
	}
	
	@Override
	public String getSignature()
	{
		return this.typeVariable.getSignature();
	}
	
	@Override
	public boolean equals(IClass iclass)
	{
		if (!iclass.isSubTypeOf(this.superType))
		{
			return false;
		}
		
		for (IType i : this.interfaces)
		{
			if (!iclass.isSubTypeOf(i))
			{
				return false;
			}
		}
		return true;
	}
}
