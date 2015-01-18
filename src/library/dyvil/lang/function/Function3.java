package dyvil.lang.function;

@FunctionalInterface
public interface Function3<P1, P2, P3, R>
{
	public R apply(P1 par1, P2 par2, P3 par3);
}
