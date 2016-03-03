package dyvil.tools.repl.context;

import dyvil.collection.List;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.reflect.ReflectUtils;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.*;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.ICodePosition;
import dyvil.tools.repl.DyvilREPL;

public class REPLVariable extends Field
{
	private REPLContext context;

	protected String bytecodeName;
	protected String className;
	private   Class  theClass;
	
	public REPLVariable(REPLContext context, ICodePosition position, Name name, IType type, IValue value, String className, ModifierSet modifiers)
	{
		super(null, name, type);
		this.context = context;
		this.className = className;
		this.modifiers = modifiers;
		this.position = position;
		this.value = value;

		REPLContext.updateModifiers(modifiers);
	}
	
	@Override
	public boolean hasModifier(int mod)
	{
		return mod == Modifiers.STATIC || this.modifiers.hasIntModifier(mod);
	}
	
	private static void filterStackTrace(Throwable throwable)
	{
		StackTraceElement[] traceElements = throwable.getStackTrace();
		int count = traceElements.length;
		int lastIndex = count - 1;

		for (; lastIndex >= 0; --lastIndex)
		{
			if (traceElements[lastIndex].getClassName().startsWith("sun.misc.Unsafe"))
			{
				--lastIndex;
				break;
			}
		}

		StackTraceElement[] newTraceElements = new StackTraceElement[lastIndex + 1];
		System.arraycopy(traceElements, 0, newTraceElements, 0, lastIndex + 1);

		throwable.setStackTrace(newTraceElements);

		Throwable cause = throwable.getCause();
		if (cause != null)
		{
			filterStackTrace(cause);
		}

		for (Throwable suppressed : throwable.getSuppressed())
		{
			filterStackTrace(suppressed);
		}
	}

	protected void compute(DyvilREPL repl, List<IClassCompilable> compilableList)
	{
		if (this.isConstant() && !compilableList.isEmpty())
		{
			return;
		}

		try
		{
			this.theClass = this.generateClass(this.className, compilableList);
		}
		catch (Throwable throwable)
		{
			throwable.printStackTrace(repl.getErrorOutput());
		}

		try
		{
			this.updateValue(repl);
		}
		catch (Throwable t)
		{
			filterStackTrace(t);
			t.printStackTrace(repl.getOutput());
		}
	}

	protected void updateValue(DyvilREPL repl)
	{
		try
		{
			if (this.type == Types.VOID)
			{
				ReflectUtils.UNSAFE.ensureClassInitialized(this.theClass);
			}
			else
			{
				java.lang.reflect.Field field = this.theClass.getDeclaredFields()[0];
				field.setAccessible(true);
				Object result = field.get(null);
				this.value = new REPLResult(result);
			}
		}
		catch (IllegalAccessException illegalAccess)
		{
			illegalAccess.printStackTrace(repl.getOutput());
		}
		catch (ExceptionInInitializerError initializerError)
		{
			final Throwable cause = initializerError.getCause();
			filterStackTrace(cause);
			cause.printStackTrace(repl.getOutput());
		}
		catch (Throwable throwable)
		{
			filterStackTrace(throwable);
			throwable.printStackTrace(repl.getOutput());
		}
	}

	private boolean isConstant()
	{
		return this.hasModifier(Modifiers.FINAL) && this.value != null && isConstant(this.value);
	}

	private static boolean isConstant(IValue value)
	{
		int tag = value.valueTag();
		return tag >= 0 && tag != IValue.NIL && tag < IValue.STRING;
	}

	private Class generateClass(String className, List<IClassCompilable> compilableList) throws Throwable
	{
		String name = this.bytecodeName = this.name.qualified;

		String extendedType = this.type.getExtendedName();
		ClassWriter cw = new ClassWriter();
		// Generate Class Header
		cw.visit(ClassFormat.CLASS_VERSION, Modifiers.PUBLIC | Modifiers.FINAL | ClassFormat.ACC_SUPER, className, null,
		         "java/lang/Object", null);
		
		cw.visitSource(className, null);
		
		if (this.type != Types.VOID)
		{
			// Generate the field holding the value
			cw.visitField(this.modifiers.toFlags(), name, extendedType, null, null);
		}
		
		// Compilables
		for (IClassCompilable c : compilableList)
		{
			c.write(cw);
		}
		
		// Generate <clinit> static initializer
		MethodWriter mw = new MethodWriterImpl(cw,
		                                       cw.visitMethod(Modifiers.STATIC | Modifiers.SYNTHETIC, "<clinit>", "()V",
		                                                      null, null));
		mw.begin();
		
		for (IClassCompilable c : compilableList)
		{
			c.writeStaticInit(mw);
		}
		
		// Write the value
		
		if (this.value != null)
		{
			this.writeValue(className, name, extendedType, cw, mw);
		}
		
		// Finish Method compilation
		mw.writeInsn(Opcodes.RETURN);
		mw.end();
		
		// Finish Class compilation
		cw.visitEnd();
		
		byte[] bytes = cw.toByteArray();
		
		if (this.type != Types.VOID || !compilableList.isEmpty())
		{
			// The type contains the value, so we have to keep the class loaded.
			return REPLMemberClass.loadClass(this.context.repl, className, bytes);
		}
		// We don't have any variables, so we can throw the Class away after
		// it has been loaded.
		return REPLMemberClass.loadAnonymousClass(this.context.repl, className, bytes);
	}
	
	private void writeValue(String className, String name, String extendedType, ClassWriter cw, MethodWriter mw)
			throws BytecodeException
	{
		if (this.type == Types.VOID)
		{
			this.value.writeExpression(mw, Types.VOID);
			return;
		}
		
		String methodType = "()" + extendedType;
		MethodWriter initWriter = new MethodWriterImpl(cw, cw.visitMethod(Modifiers.PRIVATE | Modifiers.STATIC,
		                                                                  "computeValue", methodType, null, null));
		initWriter.begin();
		this.value.writeExpression(initWriter, this.type);
		initWriter.end(this.type);
		
		mw.writeInvokeInsn(Opcodes.INVOKESTATIC, className, "computeValue", methodType, false);
		// Store the value to the field
		mw.writeFieldInsn(Opcodes.PUTSTATIC, className, name, extendedType);
	}
	
	@Override
	public void writeGet(MethodWriter writer, IValue receiver, int lineNumber) throws BytecodeException
	{
		if (this.isConstant())
		{
			this.value.writeExpression(writer, this.type);
			return;
		}
		
		if (this.className == null)
		{
			this.type.writeDefaultValue(writer);
			return;
		}
		
		String extended = this.type.getExtendedName();
		writer.writeFieldInsn(Opcodes.GETSTATIC, this.className, this.bytecodeName, extended);
	}
	
	@Override
	public void writeSet(MethodWriter writer, IValue receiver, IValue value, int lineNumber) throws BytecodeException
	{
		if (value != null)
		{
			value.writeExpression(writer, this.type);
		}
		
		if (this.className == null)
		{
			writer.writeInsn(Opcodes.AUTO_POP);
			return;
		}
		
		String extended = this.type.getExtendedName();
		writer.writeFieldInsn(Opcodes.PUTSTATIC, this.className, this.bytecodeName, extended);
	}
}