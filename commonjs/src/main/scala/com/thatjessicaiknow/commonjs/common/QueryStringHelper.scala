package com.thatjessicaiknow.commonjs.common

import scala.util.matching.Regex

object QueryStringHelper {
  
  def updateQueryString(uri: String,key: String,value: String): String = {
  
    val pattern = "([?&])" + key + "=.*?(&|$)"
    
    val seperator = if(uri.contains("?")) "&" else "?"
    
    if (uri.matches(pattern))
      uri.replace(pattern,"$1" + key + "=" + "$2" + value)
    else
      uri + seperator + key + "=" + value
  }
}
