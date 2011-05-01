package org.jenkinsci.jruby;

import org.jruby.Ruby;
import org.jruby.RubySymbol;

/**
 * @author Kohsuke Kawaguchi
 */
public class RubySymbolConverter extends AbstractRubyPrimitiveValueConverter<RubySymbol> {
    public RubySymbolConverter(Ruby runtime) {
        super(runtime, RubySymbol.class);
    }

    @Override
    protected String toString(RubySymbol obj) {
        return obj.toString();
    }

    @Override
    public RubySymbol fromString(String str) {
        return RubySymbol.newSymbol(runtime,str);
    }
}
