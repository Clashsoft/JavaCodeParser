package dyvil.tools.compiler.ast.method.intrinsic;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.asm.Type;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public class SpecialIntrinsicData implements IntrinsicData
{
	private IMethod method;
	
	private int[]    instructions;
	private String[] strings;
	private Label[]  targets;
	
	public SpecialIntrinsicData(IMethod method, int[] instructions, String[] strings, Label[] targets)
	{
		this.method = method;
		this.instructions = instructions;
		this.strings = strings;
		this.targets = targets;
	}
	
	@Override
	public void writeIntrinsic(MethodWriter writer, IValue instance, IArguments arguments, int lineNumber)
			throws BytecodeException
	{
		int[] ints = this.instructions;
		int insn = 0;
		
		int length = this.instructions.length;
		for (int i = 0; i < length; i++)
		{
			Label label = this.targets[insn++];
			if (label != null)
			{
				writer.visitTargetLabel(label);
			}
			
			final int opcode = ints[i];
			if (Opcodes.isFieldOpcode(opcode))
			{
				final String owner = this.strings[ints[i + 1]];
				final String name = this.strings[ints[i + 2]];
				final String desc = this.strings[ints[i + 3]];
				writer.visitFieldInsn(opcode, owner, name, desc);
				i += 3;
				continue;
			}
			if (Opcodes.isMethodOpcode(opcode))
			{
				final String owner = this.strings[ints[i + 1]];
				final String name = this.strings[ints[i + 2]];
				final String desc = this.strings[ints[i + 3]];
				
				final IClass iclass = Package.rootPackage.resolveInternalClass(owner);
				final boolean isInterface = iclass != null && iclass.isInterface();
				writer.visitMethodInsn(opcode, owner, name, desc, isInterface);

				i += 3;
				continue;
			}
			if (Opcodes.isJumpOpcode(opcode))
			{
				writer.visitJumpInsn(opcode, this.targets[ints[i + 1]]);

				i += 1;
				continue;
			}
			
			switch (opcode)
			{
			case Opcodes.LOAD_0:
				IntrinsicData.writeArgument(writer, this.method, 0, instance, arguments);
				continue;
			case Opcodes.LOAD_1:
				IntrinsicData.writeArgument(writer, this.method, 1, instance, arguments);
				continue;
			case Opcodes.LOAD_2:
				IntrinsicData.writeArgument(writer, this.method, 2, instance, arguments);
				continue;
			case Opcodes.BIPUSH:
			case Opcodes.SIPUSH:
				writer.visitLdcInsn(ints[i + 1]);
				i++;
				continue;
			case Opcodes.LDC:
				final String constant = this.strings[ints[i + 1]];
				writeLDC(writer, constant);
				i++;
				continue;
			}
			
			writer.visitInsnAtLine(opcode, lineNumber);
		}
	}
	
	@Override
	public void writeIntrinsic(MethodWriter writer, Label dest, IValue instance, IArguments arguments, int lineNumber)
			throws BytecodeException
	{
		this.writeIntrinsic(writer, instance, arguments, lineNumber);
		writer.visitJumpInsn(Opcodes.IFNE, dest);
	}
	
	@Override
	public void writeInvIntrinsic(MethodWriter writer, Label dest, IValue instance, IArguments arguments, int lineNumber)
			throws BytecodeException
	{
		this.writeIntrinsic(writer, instance, arguments, lineNumber);
		writer.visitJumpInsn(Opcodes.IFEQ, dest);
	}
	
	private static void writeLDC(MethodWriter writer, String constant)
	{
		switch (constant.charAt(0))
		{
		case 'I':
			writer.visitLdcInsn(Integer.parseInt(constant.substring(1)));
			return;
		case 'L':
			writer.visitLdcInsn(Long.parseLong(constant.substring(1)));
			return;
		case 'F':
			writer.visitLdcInsn(Float.parseFloat(constant.substring(1)));
			return;
		case 'D':
			writer.visitLdcInsn(Double.parseDouble(constant.substring(1)));
			return;
		case 'S':
		case '"':
		case '\'':
			writer.visitLdcInsn(constant.substring(1));
			return;
		case 'C':
			writer.visitLdcInsn(Type.getType(constant.substring(1)));
			return;
		}
	}
}
