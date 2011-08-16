package org.jenkinsci.jruby;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * @author Kohsuke Kawaguchi
 */
public class RubyArrayConverter implements Converter {
    private final RubyRuntimeResolver resolver;

    public RubyArrayConverter(RubyRuntimeResolver resolver) {
        this.resolver = resolver;
    }

    public boolean canConvert(Class type) {
        return type==RubyArray.class;
    }

    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext context) {

        RubyArray ra = (RubyArray) o;
        int len = ra.getLength();

        resolver.marshal(ra,writer,context);
        writer.addAttribute("ruby-class", ra.getType().getName());

        for (int i = 0; i < len; i++) {
            IRubyObject item = ra.entry(i);

            writer.startNode("item");
            context.convertAnother(item);
            writer.endNode();
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Ruby runtime = resolver.unmarshal(reader,context);
        RubyArray a = RubyArray.newArray(runtime);

        // read the items from xml into a list
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            IRubyObject item = (IRubyObject)context.convertAnother(null, IRubyObject.class);
            a.append(item);
            reader.moveUp();
        }

        return a;
    }
}
