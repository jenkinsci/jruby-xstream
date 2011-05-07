package org.jenkinsci.jruby;

import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import org.jruby.javasupport.proxy.InternalJavaProxy;

/**
 * @author Kohsuke Kawaguchi
 */
public class JRubyMapper extends MapperWrapper {
    public JRubyMapper(Mapper wrapped) {
        super(wrapped);
    }

    public String serializedClass(Class type) {
        if (InternalJavaProxy.class.isAssignableFrom(type)) {
            return ALIAS;
        } else {
            return super.serializedClass(type);
        }
    }

    public Class realClass(String elementName) {
        if (elementName.equals(ALIAS)) {
            return DynamicProxy.class;
        } else {
            return super.realClass(elementName);
        }
    }

    /**
     * Place holder type used for dynamic proxies.
     */
    public static class DynamicProxy {}

    private static final String ALIAS = "ruby-proxy-object";
}
