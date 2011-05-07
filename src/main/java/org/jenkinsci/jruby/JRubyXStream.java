package org.jenkinsci.jruby;

import com.thoughtworks.xstream.XStream;
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
    public static void register(XStream xs, Ruby runtime) {
        xs.registerConverter(new RubyStringConverter(runtime));
        xs.registerConverter(new RubyFixnumConverter(runtime));
        xs.registerConverter(new RubyIntegerConverter(runtime));
        xs.registerConverter(new RubyBooleanConverter(runtime));
        xs.registerConverter(new RubySymbolConverter(runtime));
        xs.registerConverter(new RubyArrayConverter(runtime));
        xs.registerConverter(new RubyHashConverter(runtime));
        xs.registerConverter(new JavaProxyConverter(runtime,xs));
        xs.registerConverter(new JRubyXStreamConverter(xs,runtime), XStream.PRIORITY_LOW);
    }

    public static void register(XStream xs, ScriptingContainer container) {
        register(xs,container.getProvider().getRuntime());
    }
}
