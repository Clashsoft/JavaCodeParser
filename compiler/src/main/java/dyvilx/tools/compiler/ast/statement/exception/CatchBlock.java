package dyvilx.tools.compiler.ast.statement.exception;

import dyvil.lang.Name;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IDefaultContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.phase.Resolvable;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

public class CatchBlock implements Resolvable, IDefaultContext
{
	public IValue action;

	protected IVariable variable;

	public CatchBlock()
	{
	}

	public IValue getAction()
	{
		return this.action;
	}

	public void setAction(IValue value)
	{
		this.action = value;
	}

	public IVariable getVariable()
	{
		return this.variable;
	}

	public void setVariable(IVariable variable)
	{
		this.variable = variable;
	}

	public IType getType()
	{
		return this.variable.getType();
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		if (this.variable.getName() == name)
		{
			return this.variable;
		}

		return null;
	}

	@Override
	public boolean isMember(IVariable variable)
	{
		return variable == this.variable;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.variable.resolveTypes(markers, context);
		this.action.resolveTypes(markers, context);
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		this.variable.resolve(markers, context);

		context = context.push(this);

		this.action = this.action.resolve(markers, context);

		context.pop();
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.variable.checkTypes(markers, context);

		context = context.push(this);
		this.action.checkTypes(markers, context);
		context.pop();
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.variable.check(markers, context);

		final IType type = this.variable.getType();
		if (!Types.isSuperType(Types.THROWABLE, type))
		{
			final Marker marker = Markers.semanticError(this.variable.getPosition(), "try.catch.type.not_throwable");
			marker.addInfo(Markers.getSemantic("exception.type", type));
			markers.add(marker);
		}

		context = context.push(this);
		this.action.check(markers, context);
		context.pop();
	}

	@Override
	public void foldConstants()
	{
		this.variable.foldConstants();
		this.action = this.action.foldConstants();
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.variable.cleanup(compilableList, classCompilableList);
		this.action = this.action.cleanup(compilableList, classCompilableList);
	}
}
