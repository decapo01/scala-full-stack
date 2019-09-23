package com.thatjessicaiknow.server.common

import play.api.mvc.Request

object SortBy {


  def apply(req: Request[_],sortAsc: String, sortDesc: String): String = {
  
    req.getQueryString("sort") match {
    
      case None => req.rawQueryString + "sort=" + sortAsc
    
      case Some(s) => {
      
        val sortItem = if(s == sortAsc) sortDesc else sortAsc
      
        (req.queryString + ("sort" -> Seq(sortItem))).map {
          case (k,v) => s"$k=${v.head}"
        
        }.reduce((a,b) => s"$a&$b")
      }
    }
  }
  
}
