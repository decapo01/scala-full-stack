package com.thatjessicaiknow.server.common

import play.api.mvc.Request

object SortBy {


  def apply(req: Request[_],sort: String): String = {
  
    val order = req.getQueryString("order") match {
      case Some(o) => if(o == "asc") "desc" else "asc"
      case None    => "asc"
    }
    
    (req.queryString + ("sort" -> Seq(sort)) + ("order" -> Seq(order))).map {
  
      case (k,v) => s"$k=${v.head}"
    
    }.reduce(_+"&"+_)
  
    /*
    (req.getQueryString("sort"),req.getQueryString("order")) match {
      
      case (Some(s),Some(o)) if s == sort => {
        
        val orderItem = if(o == "asc") "desc" else "asc"
      
        (req.queryString + ("sort" -> Seq(s)) + ("order" -> Seq(orderItem))).map {
          case (k,v) => s"$k=${v.head}"
        
        }.reduce((a,b) => s"$a&$b")
      }

      case _ =>
        req
        .queryString
        .filter{ case (k,v) => k != "sort" }
        .filter{ case (k,v) => k != "order"}
        .map   { case (k,v) => k + "=" + v.head }
        .foldLeft("")(_ + "&" + _) + "sort=" + sort + "&order=asc"
    }
    */
  }
  
}
