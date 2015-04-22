package dyvil.lang;

import dyvil.annotation.mutating;
import dyvil.collection.immutable.ImmutableCollection;
import dyvil.collection.immutable.ImmutableMap;

/**
 * Marker interface that marks types as <i>immutable</i> to be used in
 * conjunction with the {@link mutating} annotation to compile-time check
 * mutating accesses to immutable types, especially {@link Collection
 * collections}.
 * 
 * @see mutating
 * @see Collection
 * @see ImmutableCollection
 * @see ImmutableMap
 * @author Clashsoft
 * @version 1.0
 */
public interface Immutable
{
}