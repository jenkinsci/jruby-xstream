

module Transient
  def transient?(field)
    @transients[field.to_s] ? true : false
  end

  def transient(*fields)
    @transients ||= {}
    fields.each do |field|
      @transients[field.to_s] = true
    end
  end
end

class Point
  extend Transient
  attr_accessor :x, :y
  transient :x
end

