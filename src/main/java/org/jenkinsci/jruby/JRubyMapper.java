package org.jenkinsci.jruby;

import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import org.jruby.RubyBasicObject;
import org.jruby.javasupport.proxy.InternalJavaProxy;

/**
 * Set the proper alias for JRuby objects and proxy objects,
 * so that implementation detail class names won't leak out to the persisted form.
 *
 * @author Kohsuke Kawaguchi
 */
public class JRubyMapper extends MapperWrapper {
    public JRubyMapper(Mapper wrapped) {
        super(wrapped);
    }

    public String serializedClass(Class type) {
        if (type!=null && InternalJavaProxy.class.isAssignableFrom(type))
            return RUBY_PROXY;
        if (type!=null && RubyBasicObject.class.isAssignableFrom(type))
            return RUBY_OBJECT;
        else
            return super.serializedClass(type);
    }

    public Class realClass(String elementName) {
        if (elementName.equals(RUBY_PROXY))
            return DynamicProxy.class;
        if (elementName.equals(RUBY_OBJECT))
            return RubyBasicObject.class;
        else
            return super.realClass(elementName);
    }

    /**
     * Place holder type used for dynamic proxies.
     */
    public static class DynamicProxy {}

    private static final String RUBY_PROXY = "ruby-proxy-object";
    private static final String RUBY_OBJECT = "ruby-object";
}
