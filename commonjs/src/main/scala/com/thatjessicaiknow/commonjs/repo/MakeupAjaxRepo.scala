package com.thatjessicaiknow.commonjs.repo

import com.thatjessicaiknow.shared.makeup.Makeups.{Makeup, MakeupId, MakeupTypeId}
import org.scalajs.dom.ext.Ajax
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}

import scala.concurrent.{ExecutionContext, Future}

class MakeupAjaxRepo (implicit ec: ExecutionContext){

  implicit def makeupIdReads = Json.reads[MakeupId]
  
  implicit def makeupTypeIdReads = Json.reads[MakeupTypeId]
  
  implicit def makeupReads = Json.reads[Makeup]
  
  def findByType(typeId: String): Future[Seq[Makeup]] = {
  
    Ajax.get("").map { xhr =>
    
      val json = Json.parse(xhr.responseText)
      
      Json.fromJson[Seq[Makeup]](json) match {
  
        case JsSuccess(value, path) => value
        case JsError(errors)        => Seq()
      }
    }
  }
  
}
