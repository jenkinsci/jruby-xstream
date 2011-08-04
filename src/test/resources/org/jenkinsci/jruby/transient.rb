class Class
  def transient(*properties)
    properties.each do |p|
      transients[p.to_sym] = true
    end
  end

  def transient?(property)
    transients.keys.member?(property.to_sym) || (superclass < Class && superclass.transient?(property))
  end

  def transients
    @transients ||= {}
  end
end

class Point
  attr_accessor :x, :y
  transient :x
end

