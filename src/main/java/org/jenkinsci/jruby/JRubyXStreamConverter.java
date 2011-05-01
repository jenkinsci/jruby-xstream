package org.jenkinsci.jruby;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import org.jruby.Ruby;
import org.jruby.RubyBasicObject;
import org.jruby.RubyClass;
import org.jruby.RubyObject;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.builtin.Variable;

import java.util.Iterator;

/**
 * @author Kohsuke Kawaguchi
 */
public class JRubyXStreamConverter implements Converter {
    private final Ruby runtime;
    protected final Mapper mapper;

    public JRubyXStreamConverter(Ruby runtime, Mapper mapper) {
        this.runtime = runtime;
        this.mapper = mapper;
    }

    public boolean canConvert(Class type) {
        return RubyBasicObject.class.isAssignableFrom(type);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        RubyBasicObject o = (RubyBasicObject) source;
        for (Variable v : o.getVariableList()) {
            Object value = v.getValue();
            if (value ==null)   continue;
            writer.startNode(v.getName().substring(1)); // cut off the first '@'

            String serializedClassName;
            if (value instanceof RubyBasicObject)
                serializedClassName = "ruby:"+((RubyBasicObject)value).getMetaClass().getName();
            else
                serializedClassName = mapper.serializedClass(value.getClass());

            writer.addAttribute("class", serializedClassName);

            context.convertAnother(value);
            writer.endNode();
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        String className = reader.getAttribute("class");
        assert className.startsWith("ruby:");
        RubyClass c = runtime.getClass(className.substring(5));
        // TODO: error handling in class resolution

        RubyObject o = new RubyObject(c);

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            String originalNodeName = reader.getNodeName();

            className = reader.getAttribute("class");
            // TODO: support type annotation in Ruby class

            IRubyObject value = (IRubyObject)context.convertAnother(o, mapper.realClass(className));

            String fieldName = reader.getNodeName();

            o.setInstanceVariable('@' + fieldName, value);

            reader.moveUp();
        }
        return o;
    }
}
