package com.thatjessicaiknow.commonjs.common

import org.querki.jquery.{$,JQuery}

object $$ {
  
  def apply(arg: String): Option[JQuery] = {
  
    val maybeElement = $(arg)
    
    Option(maybeElement)
  }
  
}
