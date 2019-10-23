package com.thatjessicaiknow.commonjs.common

import scala.util.matching.Regex

object QueryStringHelper {
  
  def addQueryStringParam(uri: String,key: String,value: String): String = {
  
    val pattern = "([?&])" + key + "=.*?(&|$)"
  
    val seperator = if (uri.contains("?")) "&" else "?"
  
    if (uri.matches(pattern))
      uri.replace(pattern, "$1" + key + "=" + "$2" + value)
    else
      uri + seperator + key + "=" + value
  }
  
  def removeQueryStringParam(url: String,parameter: String): String = {
  
    val urlParts = url.split("\\?")
  
    if (urlParts.length == 2) {
    
      val qParts = urlParts(1).split("&")
    
      val newParts = qParts.filter(!_.contains(parameter))
    
      if (newParts.nonEmpty) urlParts(0) + "?" + newParts.mkString("&") else urlParts(0)
    }
    else
      url
  }
  
  def getQueryString(uri: String): Option[String] = {
  
    val urlParts = uri.split("//?")
    
    if (urlParts.length == 2)
      Some(urlParts(1))
    else
      None
  }
  
  def queryStringToMap(queryString: String): Map[String,String] = {
  
    if (queryString.contains("&"))
      queryString.split("&").map { itemPair =>
      
        val keyVal = itemPair.split("=")
        
        (keyVal(0),keyVal(1))
      }
      .toMap
    else {
  
      val keyVal = queryString.split("=")
  
      Seq((keyVal(0),keyVal(1))).toMap
    }
    
  }
  
  def mapToQueryString(map: Map[String,String]): String = {
    
    map.map { case (key,value) =>
      
      s"$key=$value"
    }
    .mkString("&")
  }
  
  
}
