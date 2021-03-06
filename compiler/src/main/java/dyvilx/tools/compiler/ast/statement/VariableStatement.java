package dyvilx.tools.compiler.ast.statement;

import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public class VariableStatement implements IStatement
{
	protected IVariable variable;

	public VariableStatement(IVariable variable)
	{
		this.variable = variable;
	}

	public IVariable getVariable()
	{
		return this.variable;
	}

	public void setVariable(IVariable variable)
	{
		this.variable = variable;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.variable.getPosition();
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.variable.setPosition(position);
	}

	@Override
	public int valueTag()
	{
		return VARIABLE;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.variable.resolveTypes(markers, context);
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.variable.resolve(markers, context);
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.variable.checkTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.variable.check(markers, context);
	}

	@Override
	public IValue foldConstants()
	{
		this.variable.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.variable.cleanup(compilableList, classCompilableList);
		return this;
	}

	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.variable.writeInit(writer);
	}

	@Override
	public String toString()
	{
		return this.variable.toString();
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.variable.toString(prefix, buffer);
	}
}
