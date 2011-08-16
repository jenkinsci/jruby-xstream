package org.jenkinsci.jruby;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
public class RubyHashConverter implements Converter {
    private final RubyRuntimeResolver resolver;

    public RubyHashConverter(RubyRuntimeResolver resolver) {
        this.resolver = resolver;
    }

    public boolean canConvert(Class type) {
        return type==RubyHash.class;
    }

    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext context) {
        RubyHash hash = (RubyHash) o;
        resolver.marshal(hash,writer,context);
        writer.addAttribute("ruby-class", hash.getType().getName());

        for (Entry e : (Set<Entry>)hash.directEntrySet()) {
            writer.startNode("entry");
            writer.startNode("key");
            context.convertAnother(e.getKey());
            writer.endNode();
            writer.startNode("value");
            context.convertAnother(e.getValue());
            writer.endNode();
            writer.endNode();
        }
    }

    public RubyHash unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Ruby runtime = resolver.unmarshal(reader,context);
        RubyHash hash = RubyHash.newHash(runtime);

        // read the items from xml into a list
        while (reader.hasMoreChildren()) {
            reader.moveDown();

            reader.moveDown();
            IRubyObject key = (IRubyObject)context.convertAnother(null, IRubyObject.class);
            reader.moveUp();

            reader.moveDown();
            IRubyObject value = (IRubyObject)context.convertAnother(null, IRubyObject.class);
            reader.moveUp();

            hash.op_aset(key,value);

            reader.moveUp();
        }

        return hash;
    }
}
