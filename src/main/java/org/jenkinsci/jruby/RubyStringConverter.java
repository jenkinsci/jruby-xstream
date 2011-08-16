package org.jenkinsci.jruby;

import org.jruby.Ruby;
import org.jruby.RubyString;

/**
 * @author Kohsuke Kawaguchi
 */
public class RubyStringConverter extends AbstractRubyPrimitiveValueConverter<RubyString> {
    public RubyStringConverter(RubyRuntimeResolver resolver) {
        super(resolver, RubyString.class);
    }

    @Override
    protected String toString(RubyString obj) {
        return obj.toString();
    }

    @Override
    public RubyString fromString(Ruby runtime, String str) {
        return RubyString.newString(runtime,str);
    }
}
