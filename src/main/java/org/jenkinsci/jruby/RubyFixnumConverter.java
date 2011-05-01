package org.jenkinsci.jruby;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import org.jruby.Ruby;
import org.jruby.RubyFixnum;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * @author Kohsuke Kawaguchi
 */
public class RubyFixnumConverter extends AbstractRubyPrimitiveValueConverter<RubyFixnum> {
    public RubyFixnumConverter(Ruby runtime) {
        super(runtime, RubyFixnum.class);
    }

    @Override
    protected String toString(RubyFixnum obj) {
        return obj.toString();
    }

    @Override
    public RubyFixnum fromString(String str) {
        return RubyFixnum.newFixnum(runtime, Long.valueOf(str));
    }
}
