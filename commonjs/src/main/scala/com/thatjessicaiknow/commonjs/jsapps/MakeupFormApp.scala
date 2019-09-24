package com.thatjessicaiknow.commonjs.jsapps

import com.thatjessicaiknow.commonjs.repo.MakeupAjaxRepo
import org.querki.jquery.{$, JQuery}
import org.scalajs.dom.document

import scala.concurrent.ExecutionContext

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

object MakeupFormApp {

  val makeupRepo = new MakeupAjaxRepo()


  def app(): Unit = {
  
    def typeSelector = $(".selectpicker")
  
    $(document).ready { () =>
      println("Hello from jquery")
  
      typeSelector.selectpicker()
  
      val id = typeSelector.value()
      
      makeupRepo.findByType("fe1dce98-ff56-473a-b171-2ce271533338").foreach(m => {
      
        println(m)
      })
  
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
