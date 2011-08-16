package org.jenkinsci.jruby;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyString;

/**
 * @author Kohsuke Kawaguchi
 */
public class RubyClassConverter extends AbstractRubyPrimitiveValueConverter<RubyClass> {
    public RubyClassConverter(RubyRuntimeResolver resolver) {
        super(resolver, RubyClass.class);
    }

    @Override
    protected String toString(RubyClass obj) {
        return obj.getName();
    }

    @Override
    public RubyClass fromString(Ruby runtime, String str) {
        return runtime.getClass(str);
    }
}
