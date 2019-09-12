package com.thatjessicaiknow.server.controllers.admin

import java.util.UUID

import javax.inject.Inject
import com.thatjessicaiknow.server.actions.Authentication.{AuthenticationAction, AuthorizationAction}
import com.thatjessicaiknow.server.providers.UUIDProvider
import com.thatjessicaiknow.shared.accounts.Accounts.{AdminRole, UserRole}
import com.thatjessicaiknow.shared.makeup.Makeup._
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, uuid}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}
import views.html.admin.{makeUpTypeFormView => formView, makeUpTypeIndexView => indexView}

class AdminMakeupTypeController @Inject()(
  authenticationAction : AuthenticationAction,
  makeupTypeRepo       : MakeupTypeRepo,
  UUIDProvider         : UUIDProvider,
  controllerComponents : ControllerComponents
)(
  implicit ec: ExecutionContext) extends AbstractController(controllerComponents)
                                    with I18nSupport {
                                    
                                    
  val form = Form(
    mapping(
      "id" -> mapping(
        "value" -> uuid
      )(MakeupTypeId.apply)(MakeupTypeId.unapply),
      "name" -> nonEmptyText
    )(MakeupType.apply)(MakeupType.unapply)
  )
  
  
  val indexRoute = com.thatjessicaiknow.server.controllers.admin.routes.AdminMakeupTypeController.index()
  
  val createRoute = com.thatjessicaiknow.server.controllers.admin.routes.AdminMakeupTypeController.postCreate()
  
  val updateRoute = (id: UUID) => com.thatjessicaiknow.server.controllers.admin.routes.AdminMakeupTypeController.postEdit(id)
  
  val deleteRoute = (id: UUID) => com.thatjessicaiknow.server.controllers.admin.routes.AdminMakeupTypeController.postDelete(id)
  
  def index = (authenticationAction andThen AuthorizationAction(AdminRole)).async { implicit req =>
    for {
      makeupTypePage <- makeupTypeRepo.findPage(sort = MakeupTypeIdAsc)
    }
    yield {
      Ok(indexView(makeupTypePage))
    }
  }
  
  
  def getCreate = (authenticationAction andThen AuthorizationAction(AdminRole)) { implicit req =>
    
    val id = UUIDProvider.randomUUID
    
    val makeupTypeId = MakeupTypeId(id)

    val makeupType = MakeupType(makeupTypeId,"")
    
    Ok(formView(form.fill(makeupType),createRoute))
  }
  
  def postCreate = (authenticationAction andThen AuthorizationAction(AdminRole)).async { implicit req =>
    
    form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(formView(formWithErrors,createRoute)))
      },
      makeupType => {
        
        for {
          makeupTypeOpt <- makeupTypeRepo.findByCriteria(Seq(MakeupTypeNameEq(makeupType.name)))
          res           <- makeupTypeOpt match {
  
            case Some(m) => Future.successful {
              BadRequest {
                formView (
                  form.fill(makeupType).withGlobalError("Makeup Type name exists.  Please choose a unique name."),
                  createRoute
                )
              }
            }
            case None    => makeupTypeRepo.insert(makeupType).map { _ =>
            
              Redirect(indexRoute).flashing("msg" -> "Type created")
            }
          }
        }
        yield {
          res
        }
      }
    )
  }
  
  def getEdit(id: UUID) = (authenticationAction andThen AuthorizationAction(AdminRole)).async { implicit req =>
    for {
      makeupTypeOpt <- makeupTypeRepo.findById(MakeupTypeId(id))
    }
    yield {
      makeupTypeOpt match {
        case None => NotFound("Not Found")
        case Some(m) => Ok(formView(form.fill(m),updateRoute(id)))
      }
    }
  }
  
  def postEdit(id: UUID) = (authenticationAction andThen AuthorizationAction(AdminRole)).async { implicit req =>
    form.bindFromRequest().fold(
    
      formWithErrors => {
        Future.successful(BadRequest(formView(formWithErrors,updateRoute(id))))
      },
      makeupType => {
        for {
          oldMakeupTypeOpt <- makeupTypeRepo.findById(MakeupTypeId(id))
          maybeDuplicateType <- makeupTypeRepo.findByCriteria(criteria = Seq(MakeupTypeIdNotEq(MakeupTypeId(id)),MakeupTypeNameEq(makeupType.name)))
          res <- (oldMakeupTypeOpt,maybeDuplicateType) match {
            case (None,_)    => Future.successful(NotFound("Not Founded"))
            case (_,Some(duplicate)) => Future.successful(
              BadRequest(
                formView(
                  form.fill(makeupType).withGlobalError("Makeup Type name exists.  Please choose a unique name."),
                  updateRoute(id)
                )
              )
            )
            case _ => makeupTypeRepo.update(makeupType).map { _ =>
              Redirect(indexRoute).flashing("msg" -> "Makeup Type Updated")
            }
          }
        }
        yield {
          res
        }
      }
    )
  }
  
  def getDelete(id: UUID) = (authenticationAction andThen AuthorizationAction(AdminRole)).async { implicit req =>
    
    for {
      maybeMakeupType <- makeupTypeRepo.findById(MakeupTypeId(id))
    }
    yield  {
      maybeMakeupType match {
        case None =>
          NotFound("Not Found")
        case Some(_type) =>
          Ok(formView(form.fill(_type).withGlobalError("Are you sure you want to delete this?"),deleteRoute(id)))
      }
    }
  }
  
  def postDelete(id: UUID) = (authenticationAction andThen AuthorizationAction(AdminRole)).async { implicit req =>
    
    for {
      _ <- makeupTypeRepo.delete(MakeupTypeId(id))
    }
    yield {
      Redirect(indexRoute).flashing("msg" -> "Makeup Type deleted")
    }
  }
  
}
