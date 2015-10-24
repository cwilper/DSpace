package org.dspace.core;

/**
 * Interface for consumers of <code>Context</code> objects.
 */
public interface ContextConsumer
{
    /**
     * Performs some action with a given context.
     */
    void accept(Context context) throws Exception;
}
