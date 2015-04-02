package dyvil.tools.compiler.ast.statement;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class SyncStatement extends ASTNode implements IValue
{
	public IValue	lock;
	public IValue	block;
	
	public SyncStatement(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int getValueType()
	{
		return SYNCHRONIZED;
	}
	
	@Override
	public IType getType()
	{
		return this.block.getType();
	}
	
	@Override
	public IValue withType(IType type)
	{
		this.block = this.block.withType(type);
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return this.block.isType(type);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return this.block.getTypeMatch(type);
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.lock.resolveTypes(markers, context);
		this.block.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.lock.resolve(markers, context);
		this.block.resolve(markers, context);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.lock.checkTypes(markers, context);
		this.block.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.lock.check(markers, context);
		this.block.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		this.write(writer, true);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.write(writer, false);
	}
	
	private void write(MethodWriter writer, boolean expression)
	{
		int localCount = writer.registerLocal();
		this.lock.writeExpression(writer);
		writer.writeInsn(Opcodes.DUP);
		writer.writeVarInsn(Opcodes.ASTORE, localCount);
		writer.writeInsn(Opcodes.MONITORENTER);
		
		org.objectweb.asm.Label start = new org.objectweb.asm.Label();
		org.objectweb.asm.Label end = new org.objectweb.asm.Label();
		org.objectweb.asm.Label handlerStart = new org.objectweb.asm.Label();
		org.objectweb.asm.Label throwLabel = new org.objectweb.asm.Label();
		org.objectweb.asm.Label handlerEnd = new org.objectweb.asm.Label();
		
		writer.writeLabel(start);
		if (expression)
		{
			this.block.writeExpression(writer);
		}
		else
		{
			this.block.writeStatement(writer);
		}
		writer.writeVarInsn(Opcodes.ALOAD, localCount);
		writer.writeInsn(Opcodes.MONITOREXIT);
		writer.writeLabel(end);
		
		writer.writeJumpInsn(Opcodes.GOTO, handlerEnd);
		
		writer.writeLabel(handlerStart);
		writer.writeVarInsn(Opcodes.ALOAD, localCount);
		writer.writeInsn(Opcodes.MONITOREXIT);
		writer.writeLabel(throwLabel);
		writer.writeInsn(Opcodes.ATHROW);
		if (expression)
		{
			this.block.getType().writeDefaultValue(writer);
		}
		
		writer.resetLocals(localCount);
		writer.writeLabel(handlerEnd);
		
		writer.writeFinallyBlock(start, end, handlerStart);
		writer.writeFinallyBlock(handlerStart, throwLabel, handlerStart);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Statements.syncStart);
		if (this.lock != null)
		{
			this.lock.toString(prefix, buffer);
		}
		buffer.append(Formatting.Statements.syncEnd);
		this.block.toString(prefix, buffer);
	}
}