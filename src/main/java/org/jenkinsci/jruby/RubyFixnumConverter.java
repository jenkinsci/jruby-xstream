package org.jenkinsci.jruby;

import org.jruby.Ruby;
import org.jruby.RubyFixnum;

/**
 * @author Kohsuke Kawaguchi
 */
public class RubyFixnumConverter extends AbstractRubyPrimitiveValueConverter<RubyFixnum> {
    public RubyFixnumConverter(RubyRuntimeResolver resolver) {
        super(resolver, RubyFixnum.class);
    }

    @Override
    protected String toString(RubyFixnum obj) {
        return obj.toString();
    }

    @Override
    public RubyFixnum fromString(Ruby runtime, String str) {
        return RubyFixnum.newFixnum(runtime, Long.valueOf(str));
    }
}
