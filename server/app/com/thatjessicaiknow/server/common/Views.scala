package com.thatjessicaiknow.server.common

import play.api.mvc.Call


object Views {
  
  sealed trait View {
    val call: Call
  }
  
  final case class Create(call: Call) extends View
  final case class Update(call: Call) extends View
  final case class Delete(call: Call) extends View
  
}
