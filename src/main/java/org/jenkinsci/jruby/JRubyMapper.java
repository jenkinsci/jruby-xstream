package org.jenkinsci.jruby;

import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import org.jruby.RubyBasicObject;

/**
 * @author Kohsuke Kawaguchi
 */
public class JRubyMapper extends MapperWrapper {
    public JRubyMapper(Mapper wrapped) {
        super(wrapped);
    }

    @Override
    public Class realClass(String elementName) {
        if (elementName.startsWith("ruby:"))
            return RubyBasicObject.class;

        return super.realClass(elementName);
    }
}
