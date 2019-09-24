package com.thatjessicaiknow.server.controllers.api.v1.admin

import java.util.UUID

import com.thatjessicaiknow.server.actions.Authentication.{AuthenticationAction, AuthorizationAction}
import com.thatjessicaiknow.server.repo.MakeupRepo.MakeupViewRepo
import com.thatjessicaiknow.shared.accounts.Accounts.AdminRole
import com.thatjessicaiknow.shared.makeup.Makeups._
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

class AdminMakeupRestController @Inject()(
  controllerComponents: ControllerComponents,
  makeupTypeRepo: MakeupTypeRepo,
  makeupRepo: MakeupRepo,
  makeupViewRepo: MakeupViewRepo,
  authenticationAction: AuthenticationAction
  )(implicit executionContext: ExecutionContext)
  extends AbstractController(controllerComponents)
  with    I18nSupport {
  
  
  implicit def makeupIdWrites = Json.writes[MakeupId]
  
  implicit def makeupTypeIdWrites = Json.writes[MakeupTypeId]
  
  implicit def makeupWrites = Json.writes[Makeup]
  
  
  def index(typeIdOpt: Option[UUID] = None) = (authenticationAction andThen AuthorizationAction(AdminRole)).async { implicit req =>
  
    val typeIdCriteriaOpt = typeIdOpt.map(t => MakeupMakeupTypeIdEq(MakeupTypeId(t)))
  
    val criteria: Seq[MakeupCriteria[_]] = Seq(typeIdCriteriaOpt).filter(_.isDefined).map(_.get)
  
    for {
      makeups <- makeupRepo.findAll(criteria)
    }
    yield {
      Ok(Json.toJson(makeups))
    }
  }
  
}
