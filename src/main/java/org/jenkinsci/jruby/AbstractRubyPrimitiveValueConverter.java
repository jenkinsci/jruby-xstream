package org.jenkinsci.jruby;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * Base class for converting simple values that maps to a string.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractRubyPrimitiveValueConverter<T extends IRubyObject> implements Converter {
    protected final RubyRuntimeResolver resolver;
    protected final Class<T> type;
    protected abstract T fromString(Ruby runtime, String value);
    protected abstract String toString(T obj);

    protected AbstractRubyPrimitiveValueConverter(RubyRuntimeResolver resolver, Class<T> type) {
        this.resolver = resolver;
        this.type = type;
    }

    public boolean canConvert(Class type) {
        return type==this.type;
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        IRubyObject o = (IRubyObject) source;
        resolver.marshal(o,writer,context);
        writer.addAttribute("ruby-class", o.getType().getName());
        writer.setValue(toString((T)source));
    }

    public T unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Ruby runtime = resolver.unmarshal(reader, context);
        return fromString(runtime,reader.getValue());
    }
}
