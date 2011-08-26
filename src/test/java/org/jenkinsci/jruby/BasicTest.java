package org.jenkinsci.jruby;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import junit.framework.TestCase;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyHash;
import org.jruby.RubyObject;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public class BasicTest extends TestCase {
    private ScriptingContainer jruby;
    private XStream xs;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        jruby = new ScriptingContainer(LocalContextScope.SINGLETHREAD);
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

    public void testProxyAndReadCompleted() {
        SomeJavaObject f = (SomeJavaObject)jruby.runScriptlet("require 'org/jenkinsci/jruby/proxy_read_completed'; Foo.new");
        SomeJavaObject r = roundtrip(f);
        assertEquals(1,r.x);
    }

    /**
     * Mixing & matching objects from multiple ruby VMs.
     */
    public void testCrossVM() {
        final ScriptingContainer r1 = new ScriptingContainer(LocalContextScope.SINGLETHREAD);
        final ScriptingContainer r2 = new ScriptingContainer(LocalContextScope.SINGLETHREAD);

        assertNotSame(r1.getProvider().getRuntime(), r2.getProvider().getRuntime());

        xs = new XStream() {
            @Override
            protected MapperWrapper wrapMapper(MapperWrapper next) {
                return new JRubyMapper(next);
            }
        };
        JRubyXStream.register(xs, new RubyRuntimeResolver() {
            @Override
            public Ruby unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
                switch(Integer.parseInt(reader.getAttribute("runtime"))) {
                case 0: return r1.getProvider().getRuntime();
                case 1: return r2.getProvider().getRuntime();
                default:    throw new AssertionError();
                }
            }

            @Override
            public void marshal(IRubyObject instance, HierarchicalStreamWriter writer, MarshallingContext context) {
                Ruby r = instance.getRuntime();
                int i;
                if (r==r1.getProvider().getRuntime())  i=0;
                else
                if (r==r2.getProvider().getRuntime())  i=1;
                else
                    throw new AssertionError();

                writer.addAttribute("runtime",String.valueOf(i));
            }
        });

        Map before = new HashMap();
        before.put("o1", r1.runScriptlet("Object"));
        before.put("o2", r2.runScriptlet("Object"));

        String xml = xs.toXML(before);
        System.out.println(xml);
        Map after = (Map)xs.fromXML(xml);

        assertSame(before.get("o1"),after.get("o1"));
        assertSame(before.get("o2"),after.get("o2"));
    }
}
