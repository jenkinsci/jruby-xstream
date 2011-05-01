package org.jenkinsci.jruby;

import org.jruby.Ruby;
import org.jruby.RubyBoolean;

/**
 * @author Kohsuke Kawaguchi
 */
public class RubyBooleanConverter extends AbstractRubyPrimitiveValueConverter<RubyBoolean> {
    public RubyBooleanConverter(Ruby runtime) {
        super(runtime, RubyBoolean.class);
    }

    @Override
    protected RubyBoolean fromString(String value) {
        return RubyBoolean.newBoolean(runtime,value.equalsIgnoreCase("true"));
    }

    @Override
    protected String toString(RubyBoolean obj) {
        return obj.isTrue() ? "true" : "false";
    }
}
