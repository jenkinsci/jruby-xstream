package org.jenkinsci.jruby;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractRubyPrimitiveValueConverter<T extends IRubyObject> implements Converter {
    protected final Ruby runtime;
    protected final Class<T> type;
    protected abstract T fromString(String value);
    protected abstract String toString(T obj);

    protected AbstractRubyPrimitiveValueConverter(Ruby runtime, Class<T> type) {
        this.runtime = runtime;
        this.type = type;
    }

    public boolean canConvert(Class type) {
        return type==this.type;
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        writer.addAttribute("ruby-class", ((IRubyObject)source).getType().getName());
        writer.setValue(toString((T)source));
    }

    public T unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        return fromString(reader.getValue());
    }
}
