package dyvil.tools.compiler.ast.access;

import java.util.List;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.expression.ValueList;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Modifiers;

public class SpecialConstructor extends ASTNode implements IValue, IValued
{
	public IType		type;
	
	public ValueList	list;
	
	public boolean		isCustom;
	
	public IMethod		method;
	
	public SpecialConstructor(ICodePosition position)
	{
		this.position = position;
	}
	
	public SpecialConstructor(ICodePosition position, ConstructorCall cc)
	{
		this.position = cc.getPosition();
		this.type = cc.type;
		this.isCustom = cc.isCustom;
		this.list = new StatementList(position);
	}
	
	@Override
	public void expandPosition(ICodePosition position)
	{
		this.list.expandPosition(position);
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public int getValueType()
	{
		return CONSTRUCTOR_CALL;
	}
	
	@Override
	public void setValue(IValue value)
	{
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		this.type = this.type.resolve(context);
		if (!this.type.isResolved())
		{
			markers.add(new SemanticError(this.type.getPosition(), "'" + this.type + "' could not be resolved to a type"));
			return;
		}
		
		this.list.resolveTypes(markers, this.type);
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		MethodMatch method = context.resolveMethod(null, "<init>", Type.EMPTY_TYPES);
		if (method != null)
		{
			this.method = method.theMethod;
			this.list.resolve(markers, this.method);
			return this;
		}
		
		markers.add(new SemanticError(this.position, "The constructor could not be resolved"));
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		IClass iclass = this.type.getTheClass();
		if (iclass.hasModifier(Modifiers.INTERFACE_CLASS))
		{
			markers.add(new SemanticError(this.position, "The interface '" + iclass.getName() + "' cannot be instantiated"));
		}
		else if (iclass.hasModifier(Modifiers.ABSTRACT))
		{
			markers.add(new SemanticError(this.position, "The abstract class '" + iclass.getName() + "' cannot be instantiated"));
		}
		else if (this.method != null)
		{
			byte access = context.getAccessibility(this.method);
			if (access == IContext.SEALED)
			{
				markers.add(new SemanticError(this.position, "The sealed constructor cannot be invoked because it is private to it's library"));
			}
			else if ((access & IContext.READ_ACCESS) == 0)
			{
				markers.add(new SemanticError(this.position, "The constructor cannot be invoked because it is not visible"));
			}
			
			this.list.check(markers, this.method);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		this.list.foldConstants();
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		int opcode;
		int args = 0;
		if (this.isCustom)
		{
			opcode = Opcodes.INVOKESTATIC;
		}
		else
		{
			opcode = Opcodes.INVOKESPECIAL;
			args = 1;
			
			writer.visitTypeInsn(Opcodes.NEW, this.type);
			writer.visitInsn(Opcodes.DUP, this.type);
		}
		
		String owner = this.method.getTheClass().getInternalName();
		String name = "<init>";
		String desc = "()V";
		writer.visitMethodInsn(opcode, owner, name, desc, false, args, null);
		
		this.list.writeExpression(writer);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		int opcode;
		int args = 0;
		if (this.isCustom)
		{
			opcode = Opcodes.INVOKESTATIC;
		}
		else
		{
			opcode = Opcodes.INVOKESPECIAL;
			args = 1;
			
			writer.visitTypeInsn(Opcodes.NEW, this.type);
		}
		
		String owner = this.method.getTheClass().getInternalName();
		String name = "<init>";
		String desc = "()V";
		writer.visitMethodInsn(opcode, owner, name, desc, false, args, null);
		
		this.list.writeExpression(writer);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("new ");
		this.type.toString(prefix, buffer);
		buffer.append(' ');
		this.list.toString(prefix, buffer);
	}
}