package com.thatjessicaiknow.commonjs.jsapps

import org.querki.jquery.{$, JQuery, JQueryEventObject}
import org.scalajs.dom.document
import org.scalajs.dom.window
import org.scalajs.dom.raw.Location
import com.thatjessicaiknow.commonjs.common.QueryStringHelper.{getQueryString, mapToQueryString, queryStringToMap, removeQueryStringParam}
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

import scala.language.implicitConversions


object HomeApp {

  def app(): Unit = {
  
    implicit def jqSelectPicker(jq: JQuery): BootstrapSelectPicker = jq.asInstanceOf[BootstrapSelectPicker]
  
    val typeIdEqKey = "typeIdEq"
    
    val searchTermKey = "searchTerm"
  
    def $typeSelector = $("#type-selector")
    
    def $searchTerm = $("#search-term")
  
    $(document).ready { () =>
    
      val url = dom.window.location.href
    
      val queryMap = getQueryStringMap(url)
      
      queryMap.get(typeIdEqKey) match {
        case Some(value) =>
          $typeSelector.selectpicker("val",value)
        case None =>
          $typeSelector.selectpicker()
      }
      
      queryMap.get(searchTermKey) match {
        case Some(value) =>
          $searchTerm.value(value)
        case None =>
          ()
      }
    }
    
    $("form").on("submit", (e: JQueryEventObject, data: Any) => {
    
      e.preventDefault()
      
      val searchTerm = $searchTerm.value().asInstanceOf[String]
      
      val url = dom.window.location.href
      
      val urlParts = url.split("\\?")
      
      if(urlParts.length == 2){
  
        val queryMap =
          if (searchTerm.nonEmpty)
            queryStringToMap(urlParts(1)) + (searchTermKey -> searchTerm)
          else
            queryStringToMap(urlParts(1)) - searchTermKey
  
        val newQueryString = mapToQueryString(queryMap)
  
        window.location.assign(urlParts(0) + "?" + newQueryString)
      }
      else {
  
        val queryMap = Map("searchTerm" -> searchTerm)
  
        val newQueryString = mapToQueryString(queryMap)
  
        window.location.assign(urlParts(0) + "?" + newQueryString)
      }
    })
  
    $typeSelector.on("change",(e: JQueryEventObject, data: Any) => {
      
      val typeIdDyn = $typeSelector.value()
      
      val typeIdStr = typeIdDyn.asInstanceOf[String]
      
      val url = dom.window.location.href

      val urlParts = url.split("\\?")

      if (urlParts.length == 2) {
  
        val queryMap =
          if (typeIdStr == "0")
            queryStringToMap(urlParts(1)) - "typeIdEq"
          else
            queryStringToMap(urlParts(1)) + ("typeIdEq" -> typeIdStr)

        val newQueryString = mapToQueryString(queryMap)

        window.location.assign(urlParts(0) + "?" + newQueryString)
      }
      else {
  
        val queryMap = Map("typeIdEq" -> typeIdStr)
  
        val newQueryString = mapToQueryString(queryMap)
  
        window.location.assign(urlParts(0) + "?" + newQueryString)
      }
    })
  
    def getQueryStringMap(fullUrl: String): Map[String,String] = {
    
      val urlParts = fullUrl.split("\\?")
    
      if(urlParts.length == 2)
        queryStringToMap(urlParts(1))
      else
        Map()
    }
  }
  
  /*
  implicit class SelectPicker(val self: JQuery) extends AnyVal {
    
    def selectpicker(): self.type = {
      self
    }
  
    def selectpicker(refresh: String): self.type = {
      self
    }
  
    def selectpicker(value: String,newValue: String): self.type = {
      self
    }
  }
  
  js.Dynamic.global.jQuery.fn.selectpicker = { (self: JQuery) =>
    self
  }: js.ThisFunction
  */
}

