class B
  attr_reader :s
  private
  def read_completed()
    @s = "b"
  end
end

class D < B
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