package com.thatjessicaiknow.commonjs.common

object Slugify {
  
  def apply(input: String): String = {
  
    val specialChars = "àáâäæãåāăąçćčđďèéêëēėęěğǵḧîïíīįìłḿñńǹňôöòóœøōõṕŕřßśšşșťțûüùúūǘůűųẃẍÿýžźż·/_,:;"
    val replacement  = "aaaaaaaaaacccddeeeeeeeegghiiiiiilmnnnnooooooooprrsssssttuuuuuuuuuwxyyzzz------"
    
    input.toLowerCase().replace(' ','-').trim().map{ x =>
    
      if (specialChars.contains(x)) {
        val index = specialChars.indexOf(x)
        replacement.charAt(index).toString
      }
      else
        x.toString
    }
    .reduceLeftOption((a,b) => a + b).getOrElse("")
  }
}
