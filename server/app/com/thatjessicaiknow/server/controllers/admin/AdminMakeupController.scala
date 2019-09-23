package com.thatjessicaiknow.server.controllers.admin

import java.util.UUID

import com.thatjessicaiknow.server.actions.Authentication.{AuthenticationAction, AuthorizationAction}
import com.thatjessicaiknow.server.common.Views.{Create, Delete, Update}
import com.thatjessicaiknow.server.providers.UUIDProvider
import com.thatjessicaiknow.server.repo.MakeupRepo.MakeupViewRepo
import com.thatjessicaiknow.shared.accounts.Accounts.AdminRole
import com.thatjessicaiknow.shared.makeup.Makeups._
import javax.inject.Inject
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, number, optional, text, uuid}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}
import views.html.admin.makeup.{makeupFormView => formView, makeupIndexView => indexView}

class AdminMakeupController @Inject()(
  makeupRepo: MakeupRepo,
  makeupViewRepo: MakeupViewRepo,
  makeupTypeRepo: MakeupTypeRepo,
  controllerComponents: ControllerComponents,
  UUIDProvider: UUIDProvider,
  authAction: AuthenticationAction)(implicit executionContext: ExecutionContext)
  
  extends AbstractController(controllerComponents)
  with    I18nSupport {
  
  
  val form = Form[Makeup](
    mapping(
      "id" -> mapping(
        "value" -> uuid
      )(MakeupId.apply)(MakeupId.unapply),
      "typeId" -> mapping(
        "value" -> uuid
      )(MakeupTypeId.apply)(MakeupTypeId.unapply),
      "name"        -> nonEmptyText,
      "description" -> optional(text),
      "rank"        -> optional(number)
    )(Makeup.apply)(Makeup.unapply)
  )
  
  def index = (authAction andThen AuthorizationAction(AdminRole)).async { implicit req =>
  
    for {
      viewItems <- makeupViewRepo.findPage()
    }
    yield {
      Ok(indexView(viewItems))
    }
  }
  
  
  def getCreate = (authAction andThen AuthorizationAction(AdminRole)).async { implicit req =>
    
    val action = com.thatjessicaiknow.server.controllers.admin.routes.AdminMakeupController.postCreate
    
    for {
      makeupTypes <- makeupTypeRepo.findAll()
    }
    yield {
    
      val types = makeupTypes.map(t => (t.id.value.toString,t.name))
      
      Ok(formView(form.fill(defaultMakeup),Create(action),types))
    }
  }
  
  def postCreate = (authAction andThen AuthorizationAction(AdminRole)).async { implicit req =>
  
    val action = com.thatjessicaiknow.server.controllers.admin.routes.AdminMakeupController.postCreate
    
    val _form = form.fill(defaultMakeup)
    
    Future.successful{
      Ok(formView(_form,Create(action)))
    }
  }
  
  def getUpdate(id: UUID) = (authAction andThen AuthorizationAction(AdminRole)).async { implicit req =>
  
    val action = com.thatjessicaiknow.server.controllers.admin.routes.AdminMakeupController.postCreate
  
    val _form = form.fill(defaultMakeup)
  
    Future.successful {
      Ok(formView(_form,Update(action)))
    }
  }
  
  def postUpdate(id: UUID) = (authAction andThen AuthorizationAction(AdminRole)).async { implicit req =>
  
    val action = com.thatjessicaiknow.server.controllers.admin.routes.AdminMakeupController.postCreate
  
    val _form = form.fill(defaultMakeup)
  
    Future.successful {
      Ok(formView(_form,Update(action)))
    }
  }
  
  def getDelete(id: UUID) = (authAction andThen AuthorizationAction(AdminRole)).async { implicit  req =>
  
    val action = com.thatjessicaiknow.server.controllers.admin.routes.AdminMakeupController.postCreate
  
    val _form = form.fill(defaultMakeup)
  
    Future.successful {
      Ok(formView(_form,Delete(action)))
    }
  }
  
  def postDelete(id: UUID) = (authAction andThen AuthorizationAction(AdminRole)).async { implicit req =>
  
    val action = com.thatjessicaiknow.server.controllers.admin.routes.AdminMakeupController.postCreate
  
    val _form = form.fill(defaultMakeup)
    
    Future.successful {
      Ok(formView(_form,Delete(action)))
    }
  }
  
  val defaultMakeupTypeId = MakeupTypeId(UUID.fromString("e8655706-a6b5-4045-85c8-0a57ff72c957"))
  
  def defaultMakeup =
    Makeup(
      id = MakeupId(UUIDProvider.randomUUID),
      typeId = defaultMakeupTypeId,
      name = "",
      description = None,
      rank = None
    )
}
