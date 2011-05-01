package org.jenkinsci.jruby;

import org.jruby.Ruby;
import org.jruby.RubyBignum;

/**
 * @author Kohsuke Kawaguchi
 */
public class RubyIntegerConverter extends AbstractRubyPrimitiveValueConverter<RubyBignum> {
    public RubyIntegerConverter(Ruby runtime) {
        super(runtime, RubyBignum.class);
    }

    @Override
    protected RubyBignum fromString(String value) {
        return RubyBignum.newBignum(runtime,value);
    }

    @Override
    protected String toString(RubyBignum obj) {
        return obj.getValue().toString();
    }
}
