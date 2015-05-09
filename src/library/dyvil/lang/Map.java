package dyvil.lang;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import dyvil.collection.ImmutableMap;
import dyvil.collection.MutableMap;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;
import dyvil.tuple.Tuple2;

@NilConvertible
@ArrayConvertible
public interface Map<K, V> extends Iterable<Entry<K, V>>
{
	public static <K, V> MutableMap<K, V> apply()
	{
		return MutableMap.apply();
	}
	
	public static <K, V> ImmutableMap<K, V> apply(Entry<K, V> entry)
	{
		return ImmutableMap.apply(entry);
	}
	
	public static <K, V> ImmutableMap<K, V> apply(Tuple2<? extends K, ? extends V>... entries)
	{
		return ImmutableMap.apply(entries);
	}
	
	// Simple Getters
	
	/**
	 * Returns the size of this map, i.e. the number of mappings contained in
	 * this map.
	 */
	public int size();
	
	/**
	 * Returns true if and if only this map contains no mappings. The standard
	 * implementation defines a map as empty if it's size as calculated by
	 * {@link #size()} is exactly {@code 0}.
	 * 
	 * @return true, if this map contains no mappings
	 */
	public default boolean isEmpty()
	{
		return this.size() == 0;
	}
	
	/**
	 * Creates and returns an {@link Iterator} over the mappings of this map,
	 * packed in {@linkplain Entry Tuples} containing the key as their first
	 * value and the value as their second value.
	 * 
	 * @return an iterator over the mappings of this map
	 */
	@Override
	public Iterator<Entry<K, V>> iterator();
	
	/**
	 * Creates and returns an {@link Spliterator} over the mappings of this map,
	 * packed in {@linkplain Entry Tuples} containing the key as their first
	 * value and the value as their second value.
	 * 
	 * @return an iterator over the mappings of this map
	 */
	@Override
	public default Spliterator<Entry<K, V>> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.size(), 0);
	}
	
	public Iterator<K> keyIterator();
	
	public Iterator<V> valueIterator();
	
	@Override
	public void forEach(Consumer<? super Entry<K, V>> action);
	
	public void forEach(BiConsumer<? super K, ? super V> action);
	
	/**
	 * Returns true if and if only this map contains a mapping for the given
	 * {@code key}.
	 * 
	 * @param key
	 *            the key
	 * @return true, if this map contains a mapping for the key
	 */
	public boolean $qmark(Object key);
	
	/**
	 * Returns true if and if only this map contains a mapping that maps the
	 * given {@code key} to the given {@code value}.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return true, if this map contains a mapping for the key and the value
	 */
	public boolean $qmark(Object key, Object value);
	
	/**
	 * Returns true if and if only this map contains a mapping that maps the
	 * key, as given by the first value of the {@code entry} to the value, as
	 * given by the second value of the {@code entry}. The default
	 * implementation of this method delegates to the {@link $qmark(Object,
	 * Object)} method.
	 * 
	 * @param entry
	 *            the entry
	 * @return true, if this map contains the mapping represented by the entry
	 */
	public default boolean $qmark(Entry<? extends K, ? extends V> entry)
	{
		return this.$qmark(entry.getKey(), entry.getValue());
	}
	
	/**
	 * Returns true if and if only this map contains a mapping to the given
	 * {@code value}.
	 * 
	 * @param value
	 *            the value
	 * @return true, if this map contains a mapping to the value
	 */
	public boolean $qmark$colon(V value);
	
	/**
	 * Gets and returns the value for the given {@code key}. If no mapping for
	 * the {@code key} exists, {@code null} is returned.
	 * 
	 * @param key
	 *            the key
	 * @return the value
	 */
	public V apply(K key);
	
	// Non-mutating Operations
	
	/**
	 * Returns a map that contains all entries of this map plus the new entry
	 * specified by {@code key} and {@code value} as if it were added by
	 * {@link #update(Object, Object)}. If the {@code key} is already present in
	 * this map, a map is returned that uses the given {@code value} instead of
	 * the previous value for the {@code key}, and that has the same size as
	 * this map.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return a map that contains all entries of this map plus the new entry
	 */
	public Map<K, V> $plus(K key, V value);
	
	/**
	 * Returns a map that contains all entries of this map plus the new
	 * {@code entry}, as if it were added by {@link #update(Object, Object)}. If
	 * the {@code key} is already present in this map, a map is returned that
	 * uses the given {@code value} instead of the previous value for the
	 * {@code key}, and that has the same size as this map.
	 * 
	 * @see #$plus(Object, Object)
	 * @param entry
	 *            the entry
	 * @return a map that contains all entries of this map plus the new entry
	 */
	public default Map<K, V> $plus(Entry<? extends K, ? extends V> entry)
	{
		return this.$plus(entry.getKey(), entry.getValue());
	}
	
	/**
	 * Returns a map that contains all entries of this map plus all entries of
	 * the given {@code map}, as if they were added by
	 * {@link #update(Object, Object)}. If a key in the given map is already
	 * present in this map, a map is returned that uses the value from the given
	 * {@code map} for that key.
	 * 
	 * @param map
	 * @return
	 */
	public Map<K, V> $plus$plus(Map<? extends K, ? extends V> map);
	
	public Map<K, V> $minus(K key);
	
	public Map<K, V> $minus(K key, V value);
	
	public default Map<K, V> $minus(Entry<? extends K, ? extends V> entry)
	{
		return this.$minus(entry.getKey(), entry.getValue());
	}
	
	public Map<K, V> $minus$colon(V value);
	
	public Map<K, V> $minus$minus(Map<? extends K, ? extends V> map);
	
	public <U> Map<K, U> mapped(BiFunction<? super K, ? super V, ? extends U> mapper);
	
	public Map<K, V> filtered(BiPredicate<? super K, ? super V> condition);
	
	// Mutating Operations
	
	public void clear();
	
	public void update(K key, V value);
	
	public V put(K key, V value);
	
	public default void $plus$eq(Entry<? extends K, ? extends V> entry)
	{
		this.update(entry.getKey(), entry.getValue());
	}
	
	public void $plus$plus$eq(Map<? extends K, ? extends V> map);
	
	public void $minus$eq(K key);
	
	public V remove(K key);
	
	public boolean remove(K key, V value);
	
	public default void $minus$eq(Entry<? extends K, ? extends V> entry)
	{
		this.remove(entry.getKey(), entry.getValue());
	}
	
	public void $minus$colon$eq(V value);
	
	public void $minus$minus$eq(Map<? extends K, ? extends V> map);
	
	public void map(BiFunction<? super K, ? super V, ? extends V> mapper);
	
	public void filter(BiPredicate<? super K, ? super V> condition);
	
	// Copying
	
	public Map<K, V> copy();
	
	public MutableMap<K, V> mutable();
	
	public ImmutableMap<K, V> immutable();
}
