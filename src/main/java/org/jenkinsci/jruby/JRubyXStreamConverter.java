package org.jenkinsci.jruby;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import org.jruby.Ruby;
import org.jruby.RubyBasicObject;
import org.jruby.RubyClass;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.builtin.Variable;

/**
 * @author Kohsuke Kawaguchi
 */
public class JRubyXStreamConverter implements Converter {
    private final Ruby runtime;
    private final XStream xs;
    protected final Mapper mapper;

    public JRubyXStreamConverter(XStream xs, Ruby runtime) {
        this.xs = xs;
        this.runtime = runtime;
        this.mapper = xs.getMapper();
    }

    public boolean canConvert(Class type) {
        return IRubyObject.class.isAssignableFrom(type);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        RubyBasicObject o = (RubyBasicObject) source;
        writer.addAttribute("ruby-class", o.getType().getName());

        for (Variable v : o.getVariableList()) {
            Object value = v.getValue();
            if (value ==null)   continue;
            writer.startNode(v.getName().substring(1)); // cut off the first '@'

            if (!(value instanceof IRubyObject)) {
                // if a ruby object refers to another ruby object, just rely on @ruby-class
                // and we don't need @class
                Class<?> valueType = value.getClass();
                String serializedClassName = mapper.serializedClass(valueType);
                writer.addAttribute("class", serializedClassName);
            } else {
                // TODO: use the type annotation to try to omit this whenever we can
            }

            context.convertAnother(value);
            writer.endNode();
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        String className = reader.getAttribute("ruby-class");
        RubyClass c = runtime.getClass(className);
        // TODO: error handling in class resolution

        Class r = c.getReifiedClass();
        if (r!=null) {
            // forward to primitive type converters
            Converter cnv = xs.getConverterLookup().lookupConverterForType(r);
            if (cnv!=this)
                return cnv.unmarshal(reader, context);
        }
        IRubyObject o = c.allocate();

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String fieldName = reader.getNodeName();

            Class valueType;

            className = reader.getAttribute("class");
            if (className!=null) {
                valueType = mapper.realClass(className);
            } else {
                valueType = IRubyObject.class;
            }

            // TODO: support type annotation in Ruby class
            IRubyObject value = (IRubyObject)context.convertAnother(o, valueType);

            c.getVariableAccessorForWrite('@' + fieldName).set(o, value);

            reader.moveUp();
        }
        return o;
    }
}
