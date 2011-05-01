package org.jenkinsci.jruby;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import org.jruby.Ruby;
import org.jruby.RubyString;

/**
 * @author Kohsuke Kawaguchi
 */
public class RubyStringConverter extends AbstractSingleValueConverter {
    private final Ruby runtime;

    public RubyStringConverter(Ruby runtime) {
        this.runtime = runtime;
    }

    public boolean canConvert(Class type) {
        return type==RubyString.class;
    }

    @Override
    public Object fromString(String str) {
        return RubyString.newString(runtime,str);
    }
}
