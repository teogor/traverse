package dev.teogor.traverse.core.dsl

/**
 * DslMarker for the Traverse navigation graph builder DSL.
 *
 * Applied to [TraverseGraphBuilder][dev.teogor.traverse.compose.graph.TraverseGraphBuilder]
 * and any nested builder classes to prevent implicit scope leaks — e.g. calling
 * `screen<T>` from inside a `dialog<T>` lambda is a compile error.
 */
@DslMarker
public annotation class TraverseDsl

