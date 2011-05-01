package org.jenkinsci.jruby;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * @author Kohsuke Kawaguchi
 */
public class RubyArrayConverter extends AbstractCollectionConverter {
    private final Ruby runtime;

    public RubyArrayConverter(XStream xs, Ruby runtime) {
        super(xs.getMapper());
        this.runtime = runtime;
    }

    public boolean canConvert(Class type) {
        return type==RubyArray.class;
    }

    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext context) {
        RubyArray ra = (RubyArray) o;
        int len = ra.getLength();

        for (int i = 0; i < len; i++) {
            Object item = ra.entry(i);
            writeItem(item, context, writer);
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        RubyArray a = RubyArray.newArray(runtime);

        // read the items from xml into a list
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            IRubyObject item = (IRubyObject)readItem(reader, context, null);
            a.append(item);
            reader.moveUp();
        }

        return a;
    }
}
