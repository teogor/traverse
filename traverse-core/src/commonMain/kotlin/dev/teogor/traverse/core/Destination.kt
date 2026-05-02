package dev.teogor.traverse.core
/**
 * Marker interface implemented by all Traverse navigation destinations.
 *
 * Every concrete destination must also be annotated with @Serializable:
 *
 * ```kotlin
 * @Serializable data object Home : Destination
 * @Serializable data class UserProfile(val userId: String) : Destination
 * ```
 */
public interface Destination
