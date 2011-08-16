package org.jenkinsci.jruby;

import org.jruby.Ruby;
import org.jruby.RubyBignum;

/**
 * @author Kohsuke Kawaguchi
 */
public class RubyIntegerConverter extends AbstractRubyPrimitiveValueConverter<RubyBignum> {
    public RubyIntegerConverter(RubyRuntimeResolver resolver) {
        super(resolver, RubyBignum.class);
    }

    @Override
    protected RubyBignum fromString(Ruby runtime, String value) {
        return RubyBignum.newBignum(runtime,value);
    }

    @Override
    protected String toString(RubyBignum obj) {
        return obj.getValue().toString();
    }
}
