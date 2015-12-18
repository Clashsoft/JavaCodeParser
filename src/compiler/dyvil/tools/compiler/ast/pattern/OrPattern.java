package dyvil.tools.compiler.ast.pattern;

import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class OrPattern implements IPattern
{
	protected IPattern      left;
	protected IPattern      right;
	protected ICodePosition position;

	public OrPattern(IPattern left, ICodePosition token, IPattern right) {

		this.left = left;
		this.position = token;
		this.right = right;
	}

	@Override
	public int getPatternType()
	{
		return OR;
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
	public boolean isExhaustive()
	{
		return this.left.isExhaustive() | this.right.isExhaustive();
	}

	@Override
	public IType getType()
	{
		return Types.findCommonSuperType(this.left.getType(), this.right.getType());
	}

	@Override
	public IPattern withType(IType type, MarkerList markers)
	{
		this.left = this.left.withType(type, markers);
		this.right = this.right.withType(type, markers);
		return this;
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

	@Override
	public boolean isSwitchable()
	{
		return this.left.isSwitchable() && this.right.isSwitchable();
	}

	@Override
	public int switchCases()
	{
		return this.left.switchCases() + this.right.switchCases();
	}

	@Override
	public boolean switchCheck()
	{
		return this.left.switchCheck() || this.right.switchCheck();
	}

	@Override
	public int switchValue(int index)
	{
		final int leftCount = this.left.switchCases();
		if (index < leftCount) {
			return this.left.switchValue(index);
		}
		return this.right.switchValue(index - leftCount);
	}

	@Override
	public int minValue()
	{
		return Math.min(this.left.minValue(), this.left.minValue());
	}

	@Override
	public int maxValue()
	{
		return Math.max(this.left.maxValue(), this.right.maxValue());
	}

	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		final Label rightLabel = new Label();
		this.left.writeInvJump(writer, varIndex, rightLabel);
		writer.writeLabel(rightLabel);
		this.right.writeInvJump(writer, varIndex, elseLabel);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.left.toString(prefix, buffer);
		buffer.append(" | ");
		this.right.toString(prefix, buffer);
	}
}
