package org.jenkinsci.jruby;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.jenkinsci.jruby.JRubyMapper.DynamicProxy;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.internal.runtime.methods.DynamicMethod;
import org.jruby.java.proxies.JavaProxy;
import org.jruby.javasupport.JavaUtil;
import org.jruby.javasupport.proxy.InternalJavaProxy;
import org.jruby.javasupport.proxy.JavaProxyClass;
import org.jruby.javasupport.proxy.JavaProxyInvocationHandler;
import org.jruby.javasupport.proxy.JavaProxyMethod;
import org.jruby.javasupport.util.RuntimeHelpers;
import org.jruby.runtime.Block;
import org.jruby.runtime.builtin.IRubyObject;

import java.lang.reflect.Field;

/**
 * Converts instances of Java classes that represent Ruby objects whose classes derive
 * from some existing Java class.
 *
 * @author Kohsuke Kawaguchi
 */
public class JavaProxyConverter implements Converter {
    private final Ruby runtime;
    private final ReflectionProvider reflectionProvider;
    private final AbstractReflectionConverter reflectionConverter;

    public JavaProxyConverter(Ruby runtime, XStream owner, AbstractReflectionConverter reflectionConverter) {
        this.runtime = runtime;
        this.reflectionConverter = reflectionConverter;
        this.reflectionProvider = owner.getReflectionProvider();
    }

    public boolean canConvert(Class type) {
        return InternalJavaProxy.class.isAssignableFrom(type) || DynamicProxy.class==type;
    }

    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext context) {
        InternalJavaProxy p = (InternalJavaProxy)o;
        IRubyObject base = p.___getInvocationHandler().getOrig();

        writer.startNode("ruby-object");
        context.convertAnother(base);
        writer.endNode();

        // marshal the Java portion of it, which comes from the base type
        reflectionConverter.marshal(o,writer,context);
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        reader.moveDown();
        JavaProxy original = (JavaProxy)context.convertAnother(null, IRubyObject.class);
        reader.moveUp();

        // get a fresh instance of Java object that this JavaProxy encapsulates
        JavaProxyClass realProxyClass = getProxyClass(original);
        Class realClass = realProxyClass.getJavaClass();
        Object javaObject = reflectionProvider.newInstance(realClass);

        original.setObject(javaObject);

        // TODO: JRuby needs to let us set the proxy in a more portable fashion
        try {
            Field h = realClass.getDeclaredField("__handler");
            h.setAccessible(true);
            h.set(javaObject, new InvocationHandlerImpl(original));
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        } catch (IllegalAccessException e) {
            throw new Error(e);
        }

        // unmarshal the Java portion that comes from the base type
        reflectionConverter.doUnmarshal(javaObject,reader,context);

        return javaObject;
    }

    /**
     * Given {@link JavaProxy} instance, which represents an instance of a Ruby class that
     * extends from a Java type, return {@link JavaProxyClass}, which encapsulates a generated
     * Java subtype that JRuby uses to represent this Ruby object.
     */
    private JavaProxyClass getProxyClass(JavaProxy original) {
        // taken from org.jruby.javasupport.Java. needs to be factored out from there
        IRubyObject proxyClass = original.getMetaClass().getInstanceVariables().fastGetInstanceVariable("@java_proxy_class");
        if (proxyClass == null || proxyClass.isNil()) {
            proxyClass = JavaProxyClass.get_with_class(original, original.getMetaClass());
            original.getMetaClass().getInstanceVariables().fastSetInstanceVariable("@java_proxy_class", proxyClass);
        }
        return (JavaProxyClass)proxyClass;
    }

    /**
     * JRuby doesn't expose its implementation of {@link JavaProxyInvocationHandler}, so copied here.
     *
     * TODO: request a change in JRuby to expose this.
     */
    private static class InvocationHandlerImpl implements JavaProxyInvocationHandler {
        private final IRubyObject self;
        private final Ruby runtime;

        private InvocationHandlerImpl(IRubyObject self) {
            this.self = self;
            this.runtime = self.getRuntime();
        }

        public IRubyObject getOrig() {
            return self;
        }

        public Object invoke(Object proxy, JavaProxyMethod m, Object[] nargs) throws Throwable {
            String name = m.getName();
            DynamicMethod method = self.getMetaClass().searchMethod(name);
            int v = method.getArity().getValue();
            IRubyObject[] newArgs = new IRubyObject[nargs.length];
            for (int i = nargs.length; --i >= 0; ) {
                newArgs[i] = JavaUtil.convertJavaToUsableRubyObject(runtime, nargs[i]);
            }

            IRubyObject result = null;
            if (v < 0 || v == (newArgs.length)) {
                result = method.call(runtime.getCurrentContext(), self, self.getMetaClass(), name, newArgs);
            } else if (m.hasSuperImplementation()) {
                RubyClass superClass = self.getMetaClass().getSuperClass();
                result = RuntimeHelpers.invokeAs(runtime.getCurrentContext(), superClass, self, name, newArgs, Block.NULL_BLOCK);
            } else {
                throw runtime.newArgumentError(newArgs.length, v);
            }
            if (m.getReturnType() == void.class) {
                return null;
            } else {
                return result.toJava(m.getReturnType());
            }
        }
    }
}
