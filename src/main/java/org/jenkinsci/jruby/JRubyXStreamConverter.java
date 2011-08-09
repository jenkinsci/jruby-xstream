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
import org.jruby.RubyModule;
import org.jruby.RubySymbol;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.builtin.Variable;

import java.util.List;

/**
 * {@link Converter} for converting generic JRuby objects.
 *
 * <p>
 * If "transient?" instance method is defined on the class itself, this will consult that to find
 * which instance variables are transient, and it'll skip persisting those.
 *
 * @author Kohsuke Kawaguchi
 */
public class JRubyXStreamConverter implements Converter {
    private final Ruby runtime;
    private final XStream xs;
    protected final Mapper mapper;
    private final RubySymbol read_completed;

    public JRubyXStreamConverter(XStream xs, Ruby runtime) {
        this.xs = xs;
        this.runtime = runtime;
        this.mapper = xs.getMapper();
        read_completed = runtime.newSymbol("read_completed");
    }

    public boolean canConvert(Class type) {
        return IRubyObject.class.isAssignableFrom(type);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        RubyBasicObject o = (RubyBasicObject) source;
        RubyClass t = o.getType();
        writer.addAttribute("ruby-class", t.getName());

        boolean hasTransient = t.getType().getMethods().containsKey("transient?");

        for (Variable v : o.getVariableList()) {
            Object value = v.getValue();
            if (value ==null)   continue;

            String vname = v.getName().substring(1);    // cut off the first '@'

            if (hasTransient && t.callMethod("transient?", runtime.newString(vname)).isTrue())
                continue;   // transient field

            writer.startNode(vname);

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
        RubyClass c = resolveClass(className);
        if (c==null)
            throw new IllegalArgumentException("Undefined class: "+className);

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

        // invoke readResolve if available
        callReadCompleted(o);

        return o;
    }

    /**
     * Invokes all the defined read_completed methods.
     */
    private void callReadCompleted(IRubyObject o) {
	    List<IRubyObject> ancestors = o.getType().getAncestorList();
		for (int i = ancestors.size() - 1; i >= 0; i--) {
			RubyModule t= (RubyModule) ancestors.get(i);

			if (t.callMethod("method_defined?", read_completed).isTrue() || t.callMethod("private_method_defined?", read_completed).isTrue()) {
			    ThreadContext c = runtime.getCurrentContext();
			    IRubyObject m = t.callMethod("instance_method", read_completed);
			    m.callMethod(c,"bind",o).callMethod(c, "call");
			}
		}
    }

    /**
     * Resolves a fully qualified class name like "Foo::Bar::Zot" to {@link RubyClass}.
     */
    private RubyClass resolveClass(String className) {
        RubyModule cur = runtime.getObject();
        for (String token : className.split("::")) {
            IRubyObject o = cur.getConstantAt(token);
            if (o instanceof RubyModule) {
                cur = (RubyModule) o;
                continue;
            }
            return null;    // undefined
        }

        if (cur instanceof RubyClass)
            return (RubyClass) cur;
        return null;
    }
}
