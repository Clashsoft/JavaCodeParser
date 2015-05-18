package dyvil.collection;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import dyvil.annotation.mutating;
import dyvil.collection.immutable.EmptyMap;
import dyvil.collection.immutable.SingletonMap;
import dyvil.collection.immutable.TupleMap;
import dyvil.lang.Entry;
import dyvil.lang.Immutable;
import dyvil.lang.ImmutableException;
import dyvil.lang.Map;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;
import dyvil.tuple.Tuple2;

@NilConvertible
@ArrayConvertible
public interface ImmutableMap<K, V> extends Map<K, V>, Immutable
{
	public static <K, V> ImmutableMap<K, V> apply()
	{
		return EmptyMap.apply();
	}
	
	public static <K, V> ImmutableMap<K, V> apply(K key, V value)
	{
		return new SingletonMap(key, value);
	}
	
	public static <K, V> ImmutableMap<K, V> apply(Entry<K, V> entry)
	{
		return new SingletonMap(entry.getKey(), entry.getValue());
	}
	
	public static <K, V> ImmutableMap<K, V> apply(Tuple2<? extends K, ? extends V>... entries)
	{
		int len = entries.length;
		switch (len)
		{
		case 0:
			return EmptyMap.apply();
		case 1:
			Entry<? extends K, ? extends V> entry = entries[0];
			return new SingletonMap(entry.getKey(), entry.getValue());
		default:
			return new TupleMap(entries, len, true);
		}
	}
	
	// Simple Getters
	
	@Override
	public int size();
	
	@Override
	public Iterator<Entry<K, V>> iterator();
	
	@Override
	public default Spliterator<Entry<K, V>> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.size(), Spliterator.IMMUTABLE);
	}
	
	@Override
	public Iterator<K> keyIterator();
	
	@Override
	public default Spliterator<K> keySpliterator()
	{
		return Spliterators.spliterator(this.keyIterator(), this.size(), Spliterator.IMMUTABLE);
	}
	
	@Override
	public Iterator<V> valueIterator();
	
	@Override
	public default Spliterator<V> valueSpliterator()
	{
		return Spliterators.spliterator(this.valueIterator(), this.size(), Spliterator.IMMUTABLE);
	}
	
	@Override
	public boolean $qmark(Object key);
	
	@Override
	public boolean $qmark(Object key, Object value);
	
	@Override
	public default boolean $qmark(Entry<? extends K, ? extends V> entry)
	{
		return this.$qmark(entry.getKey(), entry.getValue());
	}
	
	@Override
	public boolean $qmark$colon(V value);
	
	@Override
	public V apply(K key);
	
	// Non-mutating Operations
	
	@Override
	public ImmutableMap<K, V> $plus(K key, V value);
	
	@Override
	public default ImmutableMap<K, V> $plus(Entry<? extends K, ? extends V> entry)
	{
		return this.$plus(entry.getKey(), entry.getValue());
	}
	
	@Override
	public ImmutableMap<K, V> $plus$plus(Map<? extends K, ? extends V> map);
	
	@Override
	public ImmutableMap<K, V> $minus(K key);
	
	@Override
	public ImmutableMap<K, V> $minus(K key, V value);
	
	@Override
	public default ImmutableMap<K, V> $minus(Entry<? extends K, ? extends V> entry)
	{
		return this.$minus(entry.getKey(), entry.getValue());
	}
	
	@Override
	public ImmutableMap<K, V> $minus$colon(V value);
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Map<? extends K, ? extends V> map);
	
	@Override
	public <U> ImmutableMap<K, U> mapped(BiFunction<? super K, ? super V, ? extends U> mapper);
	
	@Override
	public ImmutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition);
	
	// Mutating Operations
	
	@Override
	@mutating
	public default void clear()
	{
		throw new ImmutableException("clear() on Immutable Map");
	}
	
	@Override
	@mutating
	public default void update(K key, V value)
	{
		throw new ImmutableException("() on Immutable Map");
	}
	
	@Override
	@mutating
	public default V put(K key, V value)
	{
		throw new ImmutableException("put() on Immutable Map");
	}
	
	@Override
	@mutating
	public default void $plus$eq(Entry<? extends K, ? extends V> entry)
	{
		throw new ImmutableException("+= on Immutable Map");
	}
	
	@Override
	@mutating
	public default void $plus$plus$eq(Map<? extends K, ? extends V> map)
	{
		throw new ImmutableException("+= on Immutable Map");
	}
	
	@Override
	@mutating
	public default void $minus$eq(K key)
	{
		throw new ImmutableException("-= on Immutable Map");
	}
	
	@Override
	@mutating
	public default V remove(K key)
	{
		throw new ImmutableException("remove() on Immutable Map");
	}
	
	@Override
	@mutating
	public default boolean remove(K key, V value)
	{
		throw new ImmutableException("remove() on Immutable Map");
	}
	
	@Override
	@mutating
	public default void $minus$eq(Entry<? extends K, ? extends V> entry)
	{
		throw new ImmutableException("-= on Immutable Map");
	}
	
	@Override
	@mutating
	public default void $minus$colon$eq(V value)
	{
		throw new ImmutableException("-:= on Immutable Map");
	}
	
	@Override
	@mutating
	public default void $minus$minus$eq(Map<? extends K, ? extends V> map)
	{
		throw new ImmutableException("-= on Immutable Map");
	}
	
	@Override
	@mutating
	public default void map(BiFunction<? super K, ? super V, ? extends V> mapper)
	{
		throw new ImmutableException("map() on Immutable Map");
	}
	
	@Override
	@mutating
	public default void filter(BiPredicate<? super K, ? super V> condition)
	{
		throw new ImmutableException("filter() on Immutable Map");
	}
	
	// Copying
	
	@Override
	public ImmutableMap<K, V> copy();
	
	@Override
	public MutableMap<K, V> mutable();
	
	@Override
	public default ImmutableMap<K, V> immutable()
	{
		return this;
	}
}