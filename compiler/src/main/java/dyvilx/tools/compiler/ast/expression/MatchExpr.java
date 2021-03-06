package dyvilx.tools.compiler.ast.expression;

import dyvil.annotation.internal.NonNull;
import dyvil.collection.iterator.ArrayIterator;
import dyvil.lang.Formattable;
import dyvil.math.MathUtils;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.context.ILabelContext;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.pattern.Pattern;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.transform.TypeChecker;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

import java.util.*;

public class MatchExpr implements IValue
{
	// =============== Constants ===============

	public static final TypeChecker.MarkerSupplier MARKER_SUPPLIER = TypeChecker.markerSupplier(
		"match.value.type.incompatible");

	// =============== Fields ===============

	protected IValue      matchedValue;
	protected MatchCase[] cases = new MatchCase[3];
	protected int         caseCount;

	// --------------- Metadata ---------------

	protected SourcePosition position;
	private   IType          returnType;

	// =============== Constructors ===============

	public MatchExpr(SourcePosition position)
	{
		this.position = position;
	}

	public MatchExpr(SourcePosition position, IValue matchedValue)
	{
		this.position = position;
		this.matchedValue = matchedValue;
	}

	public MatchExpr(IValue matchedValue, MatchCase[] cases)
	{
		this.matchedValue = matchedValue;
		this.cases = cases;
		this.caseCount = cases.length;
	}

	// =============== Properties ===============

	public IValue getMatchedValue()
	{
		return this.matchedValue;
	}

	public void setMatchedValue(IValue value)
	{
		this.matchedValue = value;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}

	// =============== Methods ===============

	// --------------- Match Cases ---------------

	public Iterable<MatchCase> cases()
	{
		return () -> new ArrayIterator<>(this.cases, 0, this.caseCount);
	}

	public MatchCase getCase(int index)
	{
		return index >= 0 && index < this.caseCount ? this.cases[index] : null;
	}

	public void setCase(int index, MatchCase matchCase)
	{
		if (index >= 0 && index < this.caseCount)
		{
			this.cases[index] = matchCase;
		}
	}

	public void addCase(MatchCase matchCase)
	{
		int index = this.caseCount++;
		if (index >= this.cases.length)
		{
			MatchCase[] temp = new MatchCase[this.caseCount];
			System.arraycopy(this.cases, 0, temp, 0, index);
			this.cases = temp;
		}
		this.cases[index] = matchCase;
	}

	// --------------- General Expression Info ---------------

	@Override
	public int valueTag()
	{
		return MATCH;
	}

	@Override
	public boolean isUsableAsStatement()
	{
		for (int i = 0; i < this.caseCount; i++)
		{
			final IValue action = this.cases[i].action;
			if (action != null && !action.isUsableAsStatement())
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean isResolved()
	{
		for (int i = 0; i < this.caseCount; i++)
		{
			if (!this.cases[i].action.isResolved())
			{
				return false;
			}
		}

		return true;
	}

	// --------------- Types ---------------

	@Override
	public IType getType()
	{
		if (this.returnType != null)
		{
			return this.returnType;
		}

		final int cases = this.caseCount;
		if (cases == 0)
		{
			return this.returnType = Types.VOID;
		}

		IType result = null;
		for (int i = 0; i < cases; i++)
		{
			final IValue action = this.cases[i].action;
			if (action == null)
			{
				continue;
			}

			final IType actionType = action.getType();
			result = result != null ? Types.combine(result, actionType) : actionType;
		}

		return this.returnType = (result == null ? Types.VOID : result);
	}

	@Override
	public boolean isType(IType type)
	{
		if (Types.isVoid(type))
		{
			return true;
		}

		for (int i = 0; i < this.caseCount; i++)
		{
			final IValue action = this.cases[i].action;
			if (action != null && !action.isType(type))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		if (this.caseCount == 0)
		{
			return MISMATCH;
		}

		int min = Integer.MAX_VALUE;
		for (int i = 0; i < this.caseCount; i++)
		{
			final IValue action = this.cases[i].action;
			if (action == null)
			{
				continue;
			}

			final int match = TypeChecker.getTypeMatch(action, type, implicitContext);
			if (match == MISMATCH)
			{
				return MISMATCH;
			}
			if (match < min)
			{
				min = match;
			}
		}

		// min might be unchanged in case all actions were null
		return min == Integer.MAX_VALUE ? MISMATCH : min;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.caseCount; i++)
		{
			final MatchCase matchCase = this.cases[i];
			final IValue action = matchCase.action;

			if (action == null)
			{
				continue;
			}

			matchCase.action = TypeChecker.convertValue(action, type, typeContext, markers, context, MARKER_SUPPLIER);
		}

		this.returnType = type;
		return this;
	}

	// --------------- Resolution Phases ---------------

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.matchedValue.resolveTypes(markers, context);
		for (int i = 0; i < this.caseCount; i++)
		{
			this.cases[i].resolveTypes(markers, context);
		}
	}

	@Override
	public void resolveStatement(ILabelContext context, MarkerList markers)
	{
		for (int i = 0; i < this.caseCount; i++)
		{
			final IValue action = this.cases[i].action;
			if (action != null)
			{
				action.resolveStatement(context, markers);
			}
		}
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.matchedValue = this.matchedValue.resolve(markers, context);

		final IType type = this.matchedValue.getType();
		this.matchedValue = this.matchedValue.withType(type, type, markers, context);

		boolean exhaustive = false;
		for (int i = 0; i < this.caseCount; i++)
		{
			final MatchCase c = this.cases[i];
			if (exhaustive)
			{
				markers.add(Markers.semantic(c.getPattern().getPosition(), "pattern.dead"));
			}

			c.resolve(markers, type, context);
			if (!exhaustive && c.isExhaustive())
			{
				exhaustive = true;
			}
		}

		return this;
	}

	// --------------- Diagnostic Phases ---------------

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.matchedValue.checkTypes(markers, context);

		for (int i = 0; i < this.caseCount; i++)
		{
			this.cases[i].checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.matchedValue.check(markers, context);

		final Set<Object> values = new HashSet<>(this.caseCount);
		for (int i = 0; i < this.caseCount; i++)
		{
			final MatchCase matchCase = this.cases[i];
			final Pattern pattern = matchCase.pattern;

			matchCase.check(markers, context);

			if (matchCase.condition != null)
			{
				// don't diagnose duplicate patterns for conditional cases
				continue;
			}

			pattern.forEachAtom(subPattern -> {
				final Object constantValue = subPattern.getConstantValue();

				if (constantValue != null && !values.add(constantValue))
				{
					markers.add(Markers.semanticError(subPattern.getPosition(), "match.case.duplicate", constantValue));
				}
			});
		}
	}

	// --------------- Compilation Phases ---------------

	@Override
	public IValue foldConstants()
	{
		this.matchedValue = this.matchedValue.foldConstants();
		for (int i = 0; i < this.caseCount; i++)
		{
			this.cases[i].foldConstants();
		}
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.matchedValue = this.matchedValue.cleanup(compilableList, classCompilableList);
		for (int i = 0; i < this.caseCount; i++)
		{
			this.cases[i].cleanup(compilableList, classCompilableList);
		}
		return this;
	}

	// --------------- Compilation ---------------

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		if (type == null)
		{
			type = this.returnType;
		}

		// type.getFrameType() returns null for void. This is specially handled in the implementations, so we don't need
		// to handle void here

		if (this.canGenerateSwitch())
		{
			this.generateSwitch(writer, type.getFrameType());
		}
		else
		{
			this.generateBranched(writer, type.getFrameType());
		}
	}

	private boolean canGenerateSwitch()
	{
		// First run: Determine if a switch instruction can be generated
		for (int i = 0; i < this.caseCount; i++)
		{
			if (!this.cases[i].pattern.hasSwitchHash())
			{
				return false;
			}
		}

		return true;
	}

	private void generateBranched(MethodWriter writer, Object frameType) throws BytecodeException
	{
		final boolean expr = frameType != null;
		final int localCount = writer.localCount();
		final IType matchedType = this.matchedValue.getType();

		final int varIndex = this.matchedValue.writeStore(writer, null);

		final int localCountInner = writer.localCount();

		Label elseLabel = new Label();
		Label endLabel = new Label();
		boolean exhaustive = false;
		for (int i = 0; ; )
		{
			MatchCase c = this.cases[i];
			if (!exhaustive && c.isExhaustive())
			{
				exhaustive = true;
			}

			c.pattern.writeJumpOnMismatch(writer, varIndex, elseLabel);
			if (c.condition != null)
			{
				c.condition.writeInvJump(writer, elseLabel);
			}

			this.writeAction(writer, expr, frameType, c.action);

			writer.resetLocals(localCountInner);

			if (!writer.hasReturn())
			{
				writer.visitJumpInsn(Opcodes.GOTO, endLabel);
			}

			writer.visitTargetLabel(elseLabel);
			if (++i < this.caseCount)
			{
				elseLabel = new Label();
			}
			else
			{
				break;
			}
		}

		// MatchError
		writer.visitTargetLabel(elseLabel);
		if (!exhaustive)
		{
			this.writeMatchError(writer, varIndex, matchedType);
		}

		writer.visitTargetLabel(endLabel);
		writer.resetLocals(localCount);
	}

	private void writeMatchError(MethodWriter writer, int varIndex, IType matchedType) throws BytecodeException
	{
		final int lineNumber = this.lineNumber();

		writer.visitTypeInsn(Opcodes.NEW, "dyvil/util/MatchError");

		writer.visitInsn(Opcodes.DUP);
		writer.visitVarInsn(matchedType.getLoadOpcode(), varIndex);
		matchedType.writeCast(writer, Types.OBJECT, lineNumber);

		writer.visitLineNumber(lineNumber);
		writer
			.visitMethodInsn(Opcodes.INVOKESPECIAL, "dyvil/util/MatchError", "<init>", "(Ljava/lang/Object;)V", false);
		writer.visitInsn(Opcodes.ATHROW);
	}

	private void writeAction(MethodWriter writer, boolean expr, Object frameType, IValue value) throws BytecodeException
	{
		if (expr)
		{
			if (value != null)
			{
				value.writeExpression(writer, this.returnType);
			}
			else
			{
				this.returnType.writeDefaultValue(writer);
			}

			if (!writer.hasReturn())
			{
				writer.getFrame().set(frameType);
			}
		}
		else if (value != null)
		{
			value.writeExpression(writer, Types.VOID);
		}
	}

	private void generateSwitch(MethodWriter writer, Object frameType) throws BytecodeException
	{
		final KeyCache keyCache = new KeyCache();
		MatchCase defaultCase = null;
		Label defaultLabel = null;
		boolean switchVar = false; // Do we need to store the value in a variable (for further equality checks later)

		// find the default case, find out if a variable needs to be generated, and fill the key cache
		for (int i = 0; i < this.caseCount; i++)
		{
			MatchCase matchCase = this.cases[i];
			Pattern pattern = matchCase.pattern;

			switchVar |= !pattern.isSwitchHashInjective();

			if (matchCase.isExhaustive())
			{
				// if any sub-pattern is exhaustive, the entire pattern is exhaustive
				// thus, the other sub-patterns also don't need to be added to the key cache
				defaultCase = matchCase;
				defaultLabel = new Label();
			}
			else
			{
				pattern.forEachAtom(atom -> {
					if (!atom.isExhaustive())
					{
						final KeyCache.Entry entry = keyCache.add(atom.getSwitchHashValue(), matchCase, atom);
						entry.switchLabel = new Label();
					}
				});
			}
		}

		// Check if a match error should be generated - Non-exhaustive pattern
		// and no default label
		final Label endLabel = new Label();

		if (defaultCase == null)
		{
			// not exhaustive, need a match error check
			switchVar = true;
			defaultLabel = new Label();
		}

		final boolean expr = frameType != null;

		// Write the value
		final IType matchedType = this.matchedValue.getType();

		final int localCount = writer.localCount();

		final int varIndex;
		if (switchVar)
		{
			varIndex = this.matchedValue.writeStoreLoad(writer, null);
		}
		else
		{
			varIndex = -1;
			this.matchedValue.writeExpression(writer, null);
		}

		if (Types.isSuperClass(Types.ENUM, matchedType))
		{
			// x.name().hashCode()
			writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Enum", "name", "()Ljava/lang/String;", false);
			writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
		}
		else if (Types.isSuperClass(Types.STRING, matchedType))
		{
			// x.hashCode()
			writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
		}
		else if (matchedType.getAnnotation(Types.SWITCHOPTIMIZED_CLASS) != null)
		{
			// x.getClass().getName().hashCode()
			writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
			writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;", false);
			writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
		}

		final int localCountInner = writer.localCount();

		final SortedSet<KeyCache.Entry> entries = keyCache.uniqueEntries();
		final int min = keyCache.min();
		final int max = keyCache.max();
		final int count = keyCache.count();

		// Choose and generate the appropriate instruction
		if (useTableSwitch(min, max, count))
		{
			this.writeTableSwitch(writer, entries, defaultLabel, min, max);
		}
		else if (count > 0)
		{
			this.writeLookupSwitch(writer, entries, defaultLabel);
		}

		// Fourth run: generate the target labels
		for (KeyCache.Entry topEntry : entries)
		{
			KeyCache.Entry entry = topEntry;

			do
			{
				final KeyCache.Entry next = entry.next;
				final MatchCase matchCase = entry.matchCase;
				final Pattern pattern = entry.pattern;

				final Label elseLabel = next != null ? next.switchLabel : defaultLabel;

				writer.visitTargetLabel(entry.switchLabel);

				if (!pattern.isSwitchHashInjective())
				{
					pattern.writeJumpOnMismatch(writer, varIndex, elseLabel);
				}

				if (matchCase.condition != null)
				{
					matchCase.condition.writeInvJump(writer, elseLabel);
				}

				this.writeAction(writer, expr, frameType, matchCase.action);

				writer.resetLocals(localCountInner);

				if (!writer.hasReturn())
				{
					writer.visitJumpInsn(Opcodes.GOTO, endLabel);
				}

				entry = next;
			}
			while (entry != null);
		}

		writer.visitTargetLabel(defaultLabel);

		// Default Case
		if (defaultCase != null)
		{
			writer.visitTargetLabel(defaultLabel);

			if (!defaultCase.pattern.isSwitchHashInjective())
			{
				// passing null as the target because it should not perform a jump - only binding if necessary
				defaultCase.pattern.writeJumpOnMismatch(writer, varIndex, null);
			}

			assert defaultCase.condition == null; // because otherwise it wouldn't be exhaustive

			this.writeAction(writer, expr, frameType, defaultCase.action);

			writer.resetLocals(localCountInner);

			if (!writer.hasReturn())
			{
				writer.visitJumpInsn(Opcodes.GOTO, endLabel);
			}
		}
		else
		{
			this.writeMatchError(writer, varIndex, matchedType);
		}

		writer.visitTargetLabel(endLabel);

		writer.resetLocals(localCount);
	}

	/**
	 * Determines whether to generate a {@code tableswitch} or a {@code lookupswitch} instruction, and returns {@code
	 * true} when a {@code tableswitch} should be generated.
	 *
	 * @param low
	 * 	the lowest value
	 * @param high
	 * 	the highest value
	 * @param count
	 * 	the number of cases
	 *
	 * @return true, if a tableswitch instruction should be used
	 */
	private static boolean useTableSwitch(int low, int high, int count)
	{
		// this calculation can cause integer overflow with string hash codes of large absolute value
		final long tableSpace = 4L + (long) high - (long) low + 1L;
		final int tableTime = 3; // constant time
		final int lookupSpace = 3 + 2 * count;
		final int lookupTime = MathUtils.log2(count); // binary search O(log n)
		return count > 0 && tableSpace + 3 * tableTime <= lookupSpace + 3 * lookupTime;
	}

	/**
	 * Generates a {@code lookupswitch} instruction
	 */
	private void writeLookupSwitch(MethodWriter writer, SortedSet<KeyCache.Entry> entries, Label defaultLabel)
		throws BytecodeException
	{
		final int length = entries.size();
		final int[] keys = new int[length];
		final Label[] handlers = new Label[length];
		int index = 0;

		for (KeyCache.Entry entry : entries)
		{
			// keys are sorted as long as entries are sorted (and it's a SortedSet)
			keys[index] = entry.key;
			handlers[index] = entry.switchLabel;
			index++;
		}

		writer.visitLookupSwitchInsn(defaultLabel, keys, handlers);
	}

	/**
	 * Generates a {@code tableswitch} instruction
	 */
	private void writeTableSwitch(MethodWriter writer, Collection<KeyCache.Entry> entries, Label defaultLabel, int min,
		int max) throws BytecodeException
	{
		assert defaultLabel != null;

		// use long math to avoid overflow
		final int size = (int) ((long) max - (long) min + 1L);
		final Label[] handlers = new Label[size];

		// indices that don't correspond to an entry point to the default label
		Arrays.fill(handlers, defaultLabel);

		for (KeyCache.Entry entry : entries)
		{
			handlers[entry.key - min] = entry.switchLabel;
		}

		writer.visitTableSwitchInsn(min, max, defaultLabel, handlers);
	}

	// --------------- Formatting ---------------

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		this.matchedValue.toString(indent, buffer);
		if (this.caseCount == 1 && Formatting.getBoolean("match.convert_single"))
		{
			buffer.append(" match ");
			this.cases[0].toString(indent, buffer);
			return;
		}

		if (Formatting.getBoolean("match.newline_after"))
		{
			buffer.append(" match\n").append(indent).append("{\n");
		}
		else
		{
			buffer.append(" match {\n");
		}

		String casePrefix = Formatting.getIndent("match.indent", indent);
		for (int i = 0; i < this.caseCount; i++)
		{
			buffer.append(casePrefix);
			this.cases[i].toString(casePrefix, buffer);
			buffer.append('\n');
		}
		buffer.append(indent).append('}');
	}
}
