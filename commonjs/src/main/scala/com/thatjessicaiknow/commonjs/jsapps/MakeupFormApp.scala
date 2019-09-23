package com.thatjessicaiknow.commonjs.jsapps

import org.querki.jquery.{$, JQuery}
import org.scalajs.dom.document

import scala.scalajs.js

object MakeupFormApp {


  def app(): Unit = {
  
    def typeSelector = $(".selectpicker")
  
    $(document).ready { () =>
      println("Hello from jquery")
  
      typeSelector.selectpicker()
  
      val id = typeSelector.value()
  
      println(id)
    }
  }
  
  implicit class SelectPicker(val self: JQuery) extends AnyVal {
    
    def selectpicker(): self.type = {
      self
    }
  }
  
  js.Dynamic.global.jQuery.fn.selectpicker = { (self: JQuery) =>
    self
  }: js.ThisFunction
}
