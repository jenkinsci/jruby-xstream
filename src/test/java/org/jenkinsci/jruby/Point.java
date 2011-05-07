package org.jenkinsci.jruby;

/**
* @author Kohsuke Kawaguchi
*/
public abstract class Point {
    public int x, y;

    public abstract int z();
}
