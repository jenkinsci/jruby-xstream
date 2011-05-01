package org.jenkinsci.jruby;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import junit.framework.TestCase;
import org.jruby.Ruby;
import org.jruby.RubyObject;
import org.jruby.embed.ScriptingContainer;

/**
 * @author Kohsuke Kawaguchi
 */
public class BasicTest extends TestCase {
    private ScriptingContainer jruby;
    private XStream xs;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        jruby = new ScriptingContainer();
        xs = new XStream() {
            @Override
            protected MapperWrapper wrapMapper(MapperWrapper next) {
                return new JRubyMapper(next);
            }
        };
        Ruby runtime = jruby.getProvider().getRuntime();
        xs.registerConverter(new RubyStringConverter(runtime));
        xs.registerConverter(new RubyFixnumConverter(runtime));
        xs.registerConverter(new JRubyXStreamConverter(xs,runtime), XStream.PRIORITY_LOW);
    }

    public void test1() {
        RubyObject o = (RubyObject)jruby.runScriptlet("require 'org/jenkinsci/jruby/basicTest'; o = Foo.new; o.bar = Bar.new; o.bar.x='test'; o.bar.y=5; o.bar.foo=Foo.new; o");
        String xml = xs.toXML(o);
        System.out.println(xml);

        Object r = xs.fromXML(xml);
    }
}
