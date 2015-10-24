package org.dspace.core;

import com.google.api.client.repackaged.com.google.common.base.Throwables;
import org.dspace.eperson.EPerson;

import javax.annotation.Nullable;
import java.sql.SQLException;

/**
 * Provides pre-configured <code>Context</code> objects and helper methods for working with them.
 */
public class ContextSupplier
{
    private boolean readOnly;

    private boolean privileged;

    private EPerson user;

    /**
     * Creates a supplier with default configuration.
     *
     * The instance will have readOnly false, privileged false, and user null.
     */
    public ContextSupplier() {
    }

    /**
     * Creates a supplier with the same configuration as the given context, completing the context
     * before returning.
     */
    public static ContextSupplier from(Context context) {
        ContextSupplier supplier = new ContextSupplier()
                .setReadOnly(context.isReadOnly())
                .setPrivileged(context.ignoreAuthorization())
                .setUser(context.getCurrentUser());
        complete(context);
        return supplier;
    }

    /**
     * Indicates whether this instance supplies contexts in read or write mode.
     */
    public boolean getReadOnly() {
        return readOnly;
    }

    /**
     * Configures this instance to supply contexts in read or write mode.
     *
     * @return this, to support builder-style invocation.
     */
    public ContextSupplier setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    /**
     * Indicates whether this instance supplies contexts with authorization disabled or enabled.
     */
    public boolean getPrivileged() {
        return privileged;
    }

    /**
     * Configures this instance to supply contexts with authorization disabled or enabled.
     *
     * @return this, to support builder-style invocation.
     */
    public ContextSupplier setPrivileged(boolean privileged) {
        this.privileged = privileged;
        return this;
    }

    /**
     * Gets the user this instance supplies contexts for.
     */
    @Nullable
    public EPerson getUser() {
        return user;
    }

    /**
     * Configures this instance to supply contexts for the given user.
     *
     * @return this, to support builder-style invocation.
     */
    public ContextSupplier setUser(final @Nullable EPerson user) {
        this.user = user;
        return this;
    }

    /**
     * Gets a new pre-configured context.
     */
    public Context get() {
        Context context;
        if (readOnly) {
            context = new Context(Context.READ_ONLY);
        } else {
            context = new Context();
        }
        if (privileged) {
            context.turnOffAuthorisationSystem();
        }
        if (user != null) {
            context.setCurrentUser(user);
        }
        return context;
    }

    /**
     * Convenience method to call {@link #complete(Context)}, then {@link #get()}.
     */
    public Context renew(Context context) {
        complete(context);
        return get();
    }

    /**
     * Completes the given context, throwing an unchecked exception in the event of failure.
     *
     * @see Context#complete()
     */
    public static void complete(final Context context) {
        try {
            context.complete();
        } catch (SQLException e) {
            Throwables.propagate(e);
        }
    }

    /**
     * Consumes a new pre-configured context, completing when finished.
     *
     * If the consumer throws a exception, the context will be aborted before bubbling up.
     * If the exception thrown is checked, it will be wrapped in an unchecked exception.
     */
    public void consumeAndComplete(ContextConsumer consumer) {
        final Context context = get();
        try {
            consumer.accept(context);
            complete(context);
        } catch (Exception e) {
            Throwables.propagate(e);
        } finally {
            if (context.isValid()) {
                context.abort();
            }
        }
    }
}
