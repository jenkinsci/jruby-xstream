module X
  attr_reader :x
  def read_completed
    @x = "y"
  end
end

class B
  attr_reader :s
  private
  def read_completed()
    @s = "b"
  end
end

class D < B
  include X
  private
  def read_completed()
    @s += "d"
  end
end

class F < D
  private
  def read_completed()
    @s += "f"
  end
end
