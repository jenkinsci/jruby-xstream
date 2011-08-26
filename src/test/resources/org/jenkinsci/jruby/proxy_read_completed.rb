require 'java'

class Foo < org.jenkinsci.jruby.SomeJavaObject
  def read_completed
    # use self in the callback, to make sure by this time we have a fully constructed object
    java.lang.ref.WeakReference.new(self)

    setX(getX()+1)
    puts "read_completed called"
  end
end