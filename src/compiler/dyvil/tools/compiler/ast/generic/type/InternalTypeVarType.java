package dyvil.tools.compiler.ast.generic.type;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.InternalType;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class InternalTypeVarType extends InternalType
{
	public InternalTypeVarType(String internalName)
	{
		super(internalName);
	}
	
	@Override
	public int typeTag()
	{
		return INTERNAL_TYPE_VAR;
	}
	
	@Override
	public Name getName()
	{
		return Name.getQualified(this.internalName);
	}
	
	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		ITypeVariable typeVar = context.resolveTypeVariable(Name.getQualified(this.internalName));
		if (typeVar == null)
		{
			return this;
		}
		
		return new TypeVarType(typeVar);
	}
	
	@Override
	public String getInternalName()
	{
		return "java/lang/Object";
	}
	
	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		buffer.append("Ljava/lang/Object;");
	}
	
	@Override
	public void appendSignature(StringBuilder buffer)
	{
		buffer.append('T').append(this.internalName).append(';');
	}
}
