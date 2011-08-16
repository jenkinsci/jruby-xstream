package org.jenkinsci.jruby;

import org.jruby.Ruby;
import org.jruby.RubyBoolean;

/**
 * @author Kohsuke Kawaguchi
 */
public class RubyBooleanConverter extends AbstractRubyPrimitiveValueConverter<RubyBoolean> {
    public RubyBooleanConverter(RubyRuntimeResolver resolver) {
        super(resolver, RubyBoolean.class);
    }

    @Override
    protected RubyBoolean fromString(Ruby runtime, String value) {
        return RubyBoolean.newBoolean(runtime,value.equalsIgnoreCase("true"));
    }

    @Override
    protected String toString(RubyBoolean obj) {
        return obj.isTrue() ? "true" : "false";
    }
}
