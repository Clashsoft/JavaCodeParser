package dyvil.tools.compiler.ast.value;

import java.util.ArrayList;
import java.util.List;

import jdk.internal.org.objectweb.asm.Handle;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.access.ClassAccess;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.Parameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.transform.Symbols;

public final class FunctionValue extends ASTNode implements IValue, IValued, INamed
{
	public IValue				instance;
	
	public String				name;
	public String				qualifiedName;
	
	/**
	 * The type this function pointer represents
	 */
	private IType				type;
	
	/**
	 * The abstract method this lambda expression implements
	 */
	private IMethod				functionalMethod;
	
	/**
	 * The method of the instance this function pointer points at
	 */
	private IMethod				method;
	
	private List<MethodMatch>	methods;
	
	public FunctionValue(ICodePosition position, String name)
	{
		this.position = position;
		this.name = name;
		this.qualifiedName = Symbols.qualify(name);
	}
	
	// Names
	
	@Override
	public void setName(String name, String qualifiedName)
	{
		this.name = name;
		this.qualifiedName = name;
	}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public void setQualifiedName(String name)
	{
		this.qualifiedName = name;
	}
	
	@Override
	public String getQualifiedName()
	{
		return this.qualifiedName;
	}
	
	@Override
	public boolean isName(String name)
	{
		return this.qualifiedName.equals(name);
	}
	
	// Instance
	
	@Override
	public void setValue(IValue value)
	{
		this.instance = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.instance;
	}
	
	// IValue Overrides
	
	@Override
	public int getValueType()
	{
		return FUNCTION;
	}
	
	@Override
	public IType getType()
	{
		return this.type == null ? new Type("Unknown Function Pointer Type") : this.type;
	}
	
	@Override
	public IValue withType(IType type)
	{
		return this.isType(type) ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		IClass iclass = type.getTheClass();
		if (iclass == null)
		{
			return false;
		}
		IMethod method = iclass.getFunctionalMethod();
		if (method == null)
		{
			return false;
		}
		
		int parCount = method.parameterCount();
		boolean staticInstance = this.instance.getValueType() == CLASS_ACCESS;
		
		outer:
		for (MethodMatch match : this.methods)
		{
			if (staticInstance != match.theMethod.isStatic())
			{
				continue;
			}
			if (parCount != match.theMethod.parameterCount())
			{
				continue;
			}
			
			for (int i = 0; i < parCount; i++)
			{
				Parameter par1 = method.getParameter(i);
				Parameter par2 = match.theMethod.getParameter(i);
				
				if (!par1.type.equals(par2.type))
				{
					continue outer;
				}
			}
			
			this.type = type;
			this.functionalMethod = method;
			this.method = match.theMethod;
			return true;
		}
		
		return false;
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return this.isType(type) ? 3 : 0;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance = this.instance.resolve(markers, context);
		}
		else
		{
			if (context.isStatic())
			{
				this.instance = new ThisValue(context.getThisType());
			}
			else
			{
				this.instance = new ClassAccess(context.getThisType());
			}
		}
		
		List<MethodMatch> matches = new ArrayList();
		this.instance.getType().getMethodMatches(matches, this.instance, this.qualifiedName, null);
		this.methods = matches;
		
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.check(markers, context);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		this.instance = this.instance.foldConstants();
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		int len;
		int handleType;
		StringBuilder descBuf = new StringBuilder("(");
		if (this.instance != null && this.instance.getValueType() != CLASS_ACCESS)
		{
			handleType = Opcodes.H_INVOKEVIRTUAL;
			this.instance.writeExpression(writer);
			this.instance.getType().appendExtendedName(descBuf);
			len = 1;
		}
		else
		{
			handleType = Opcodes.H_INVOKESTATIC;
			len = 0;
		}
		
		descBuf.append(")");
		this.type.appendExtendedName(descBuf);
		
		String name = this.method.getQualifiedName();
		String desc = descBuf.toString();
		String methodDesc = this.method.getDescriptor();
		jdk.internal.org.objectweb.asm.Type type1 = jdk.internal.org.objectweb.asm.Type.getMethodType(this.functionalMethod.getDescriptor());
		jdk.internal.org.objectweb.asm.Type type2 = jdk.internal.org.objectweb.asm.Type.getMethodType(methodDesc);
		Handle handle = new Handle(handleType, this.method.getTheClass().getInternalName(), this.method.getQualifiedName(), methodDesc);
		writer.writeInvokeDynamic(name, desc, len, this.type, LambdaValue.BOOTSTRAP, type1, handle, type2);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.instance != null)
		{
			this.instance.toString(prefix, buffer);
		}
		buffer.append(Formatting.Method.pointerSeperator);
		if (Formatting.Method.convertQualifiedNames)
		{
			buffer.append(this.qualifiedName);
		}
		else
		{
			buffer.append(this.name);
		}
	}
}
