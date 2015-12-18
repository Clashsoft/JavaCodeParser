package dyvil.tools.compiler.ast.pattern.operator;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.pattern.IPattern;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public abstract class BinaryPattern implements IPattern
{
	protected IPattern      left;
	protected IPattern      right;
	protected ICodePosition position;
	// Metadata
	private   IType         commonType;

	public BinaryPattern(IPattern left, ICodePosition token, IPattern right)
	{
		this.right = right;
		this.left = left;
		this.position = token;
	}

	public IPattern getLeft()
	{
		return this.left;
	}

	public void setLeft(IPattern left)
	{
		this.left = left;
	}

	public IPattern getRight()
	{
		return this.right;
	}

	public void setRight(IPattern right)
	{
		this.right = right;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}

	@Override
	public IType getType()
	{
		if (this.commonType == null)
		{
			return this.commonType = Types.findCommonSuperType(this.left.getType(), this.right.getType());
		}
		return this.commonType;
	}

	@Override
	public boolean isType(IType type)
	{
		return this.left.isType(type) && this.right.isType(type);
	}

	@Override
	public IPattern resolve(MarkerList markers, IContext context)
	{
		this.left = this.left.resolve(markers, context);
		this.right = this.right.resolve(markers, context);
		return this;
	}

	public int checkVar(MethodWriter writer, int varIndex) throws BytecodeException
	{
		if (varIndex < 0)
		{
			varIndex = writer.localCount();
			writer.writeVarInsn(this.getType().getStoreOpcode(), varIndex);
		}
		return varIndex;
	}
}