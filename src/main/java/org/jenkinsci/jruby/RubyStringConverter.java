package org.jenkinsci.jruby;

import org.jruby.Ruby;
import org.jruby.RubyString;

/**
 * @author Kohsuke Kawaguchi
 */
public class RubyStringConverter extends AbstractRubyPrimitiveValueConverter<RubyString> {
    public RubyStringConverter(Ruby runtime) {
        super(runtime, RubyString.class);
    }

    @Override
    protected String toString(RubyString obj) {
        return obj.toString();
    }

    @Override
    public RubyString fromString(String str) {
        return RubyString.newString(runtime,str);
    }
}
