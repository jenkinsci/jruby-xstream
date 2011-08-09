package org.jenkinsci.jruby;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import junit.framework.TestCase;
import org.jruby.RubyArray;
import org.jruby.RubyHash;
import org.jruby.RubyObject;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.ThreadContext;

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
        JRubyXStream.register(xs, jruby.getProvider().getRuntime());
    }

    public void test1() {
        RubyObject o = (RubyObject)jruby.runScriptlet("require 'org/jenkinsci/jruby/basic_test'\n o = Foo.new\n o.bar = Bar.new\n o.bar.x='test'; o.bar.y=5; o.bar.foo=Foo.new; o");
        String xml = xs.toXML(o);
        System.out.println(xml);
        Object r = xs.fromXML(xml);
    }

    public void testArray() {
        RubyArray before = (RubyArray)jruby.runScriptlet("[1,\"abc\",nil]");
        RubyArray after = roundtrip(before);

        assertEquals(before.length(), after.length());
        for (int i=0; i<before.getLength(); i++)
            assertEquals(before.entry(i), after.entry(i));
    }

    public void testHash() {
        RubyHash before = (RubyHash)jruby.runScriptlet("{ 1 => 5, \"foo\" => \"bar\", :abc => :def, \"d\" => [nil,nil]}");
        RubyHash after = roundtrip(before);

        assertTrue(before.op_equal(ThreadContext.newContext(jruby.getRuntime()),after).isTrue());
    }

    private <T> T roundtrip(T before) {
        String xml = xs.toXML(before);
        System.out.println(xml);
        return (T) xs.fromXML(xml);
    }

    public void testProxy() {
        Point before = (Point)jruby.runScriptlet("require 'org/jenkinsci/jruby/testProxy'; o=PointSubType.new; o.z=5; o");
        assertEquals(5,before.z());
        before.x = 1;
        before.y = 2;

        Point after = roundtrip(new Point[]{before})[0];
        System.out.println(before);
        System.out.println(after);

        assertEquals(5,after.z());
        assertEquals(1,after.x);
        assertEquals(2,after.y);
    }

    /**
     * Tests the class name resolution in a module.
     */
    public void testModule() {
        RubyObject o = (RubyObject)jruby.runScriptlet("require 'org/jenkinsci/jruby/testModule'; o = ModuleTest::Bar::Zot.new; o.a='test'; o");
        RubyObject r = roundtrip(o);
        assertEquals(o.getMetaClass(),r.getMetaClass());
    }

    public void testTransient() {
        RubyObject pt = (RubyObject)jruby.runScriptlet("require 'org/jenkinsci/jruby/transient'; pt=Point.new; pt.x=1; pt.y=2; pt");
        RubyObject r = roundtrip(pt);

        assertTrue(r.callMethod("x").isNil());
        assertEquals(2,r.callMethod("y").toJava(int.class));    // non-transient
    }

    public void testReadCompleted() {
        RubyObject f = (RubyObject)jruby.runScriptlet("require 'org/jenkinsci/jruby/read_completed'; F.new");
        RubyObject r = roundtrip(f);

        // verify the call order of read_completed
        assertEquals("bdf",r.callMethod("s").toJava(String.class));
	    assertEquals("y", r.callMethod("x").toJava(String.class));
    }
}
