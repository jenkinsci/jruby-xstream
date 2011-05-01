package org.jenkinsci.jruby;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import org.jruby.Ruby;
import org.jruby.RubyFixnum;

/**
 * @author Kohsuke Kawaguchi
 */
public class RubyFixnumConverter extends AbstractSingleValueConverter {
    private final Ruby runtime;

    public RubyFixnumConverter(Ruby runtime) {
        this.runtime = runtime;
    }

    public boolean canConvert(Class type) {
        return type==RubyFixnum.class;
    }

    @Override
    public RubyFixnum fromString(String str) {
        return RubyFixnum.newFixnum(runtime, Long.valueOf(str));
    }
}
