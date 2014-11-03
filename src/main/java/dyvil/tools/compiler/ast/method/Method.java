package dyvil.tools.compiler.ast.method;

import java.util.ArrayList;
import java.util.List;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.Util;

public class Method extends Member implements IMethod
{
	private IValue				statement;
	
	private String				openBracket			= "(";
	private String				closeBracket		= ")";
	
	private List<Parameter>		parameters			= new ArrayList(3);
	private List<ThrowsDecl>	throwsDeclarations	= new ArrayList(1);
	
	public Method(IClass iclass)
	{
		super(iclass);
	}
	
	@Override
	public void setParameters(List<Parameter> parameters)
	{
		this.parameters = parameters;
	}
	
	@Override
	public List<Parameter> getParameters()
	{
		return this.parameters;
	}
	
	@Override
	public void addParameter(Parameter parameter)
	{
		this.parameters.add(parameter);
	}
	
	@Override
	public void setValue(IValue statement)
	{
		this.statement = statement;
	}
	
	@Override
	public void setThrows(List<ThrowsDecl> throwsDecls)
	{
		this.throwsDeclarations = throwsDecls;
	}
	
	@Override
	public List<ThrowsDecl> getThrows()
	{
		return this.throwsDeclarations;
	}
	
	@Override
	public void addThrows(ThrowsDecl throwsDecl)
	{
		this.throwsDeclarations.add(throwsDecl);
	}
	
	@Override
	public IValue getValue()
	{
		return this.statement;
	}
	
	@Override
	public String getDescriptor()
	{
		StringBuilder buf = new StringBuilder();
		buf.append('(');
		for (Parameter par : this.parameters)
		{
			buf.append(par.type.getInternalName());
		}
		buf.append(')');
		buf.append(this.type.getInternalName());
		return buf.toString();
	}
	
	@Override
	public String getSignature()
	{
		// TODO Generic Signature
		return null;
	}
	
	@Override
	public String[] getExceptions()
	{
		// TODO Exceptions
		return null;
	}
	
	@Override
	public void write(ClassWriter writer)
	{
		MethodVisitor visitor = writer.visitMethod(this.modifiers, this.qualifiedName, this.getDescriptor(), this.getSignature(), this.getExceptions());
		
		int index = 0;
		for (Parameter param : this.parameters)
		{
			visitor.visitParameter(param.name, index++);
		}
		
		if (this.statement != null)
		{
			this.statement.write(visitor);
		}
		
		// TODO Actual values -.-
		visitor.visitInsn(Opcodes.RETURN);
		visitor.visitMaxs(10, index + 1);
	}
	
	@Override
	public boolean isStatic()
	{
		return this.hasModifier(Modifiers.STATIC);
	}
	
	@Override
	public Type getThisType()
	{
		return this.theClass.getThisType();
	}
	
	@Override
	public void setParametersOpenBracket(String bracket)
	{
		this.openBracket = bracket;
	}
	
	@Override
	public void setParametersCloseBracket(String bracket)
	{
		this.closeBracket = bracket;
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		return this.theClass.resolveClass(name);
	}
	
	@Override
	public IField resolveField(String name)
	{
		for (Parameter param : this.parameters)
		{
			if (param.name.equals(name))
			{
				return param;
			}
		}
		
		return this.theClass.resolveField(name);
	}
	
	@Override
	public IMethod resolveMethodName(String name)
	{
		return this.theClass.resolveMethodName(name);
	}
	
	@Override
	public IMethod resolveMethod(String name, Type... args)
	{
		return this.theClass.resolveMethod(name, args);
	}
	
	@Override
	public Method applyState(CompilerState state, IContext context)
	{
		this.type = this.type.applyState(state, context);
		
		this.parameters.replaceAll(p -> p.applyState(state, context));
		
		if (this.statement != null)
		{
			this.statement = this.statement.applyState(state, this);
		}
		
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		super.toString(prefix, buffer);
		
		buffer.append(Modifiers.METHOD.toString(this.modifiers));
		if (this.type != null)
		{
			this.type.toString("", buffer);
			buffer.append(' ');
		}
		
		if (Formatting.Method.convertQualifiedNames)
		{
			buffer.append(this.qualifiedName);
		}
		else
		{
			buffer.append(this.name);
		}
		
		Util.parametersToString(this.parameters, buffer, true, this.openBracket + this.closeBracket, this.openBracket, Formatting.Method.parameterSeperator, this.closeBracket);
		
		IValue statement = this.getValue();
		if (statement != null)
		{
			buffer.append(Formatting.Method.signatureBodySeperator);
			statement.toString(prefix, buffer);
		}
		buffer.append(';');
	}
}
