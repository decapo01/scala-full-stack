package com.thatjessicaiknow.server.controllers.admin

import java.util.UUID

import com.sun.org.glassfish.gmbal.Description
import com.thatjessicaiknow.server.actions.Authentication.{AuthenticationAction, AuthorizationAction}
import com.thatjessicaiknow.server.common.Views.{Create, Delete, Update}
import com.thatjessicaiknow.server.providers.UUIDProvider
import com.thatjessicaiknow.server.repo.MakeupRepo.{DescriptionAsc, DescriptionDesc, MakeupViewRepo, MakeupViewSort, NameAsc, NameDesc, RankAsc, RankDesc, TypeAsc, TypeDesc}
import com.thatjessicaiknow.shared.accounts.Accounts.AdminRole
import com.thatjessicaiknow.shared.makeup.Makeups
import com.thatjessicaiknow.shared.makeup.Makeups.{Makeup, MakeupId, MakeupIdNotEq, MakeupMakeupTypeIdEq, MakeupNameEq, MakeupRankEq, MakeupRepo, MakeupTypeId, MakeupTypeIdEq, MakeupTypeRepo}
import javax.inject.Inject
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, number, optional, text, uuid}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}
import views.html.admin.makeup.{makeupFormView => formView, makeupIndexView => indexView}
import views.html.defaultpages.error

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
      "slug"        -> nonEmptyText,
      "description" -> optional(text),
      "rank"        -> optional(number(min=1)),
      "link"        -> optional(text(maxLength = 255))
    )(Makeup.apply)(Makeup.unapply)
  )
  
  def indexRoute = com.thatjessicaiknow.server.controllers.admin.routes.AdminMakeupController.index()
  
  def getCreateRoute = com.thatjessicaiknow.server.controllers.admin.routes.AdminMakeupController.postCreate()
  
  def postCreateRoute = com.thatjessicaiknow.server.controllers.admin.routes.AdminMakeupController.postCreate()
  
  def getUpdateRoute(id: UUID) = com.thatjessicaiknow.server.controllers.admin.routes.AdminMakeupController.getUpdate(id)
  
  def postUpdateRoute(id: UUID) = com.thatjessicaiknow.server.controllers.admin.routes.AdminMakeupController.postUpdate(id)
  
  def getDeleteRoute(id: UUID) = com.thatjessicaiknow.server.controllers.admin.routes.AdminMakeupController.getDelete(id)
  
  def postDeleteRoute(id: UUID) = com.thatjessicaiknow.server.controllers.admin.routes.AdminMakeupController.postDelete(id)
  
  def index(page: Option[Int] = None, limit: Option[Int] = None, sort: Option[String] = None, order: Option[String] = None) = (authAction andThen AuthorizationAction(AdminRole)).async { implicit req =>
  
    val _sort = (sort,order) match {
      case (Some(s),Some(o)) => makeSortFromString(s,o)
      case _ => NameAsc
    }
  
    for {
      viewItems <- makeupViewRepo.findPage(sort = _sort)
    }
    yield {
      Ok(indexView(req.user,viewItems))
    }
  }
  
  
  def getCreate = (authAction andThen AuthorizationAction(AdminRole)).async { implicit req =>
    
    for {
      makeupTypes <- makeupTypeRepo.findAll()
    }
    yield {
    
      val types = makeupTypes.map(t => (t.id.value.toString,t.name))
      
      Ok(formView(req.user,form.fill(defaultMakeup),Create(postCreateRoute),types))
    }
  }
  
  def postCreate = (authAction andThen AuthorizationAction(AdminRole)).async { implicit req =>
    
    form.bindFromRequest().fold(
      formWithErrors => {
        for {
          makeupTypes <- makeupTypeRepo.findAll()
        }
        yield {
          val types = makeupTypes.map(t => (t.id.value.toString,t.name))
  
          BadRequest(formView(req.user,formWithErrors,Create(getCreateRoute),types))
        }
      },
      makeup => {
        for {
          maybeMakeup <- makeupRepo.findByCriteria(Seq(MakeupNameEq(makeup.name)))
          res <-
            maybeMakeup match {
              case Some(_) =>
                val _form = form.fill(makeup).withGlobalError("Makeup with name currently exists")
  
                for {
                  makeupTypes <- makeupTypeRepo.findAll()
                }
                yield {
                  val types = makeupTypes.map(t => (t.id.value.toString,t.name))
                  
                  BadRequest(formView(req.user,_form,Create(getCreateRoute),types))
                }
              case None =>
                makeupRepo.insert(makeup).map { _ => Redirect(indexRoute) }
            }
        }
        yield {
          res
        }
      }
    )
  }
  
  def getUpdate(id: UUID) = (authAction andThen AuthorizationAction(AdminRole)).async { implicit req =>
    
    for {
      makeupOpt <- makeupRepo.findById(MakeupId(id))
      makeupTypes <- makeupTypeRepo.findAll()
    }
    yield {
      makeupOpt match {
        case None => NotFound
        case Some(makeup) =>
          val _form = form.fill(makeup)
          val _types = makeupTypes.map { t => (t.id.value.toString,t.name)}
          Ok(formView(req.user,_form,Update(postUpdateRoute(id)),_types))
      }
    }
  }
  
  def postUpdate(id: UUID) = (authAction andThen AuthorizationAction(AdminRole)).async { implicit req =>
  
    form.bindFromRequest().fold(
      errors => {
        for {
          makeupTypes <- makeupTypeRepo.findAll()
        }
        yield {
          val _types = makeupTypes.map { t => (t.id.value.toString,t.name)}
          BadRequest(formView(req.user,errors,Update(postUpdateRoute(id)),_types))
        }
      },
      makeup => {
        for {
          maybeExisting <- makeupRepo.findByCriteria(Seq(MakeupIdNotEq(makeup.id),MakeupNameEq(makeup.name)))
          maybeRanked   <- makeupRepo.findByCriteria(Seq(MakeupIdNotEq(makeup.id),MakeupMakeupTypeIdEq(makeup.typeId),MakeupRankEq(makeup.rank)))
          result <-
            (maybeExisting,maybeRanked) match {
              case (Some(_),_) =>
                for {
                  makeupTypes <- makeupTypeRepo.findAll()
                }
                yield {
                  val _types = makeupTypes.map { t => (t.id.value.toString,t.name)}
                  val _form  = form.fill(makeup).withGlobalError("Makeup with this name currently exists")
                  BadRequest(formView(req.user,_form,Update(postUpdateRoute(id)),_types))
                }
              case _ =>
                makeupRepo.update(makeup).map { _ => Redirect(indexRoute).flashing("msg" -> "Makeup Updated")}
            }
        }
        yield {
          result
        }
      }
    )
  }
  
  def getDelete(id: UUID) = (authAction andThen AuthorizationAction(AdminRole)).async { implicit  req =>
  
    for {
      maybeMakeup <- makeupRepo.findById(MakeupId(id))
      types       <- makeupTypeRepo.findAll()
    }
    yield {
      maybeMakeup match {
        case None => NotFound
        case Some(m) =>
          val _types = types.map { t => (t.id.value.toString,t.name)}
          val _form  = form.fill(m)
          
          Ok(formView(req.user,_form,Delete(postDeleteRoute(id)),_types))
      }
    }
  }
  
  def postDelete(id: UUID) = (authAction andThen AuthorizationAction(AdminRole)).async { implicit req =>
  
    for {
      _ <- makeupRepo.delete(MakeupId(id))
    }
    yield {
      Redirect(indexRoute).flashing("msg" -> "Makeup deleted")
    }
  }
  
  val defaultMakeupTypeId = MakeupTypeId(UUID.fromString("e8655706-a6b5-4045-85c8-0a57ff72c957"))
  
  def defaultMakeup =
    Makeup(
      id = MakeupId(UUIDProvider.randomUUID),
      typeId = defaultMakeupTypeId,
      name = "",
      slug = "",
      description = None,
      rank = None,
      link = None
    )
    
  def makeSortFromString(sortStr: String, orderStr: String): MakeupViewSort = {
    (sortStr,orderStr) match {
      case (s: String,o: String) if s == "name" && o == "asc"  => NameAsc
      case (s: String,o: String) if s == "name" && o == "desc" => NameDesc
      case (s: String,o: String) if s == "type" && o == "asc"  => TypeAsc
      case (s: String,o: String) if s == "type" && o == "desc" => TypeDesc
      case (s: String,o: String) if s == "rank" && o == "asc"  => RankAsc
      case (s: String,o: String) if s == "rank" && o == "desc" => RankDesc
      case (s: String,o: String) if s == "description" && o == "asc"   => DescriptionAsc
      case (s: String,o: String) if s == "description" && o == "desc"  => DescriptionDesc
      case _ => NameAsc
    }
  }
}
