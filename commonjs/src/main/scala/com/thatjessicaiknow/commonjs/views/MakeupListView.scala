package com.thatjessicaiknow.commonjs.views

import com.thatjessicaiknow.shared.makeup.Makeups.Makeup
import scalatags.JsDom.all._

object MakeupListView {
  
  def apply(makeup: Seq[Makeup]) =
    table( `class` := "table table-bordered",
      thead(
        tr(
          th("Name"),
          th("Rank")
        )
      ),
      tbody(
        for(m <- makeup) yield {
          tr(
            td(m.name),
            td(m.rank.map(_.toString).getOrElse("").asInstanceOf[String])
          )
        }
      )
    )
}
