package org.jenkinsci.jruby;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * When persistence of multiple JRuby runtime is involved, the caller can provide an implementation
 * of this interface to (1) write out enough keys to reconnect back to the right runtime, and (2)
 * read those info and actually resolve the instance of {@link Ruby}.
 *
 * <p>
 * When a single {@link XStream} instance may involve reading/writing an object graph that consists of
 * Ruby objects from multiple separate JVMs, this abstraction provides you a means to preserve them.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class RubyRuntimeResolver {
    /**
     *
     * @return
     *      Return the ruby runtime from which the object in question will be unmarshalled from.
     */
    public abstract Ruby unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context);

    /**
     * Write out the necessary attributes/elements for the {@link #unmarshal(HierarchicalStreamReader, UnmarshallingContext)} method to reconnect back later.
     *
     * @param instance
     *      Ruby object being serialized.
     */
    public abstract void marshal(IRubyObject instance, HierarchicalStreamWriter writer, MarshallingContext context);

    /**
     * Returns an implementation that always resolves to the single ruby runtime.
     * No data is added to the persisted form.
     */
    public static RubyRuntimeResolver of(final Ruby runtime) {
        return new RubyRuntimeResolver() {
            @Override
            public Ruby unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
                return runtime;
            }

            @Override
            public void marshal(IRubyObject instance, HierarchicalStreamWriter writer, MarshallingContext context) {
            }
        };
    }
}
