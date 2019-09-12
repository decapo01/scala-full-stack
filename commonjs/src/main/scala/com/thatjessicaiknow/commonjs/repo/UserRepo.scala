package com.thatjessicaiknow.commonjs.repo

import com.thatjessicaiknow.shared.accounts.Accounts
import com.thatjessicaiknow.shared.accounts.Accounts.{User, UserId, UserRepo}
import com.thatjessicaiknow.shared.common.Repo
import org.scalajs.dom.ext.Ajax
import play.api.libs.json.{JsResult, JsValue, Json}

import scala.concurrent.{ExecutionContext, Future}

class UserRepoAjax (implicit ec: ExecutionContext) {


  implicit def userIdReads = Json.reads[UserId]

  implicit def userReads = Json.reads[User]

  /*
  def findById(userId: UserId): Future[JsResult[User]] = {

    Ajax.get("").map { jsDictionary =>

      Json.fromJson[User](JsValue(jsDictionary))

    }
  }
  */

}

