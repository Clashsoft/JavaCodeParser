package dyvil.lang;

import java.io.Serializable;

import dyvil.lang.literal.CharConvertible;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.infix;
import dyvil.annotation.inline;
import dyvil.annotation.prefix;

import static dyvil.reflect.Opcodes.*;

@CharConvertible
public class Char implements Integer, Serializable
{
	private static final long serialVersionUID = -4823027430692159020L;
	
	public static final char	min		= java.lang.Character.MIN_VALUE;
	public static final char	max		= java.lang.Character.MAX_VALUE;
	public static final byte	size	= java.lang.Character.SIZE;
	
	protected char value;
	
	private static final class ConstantPool
	{
		protected static final int	TABLE_MIN	= -128;
		protected static final int	TABLE_SIZE	= 256;
		protected static final int	TABLE_MAX	= TABLE_MIN + TABLE_SIZE;
		
		protected static final Char[] TABLE = new Char[TABLE_SIZE];
		
		static
		{
			for (int i = 0; i < TABLE_SIZE; i++)
			{
				TABLE[i] = new Char((char) (i + TABLE_MIN));
			}
		}
	}
	
	public static Char apply(char v)
	{
		if (v >= ConstantPool.TABLE_MIN && v < ConstantPool.TABLE_MAX)
		{
			return ConstantPool.TABLE[v - ConstantPool.TABLE_MIN];
		}
		return new Char(v);
	}
	
	public static @infix char unapply(Char v)
	{
		return v == null ? 0 : v.value;
	}
	
	protected Char(char value)
	{
		this.value = value;
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2B })
	public byte byteValue()
	{
		return (byte) this.value;
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2S })
	public short shortValue()
	{
		return (short) this.value;
	}
	
	@Override
	@Intrinsic({ LOAD_0 })
	public char charValue()
	{
		return this.value;
	}
	
	@Override
	@Intrinsic({ LOAD_0 })
	public int intValue()
	{
		return this.value;
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2L })
	public long longValue()
	{
		return this.value;
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2F })
	public float floatValue()
	{
		return this.value;
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2D })
	public double doubleValue()
	{
		return this.value;
	}
	
	// Unary operators
	
	@Override
	@Intrinsic({ LOAD_0 })
	public @prefix Int $plus()
	{
		return Int.apply(this.value);
	}
	
	@Override
	@Intrinsic({ LOAD_0, INEG })
	public @prefix Int $minus()
	{
		return Int.apply((byte) -this.value);
	}
	
	@Override
	@Intrinsic({ LOAD_0, ICONST_M1, IXOR })
	public @prefix Int $tilde()
	{
		return Int.apply((byte) ~this.value);
	}
	
	// byte operators
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ICMPEQ })
	public boolean $eq$eq(byte v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ICMPNE })
	public boolean $bang$eq(byte v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ICMPLT })
	public boolean $lt(byte v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ICMPLE })
	public boolean $lt$eq(byte v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ICMPGT })
	public boolean $gt(byte v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ICMPGE })
	public boolean $gt$eq(byte v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IADD })
	public Int $plus(byte v)
	{
		return Int.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ISUB })
	public Int $minus(byte v)
	{
		return Int.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IMUL })
	public Int $times(byte v)
	{
		return Int.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2F, LOAD_1, I2F, FDIV })
	public Float $div(byte v)
	{
		return Float.apply((float) this.value / (float) v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IREM })
	public Int $percent(byte v)
	{
		return Int.apply(this.value % v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IDIV })
	public Int $bslash(byte v)
	{
		return Int.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IAND })
	public Int $amp(byte v)
	{
		return Int.apply(this.value & v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IOR })
	public Int $bar(byte v)
	{
		return Int.apply(this.value | v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IXOR })
	public Int $up(byte v)
	{
		return Int.apply(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ISHL })
	public Int $lt$lt(byte v)
	{
		return Int.apply(this.value << v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ISHR })
	public Int $gt$gt(byte v)
	{
		return Int.apply(this.value >> v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IUSHR })
	public Int $gt$gt$gt(byte v)
	{
		return Int.apply(this.value >>> v);
	}
	
	// short operators
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ICMPEQ })
	public boolean $eq$eq(short v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ICMPNE })
	public boolean $bang$eq(short v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ICMPLT })
	public boolean $lt(short v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ICMPLE })
	public boolean $lt$eq(short v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ICMPGT })
	public boolean $gt(short v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ICMPGE })
	public boolean $gt$eq(short v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IADD })
	public Int $plus(short v)
	{
		return Int.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ISUB })
	public Int $minus(short v)
	{
		return Int.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IMUL })
	public Int $times(short v)
	{
		return Int.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2F, LOAD_1, I2F, FDIV })
	public Float $div(short v)
	{
		return Float.apply((float) this.value / (float) v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IREM })
	public Int $percent(short v)
	{
		return Int.apply(this.value % v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IDIV })
	public Int $bslash(short v)
	{
		return Int.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IAND })
	public Int $amp(short v)
	{
		return Int.apply(this.value & v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IOR })
	public Int $bar(short v)
	{
		return Int.apply(this.value | v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IXOR })
	public Int $up(short v)
	{
		return Int.apply(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ISHL })
	public Int $lt$lt(short v)
	{
		return Int.apply(this.value << v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ISHR })
	public Int $gt$gt(short v)
	{
		return Int.apply(this.value >> v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IUSHR })
	public Int $gt$gt$gt(short v)
	{
		return Int.apply(this.value >>> v);
	}
	
	// char operators
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ICMPEQ })
	public boolean $eq$eq(char v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ICMPNE })
	public boolean $bang$eq(char v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ICMPLT })
	public boolean $lt(char v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ICMPLE })
	public boolean $lt$eq(char v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ICMPGT })
	public boolean $gt(char v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ICMPGE })
	public boolean $gt$eq(char v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IADD })
	public Int $plus(char v)
	{
		return Int.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ISUB })
	public Int $minus(char v)
	{
		return Int.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IMUL })
	public Int $times(char v)
	{
		return Int.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2F, LOAD_1, I2F, FDIV })
	public Float $div(char v)
	{
		return Float.apply((float) this.value / (float) v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IREM })
	public Int $percent(char v)
	{
		return Int.apply(this.value % v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IDIV })
	public Int $bslash(char v)
	{
		return Int.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IAND })
	public Int $amp(char v)
	{
		return Int.apply(this.value & v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IOR })
	public Int $bar(char v)
	{
		return Int.apply(this.value | v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IXOR })
	public Int $up(char v)
	{
		return Int.apply(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ISHL })
	public Int $lt$lt(char v)
	{
		return Int.apply(this.value << v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ISHR })
	public Int $gt$gt(char v)
	{
		return Int.apply(this.value >> v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IUSHR })
	public Int $gt$gt$gt(char v)
	{
		return Int.apply(this.value >>> v);
	}
	
	// int operators
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ICMPEQ })
	public boolean $eq$eq(int v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ICMPNE })
	public boolean $bang$eq(int v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ICMPLT })
	public boolean $lt(int v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ICMPLE })
	public boolean $lt$eq(int v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ICMPGT })
	public boolean $gt(int v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ICMPGE })
	public boolean $gt$eq(int v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IADD })
	public Int $plus(int v)
	{
		return Int.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ISUB })
	public Int $minus(int v)
	{
		return Int.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IMUL })
	public Int $times(int v)
	{
		return Int.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2F, LOAD_1, I2F, FDIV })
	public Float $div(int v)
	{
		return Float.apply((float) this.value / (float) v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IREM })
	public Int $percent(int v)
	{
		return Int.apply(this.value % v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IDIV })
	public Int $bslash(int v)
	{
		return Int.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IAND })
	public Int $amp(int v)
	{
		return Int.apply(this.value & v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IOR })
	public Int $bar(int v)
	{
		return Int.apply(this.value | v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IXOR })
	public Int $up(int v)
	{
		return Int.apply(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ISHL })
	public Int $lt$lt(int v)
	{
		return Int.apply(this.value << v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, ISHR })
	public Int $gt$gt(int v)
	{
		return Int.apply(this.value >> v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, IUSHR })
	public Int $gt$gt$gt(int v)
	{
		return Int.apply(this.value >>> v);
	}
	
	// long operators
	
	@Override
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LCMPEQ })
	public boolean $eq$eq(long v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LCMPNE })
	public boolean $bang$eq(long v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LCMPLT })
	public boolean $lt(long v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LCMPLE })
	public boolean $lt$eq(long v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LCMPGT })
	public boolean $gt(long v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LCMPGE })
	public boolean $gt$eq(long v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LADD })
	public Long $plus(long v)
	{
		return Long.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LSUB })
	public Long $minus(long v)
	{
		return Long.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LMUL })
	public Long $times(long v)
	{
		return Long.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2D, LOAD_1, L2D, DDIV })
	public Double $div(long v)
	{
		return Double.apply((double) this.value / (double) v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LREM })
	public Long $percent(long v)
	{
		return Long.apply(this.value % v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LDIV })
	public Long $bslash(long v)
	{
		return Long.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LAND })
	public Long $amp(long v)
	{
		return Long.apply(this.value & v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LOR })
	public Long $bar(long v)
	{
		return Long.apply(this.value | v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2L, LOAD_1, LXOR })
	public Long $up(long v)
	{
		return Long.apply(this.value ^ v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2I, ISHL })
	public Int $lt$lt(long v)
	{
		return Int.apply(this.value << v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2I, ISHR })
	public Int $gt$gt(long v)
	{
		return Int.apply(this.value >> v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, LOAD_1, L2I, IUSHR })
	public Int $gt$gt$gt(long v)
	{
		return Int.apply(this.value >>> v);
	}
	
	// float operators
	
	@Override
	@Intrinsic({ LOAD_0, I2F, LOAD_1, FCMPEQ })
	public boolean $eq$eq(float v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2F, LOAD_1, FCMPNE })
	public boolean $bang$eq(float v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2F, LOAD_1, FCMPLT })
	public boolean $lt(float v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2F, LOAD_1, FCMPLE })
	public boolean $lt$eq(float v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2F, LOAD_1, FCMPGT })
	public boolean $gt(float v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2F, LOAD_1, FCMPGE })
	public boolean $gt$eq(float v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2F, LOAD_1, FADD })
	public Float $plus(float v)
	{
		return Float.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2F, LOAD_1, FSUB })
	public Float $minus(float v)
	{
		return Float.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2F, LOAD_1, FMUL })
	public Float $times(float v)
	{
		return Float.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2F, LOAD_1, FDIV })
	public Float $div(float v)
	{
		return Float.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2F, LOAD_1, FREM })
	public Float $percent(float v)
	{
		return Float.apply(this.value % v);
	}
	
	// double operators
	
	@Override
	@Intrinsic({ LOAD_0, I2D, LOAD_1, DCMPEQ })
	public boolean $eq$eq(double v)
	{
		return this.value == v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2D, LOAD_1, DCMPNE })
	public boolean $bang$eq(double v)
	{
		return this.value != v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2D, LOAD_1, DCMPLT })
	public boolean $lt(double v)
	{
		return this.value < v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2D, LOAD_1, DCMPLE })
	public boolean $lt$eq(double v)
	{
		return this.value <= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2D, LOAD_1, DCMPGT })
	public boolean $gt(double v)
	{
		return this.value > v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2D, LOAD_1, DCMPGE })
	public boolean $gt$eq(double v)
	{
		return this.value >= v;
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2D, LOAD_1, DADD })
	public Double $plus(double v)
	{
		return Double.apply(this.value + v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2D, LOAD_1, DSUB })
	public Double $minus(double v)
	{
		return Double.apply(this.value - v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2D, LOAD_1, DMUL })
	public Double $times(double v)
	{
		return Double.apply(this.value * v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2D, LOAD_1, DDIV })
	public Double $div(double v)
	{
		return Double.apply(this.value / v);
	}
	
	@Override
	@Intrinsic({ LOAD_0, I2D, LOAD_1, DREM })
	public Double $percent(double v)
	{
		return Double.apply(this.value % v);
	}
	
	// generic operators
	
	@Override
	public boolean $eq$eq(Number v)
	{
		return this.value == v.byteValue();
	}
	
	@Override
	public boolean $bang$eq(Number v)
	{
		return this.value != v.byteValue();
	}
	
	@Override
	public boolean $lt(Number v)
	{
		return this.value < v.byteValue();
	}
	
	@Override
	public boolean $lt$eq(Number v)
	{
		return this.value <= v.byteValue();
	}
	
	@Override
	public boolean $gt(Number v)
	{
		return this.value > v.byteValue();
	}
	
	@Override
	public boolean $gt$eq(Number v)
	{
		return this.value >= v.byteValue();
	}
	
	@Override
	public Int $plus(Number v)
	{
		return Int.apply(this.value + v.intValue());
	}
	
	@Override
	public Int $minus(Number v)
	{
		return Int.apply(this.value - v.intValue());
	}
	
	@Override
	public Int $times(Number v)
	{
		return Int.apply(this.value * v.intValue());
	}
	
	@Override
	public Float $div(Number v)
	{
		return Float.apply(this.value / v.floatValue());
	}
	
	@Override
	public Int $bslash(Integer v)
	{
		return Int.apply(this.value / v.intValue());
	}
	
	@Override
	public Int $percent(Number v)
	{
		return Int.apply(this.value % v.intValue());
	}
	
	@Override
	public Int $bar(Integer v)
	{
		return Int.apply(this.value | v.intValue());
	}
	
	@Override
	public Int $amp(Integer v)
	{
		return Int.apply(this.value & v.intValue());
	}
	
	@Override
	public Int $up(Integer v)
	{
		return Int.apply(this.value ^ v.intValue());
	}
	
	@Override
	public Int $lt$lt(Integer v)
	{
		return Int.apply(this.value << v.intValue());
	}
	
	@Override
	public Int $gt$gt(Integer v)
	{
		return Int.apply(this.value >> v.intValue());
	}
	
	@Override
	public Int $gt$gt$gt(Integer v)
	{
		return Int.apply(this.value >>> v.intValue());
	}
	
	@Override
	public int compareTo(Number o)
	{
		return java.lang.Long.compare(this.value, o.longValue());
	}
	
	@Override
	public Char next()
	{
		return Char.apply((char) (this.value + 1));
	}
	
	@Override
	public Number previous()
	{
		return Char.apply((char) (this.value - 1));
	}
	
	// Object methods
	
	public static @infix @inline String toString(char value)
	{
		return String.valueOf(value);
	}
	
	public static @infix @inline String toDecimalString(char value)
	{
		return java.lang.Integer.toString(value);
	}
	
	public static @infix @inline String toBinaryString(char value)
	{
		return java.lang.Integer.toBinaryString(value);
	}
	
	public static @infix @inline String toHexString(char value)
	{
		return java.lang.Integer.toHexString(value);
	}
	
	public static @infix @inline String toOctalString(char value)
	{
		return java.lang.Integer.toOctalString(value);
	}
	
	public static @infix String toString(char value, int radix)
	{
		switch (radix)
		{
		case 2:
			return java.lang.Integer.toBinaryString(value);
		case 8:
			return java.lang.Integer.toOctalString(value);
		case 10:
			return java.lang.Integer.toString(value);
		case 16:
			return java.lang.Integer.toHexString(value);
		}
		return java.lang.Integer.toString(value, radix);
	}
	
	@Override
	public String toString()
	{
		return String.valueOf(this.value);
	}
	
	@Override
	public int hashCode()
	{
		return this.value;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null || !(obj instanceof Number))
		{
			return false;
		}
		Number other = (Number) obj;
		return this.value == other.byteValue();
	}
}
