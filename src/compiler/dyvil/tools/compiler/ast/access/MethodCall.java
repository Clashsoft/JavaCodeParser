package dyvil.tools.compiler.ast.access;

import java.util.ArrayList;
import java.util.List;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValueList;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.transform.AccessResolver;
import dyvil.tools.compiler.transform.ConstantFolder;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.Operators;
import dyvil.tools.compiler.util.Util;

public class MethodCall extends ASTNode implements IAccess, INamed, IValue, IValueList, IValued
{
	public String		name;
	public String		qualifiedName;
	
	public IValue		instance;
	public List<IValue>	arguments	= new ArrayList(3);
	
	public boolean		dotless;
	public boolean		isSugarCall;
	
	public IMethod		method;
	
	public MethodCall(ICodePosition position)
	{
		this.position = position;
	}
	
	public MethodCall(ICodePosition position, IValue instance, String name)
	{
		this.position = position;
		this.instance = instance;
		this.name = name;
		this.qualifiedName = Symbols.qualify(name);
	}
	
	@Override
	public IType getType()
	{
		if (this.method == null)
		{
			return null;
		}
		return this.method.getType();
	}
	
	@Override
	public int getValueType()
	{
		return METHOD_CALL;
	}
	
	@Override
	public void setName(String name, String qualifiedName)
	{
		this.name = name;
		this.qualifiedName = qualifiedName;
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
	
	@Override
	public void setValues(List<IValue> list)
	{
		this.arguments = list;
	}
	
	@Override
	public void setValue(int index, IValue value)
	{
		this.arguments.set(index, value);
	}
	
	@Override
	public void addValue(IValue value)
	{
		this.arguments.add(value);
	}
	
	@Override
	public List<IValue> getValues()
	{
		return this.arguments;
	}
	
	@Override
	public IValue getValue(int index)
	{
		return this.arguments.get(index);
	}
	
	public void setSugar(boolean sugar)
	{
		this.isSugarCall = sugar;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.resolveTypes(markers, context);
		}
		
		for (IValue v : this.arguments)
		{
			v.resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		return AccessResolver.resolve(markers, context, this);
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.check(markers, context);
		}
		
		for (IValue v : this.arguments)
		{
			v.check(markers, context);
		}
		
		if (this.method != null)
		{
			this.method.checkArguments(markers, this.instance, this.arguments);
			
			byte access = context.getAccessibility(this.method);
			if (access == IContext.STATIC)
			{
				markers.add(new SemanticError(this.position, "The instance method '" + this.name + "' cannot be invoked from a static context"));
			}
			else if (access == IContext.SEALED)
			{
				markers.add(new SemanticError(this.position, "The sealed method '" + this.name + "' cannot be invoked because it is private to it's library"));
			}
			else if ((access & IContext.READ_ACCESS) == 0)
			{
				markers.add(new SemanticError(this.position, "The method '" + this.name + "' cannot be invoked since it is not visible"));
			}
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		int len = this.arguments.size();
		if (this.instance != null)
		{
			if (this.instance.isConstant())
			{
				if (len == 0)
				{
					IValue v1 = ConstantFolder.apply(this.instance, this.qualifiedName);
					return v1 == null ? this : v1;
				}
				else if (len == 1)
				{
					IValue argument = this.arguments.get(0);
					if (argument.isConstant())
					{
						IValue v1 = ConstantFolder.apply(this.instance, this.qualifiedName, argument);
						return v1 == null ? this : v1;
					}
				}
			}
			this.instance = this.instance.foldConstants();
		}
		
		for (int i = 0; i < len; i++)
		{
			IValue v1 = this.arguments.get(i);
			IValue v2 = v1.foldConstants();
			if (v1 != v2)
			{
				this.arguments.set(i, v2);
			}
		}
		
		return this;
	}
	
	private IValue	replacement;
	
	@Override
	public boolean resolve(IContext context, List<Marker> markers)
	{
		int len = this.arguments.size();
		if (len == 0 && this.instance != null)
		{
			IValue operator = Operators.get(this.instance, this.name);
			if (operator != null)
			{
				operator.setPosition(this.position);
				this.replacement = operator;
				// Return false to apply replacement in resolve2
				return false;
			}
		}
		else if (len == 1)
		{
			IValue argument = this.arguments.get(0);
			argument = argument.resolve(markers, context);
			
			IValue operator = this.instance == null ? Operators.get(this.name, argument) : Operators.get(this.instance, this.name, argument);
			if (operator != null)
			{
				operator.setPosition(this.position);
				this.replacement = operator;
				// Return false to apply replacement in resolve2
				return false;
			}
			
			this.arguments.set(0, argument);
		}
		else
		{
			for (int i = 0; i < len; i++)
			{
				IValue v1 = this.arguments.get(i);
				IValue v2 = v1.resolve(markers, context);
				if (v1 != v2)
				{
					this.arguments.set(i, v2);
				}
			}
		}
		
		IMethod method = IAccess.resolveMethod(context, this.instance, this.qualifiedName, this.arguments);
		if (method != null)
		{
			this.method = method;
			return true;
		}
		return false;
	}
	
	@Override
	public IValue resolve2(IContext context)
	{
		if (this.replacement != null)
		{
			return this.replacement;
		}
		
		if (this.isSugarCall)
		{
			if (this.arguments.isEmpty())
			{
				IField field = IAccess.resolveField(context, this.instance, this.qualifiedName);
				if (field != null)
				{
					FieldAccess access = new FieldAccess(this.position);
					access.field = field;
					access.instance = this.instance;
					access.name = this.name;
					access.qualifiedName = this.qualifiedName;
					access.dotless = this.dotless;
					return access;
				}
			}
		}
		// Resolve Apply Method
		else if (this.instance == null)
		{
			FieldMatch field = context.resolveField(this.qualifiedName);
			if (field != null)
			{
				FieldAccess access = new FieldAccess(this.position);
				access.field = field.theField;
				access.name = this.name;
				access.qualifiedName = this.qualifiedName;
				access.dotless = this.dotless;
				
				MethodCall call = new MethodCall(this.position, access, "apply");
				call.arguments = this.arguments;
				if (call.resolve(field.theField.getType(), null))
				{
					return call;
				}
			}
		}
		
		return null;
	}
	
	@Override
	public IAccess resolve3(IContext context, IAccess next)
	{
		return null;
	}
	
	@Override
	public Marker getResolveError()
	{
		SemanticError error;
		if (this.arguments.isEmpty())
		{
			error = new SemanticError(this.position, "'" + this.name + "' could not be resolved to a method or field");
		}
		else
		{
			error = new SemanticError(this.position, "'" + this.name + "' could not be resolved to a method");
		}
		
		error.addInfo("Qualified Name: " + this.qualifiedName);
		if (this.instance != null)
		{
			IType vtype = this.instance.getType();
			error.addInfo("Instance Type: " + (vtype == null ? "unknown" : vtype));
		}
		StringBuilder builder = new StringBuilder("Argument Types: [");
		Util.typesToString(this.arguments, ", ", builder);
		error.addInfo(builder.append(']').toString());
		return error;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		// Writes the prefix opcodes if a @Bytecode annotation is present.
		this.method.writePrefixBytecode(writer);
		
		// Writes the instance (the first operand).
		if (this.instance != null)
		{
			this.instance.writeExpression(writer);
		}
		
		// Writes the infix opcodes if a @Bytecode annotation is present.
		this.method.writeInfixBytecode(writer);
		
		// Writes the arguments (the second operand).
		for (IValue arg : this.arguments)
		{
			arg.writeExpression(writer);
		}
		
		// Apply special compilation when dealing with boolean types
		if (this.method.getType() == Type.BOOLEAN)
		{
			Label ifEnd = new Label();
			ifEnd.info = MethodWriter.JUMP_INSTRUCTION_TARGET;
			
			// Condition
			if (this.method.writePostfixBytecode(writer, ifEnd))
			{
				Label elseEnd = new Label();
				elseEnd.info = MethodWriter.JUMP_INSTRUCTION_TARGET;
				
				// If Block
				writer.visitLdcInsn(1);
				writer.pop();
				writer.visitJumpInsn(Opcodes.GOTO, elseEnd);
				writer.visitLabel(ifEnd);
				// Else Block
				writer.visitLdcInsn(0);
				writer.visitLabel(elseEnd);
				return;
			}
		}
		// Writes the postfix opcodes if a @Bytecode annotation is present.
		if (this.method.writePostfixBytecode(writer))
		{
			return;
		}
		
		// If no @Bytecode annotation is present, write a normal invokation.
		this.writeInvoke(writer);
	}
	
	private void writeInvoke(MethodWriter writer)
	{
		IClass ownerClass = this.method.getTheClass();
		int opcode;
		int args = this.method.getParameters().size();
		if (this.method.hasModifier(Modifiers.STATIC))
		{
			opcode = Opcodes.INVOKESTATIC;
		}
		else if (ownerClass.hasModifier(Modifiers.INTERFACE_CLASS) && this.method.hasModifier(Modifiers.ABSTRACT))
		{
			opcode = Opcodes.INVOKEINTERFACE;
			args++;
		}
		else if (this.method.hasModifier(Modifiers.PRIVATE) || this.instance.getValueType() == IValue.SUPER)
		{
			opcode = Opcodes.INVOKESPECIAL;
			args++;
		}
		else
		{
			opcode = Opcodes.INVOKEVIRTUAL;
			args++;
		}
		
		String owner = ownerClass.getInternalName();
		String name = this.method.getQualifiedName();
		String desc = this.method.getDescriptor();
		IType type = this.method.getType();
		writer.visitMethodInsn(opcode, owner, name, desc, ownerClass.hasModifier(Modifiers.INTERFACE_CLASS), args, type);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.writeExpression(writer);
		
		if (!this.method.getType().classEquals(Type.VOID))
		{
			writer.visitInsn(Opcodes.POP);
		}
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest)
	{
		// Writes the prefix opcodes if a @Bytecode annotation is present.
		this.method.writePrefixBytecode(writer, dest);
		
		// Writes the instance (the first operand).
		if (this.instance != null)
		{
			this.instance.writeExpression(writer);
		}
		
		// Writes the infix opcodes if a @Bytecode annotation is present.
		this.method.writeInfixBytecode(writer, dest);
		
		// Writes the arguments (the second operand).
		for (IValue arg : this.arguments)
		{
			arg.writeExpression(writer);
		}
		
		// Writes the postfix opcodes if a @Bytecode annotation is present.
		if (this.method.writePostfixBytecode(writer, dest))
		{
			return;
		}
		
		// If no @Bytecode annotation is present, write a normal invokation.
		this.writeInvoke(writer);
		writer.visitJumpInsn(Opcodes.IFEQ, dest);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.instance != null)
		{
			this.instance.toString(prefix, buffer);
			if (this.dotless && !Formatting.Method.useJavaFormat)
			{
				buffer.append(Formatting.Method.dotlessSeperator);
			}
			else
			{
				buffer.append('.');
			}
		}
		
		if (Formatting.Method.convertQualifiedNames)
		{
			buffer.append(this.qualifiedName);
		}
		else
		{
			buffer.append(this.name);
		}
		
		if (this.isSugarCall && !Formatting.Method.useJavaFormat)
		{
			if (!this.arguments.isEmpty())
			{
				buffer.append(Formatting.Method.sugarCallSeperator);
				this.arguments.get(0).toString(prefix, buffer);
			}
		}
		else
		{
			Util.parametersToString(this.arguments, buffer, true);
		}
	}
}