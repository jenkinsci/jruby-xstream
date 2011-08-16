package org.jenkinsci.jruby;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import org.jruby.Ruby;
import org.jruby.embed.ScriptingContainer;

/**
 * Entry point to the JRuby/XStream support.
 * 
 * @author Kohsuke Kawaguchi
 */
public class JRubyXStream {
    /**
     * Registers all the Ruby-related converters to the given XStream.
     */
    public static void register(XStream xs, RubyRuntimeResolver resolver) {
        xs.registerConverter(new RubyStringConverter(resolver));
        xs.registerConverter(new RubyFixnumConverter(resolver));
        xs.registerConverter(new RubyIntegerConverter(resolver));
        xs.registerConverter(new RubyBooleanConverter(resolver));
        xs.registerConverter(new RubySymbolConverter(resolver));
        xs.registerConverter(new RubyArrayConverter(resolver));
        xs.registerConverter(new RubyHashConverter(resolver));
        xs.registerConverter(new RubyClassConverter(resolver));
        xs.registerConverter(new JavaProxyConverter(xs, new ReflectionConverter(xs.getMapper(),xs.getReflectionProvider())));
        xs.registerConverter(new JRubyXStreamConverter(xs,resolver), XStream.PRIORITY_LOW);
    }

    public static void register(XStream xs, Ruby runtime) {
        register(xs,RubyRuntimeResolver.of(runtime));
    }

    public static void register(XStream xs, ScriptingContainer container) {
        register(xs,container.getProvider().getRuntime());
    }
}
