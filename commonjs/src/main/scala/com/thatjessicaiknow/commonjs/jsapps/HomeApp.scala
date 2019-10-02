package com.thatjessicaiknow.commonjs.jsapps

import org.querki.jquery.{$, JQuery, JQueryEventObject}
import org.scalajs.dom.document

import scala.scalajs.js

object HomeApp {

  def app(): Unit = {
  
    def $typeSelector = $(".selectpicker")
    
    def $searchTerm = $("#search-term")
  
    $(document).ready { () =>
    
      $typeSelector.selectpicker()
      
    }
    
    $(".form").on("submit", (e: JQueryEventObject, data: Any) => {
    
      e.preventDefault()
      
      val searchTerm = $searchTerm.value()
      
      
    })
  
    println("welcome to the home app")
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
