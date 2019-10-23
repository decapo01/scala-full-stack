package com.thatjessicaiknow.commonjs.jsapps

import com.thatjessicaiknow.commonjs.common.Slugify
import com.thatjessicaiknow.commonjs.repo.MakeupAjaxRepo
import org.querki.jquery.{$, JQuery}
import org.scalajs.dom.document

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import com.thatjessicaiknow.commonjs.views.MakeupListView
import com.thatjessicaiknow.shared.makeup.Makeups.Makeup

import scala.language.implicitConversions

object MakeupFormApp {

  val makeupRepo = new MakeupAjaxRepo()
  
  implicit def jqSelectPicker(jq: JQuery): BootstrapSelectPicker = jq.asInstanceOf[BootstrapSelectPicker]


  def app(): Unit = {
  
    def $typeSelector = $("#typeId_value")
    
    def $name = $("#name")
    
    def $slug = $("#slug")
    
    def $makeupTable = $("#makeup-table-container")
  
    $(document).ready { () =>
  
      $typeSelector.selectpicker()
  
      renderTable()
    }
    
    $typeSelector.on("change",() => {
      
      renderTable()
    })
  
    $name.on("keyup",() => {
    
      val name = $name.value.asInstanceOf[String]
    
      val slug = Slugify(name)
    
      $slug.value(slug)
    })
    
    $name.on("change",() => {
    
      val name = $name.value.asInstanceOf[String]
      
      val slug = Slugify(name)
      
      $slug.value(slug)
    })
    
    def renderTable(): Unit = {
  
      val id = $typeSelector.value()
  
      if(id != null) {
        makeupRepo.findByType(id.asInstanceOf[String]).foreach(makeup => {
    
          $makeupTable.empty()
    
          $makeupTable.html(MakeupListView.apply(makeup).toString())
        })
      }
    }
  }
  
  /*
  implicit class SelectPicker(val self: JQuery) extends AnyVal {
    
    def selectpicker(): self.type = {
      self
    }
  }
  
  js.Dynamic.global.jQuery.fn.selectpicker = { (self: JQuery) =>
    self
  }: js.ThisFunction
  */
}
