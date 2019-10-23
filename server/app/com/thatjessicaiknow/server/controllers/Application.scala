package com.thatjessicaiknow.server.controllers

import java.util.UUID

import com.thatjessicaiknow.server.actions.Authentication
import com.thatjessicaiknow.server.actions.Authentication.{MaybeAuthAction, MaybeAuthReq}
import com.thatjessicaiknow.server.providers.{DateTimeProvider, HashProvider, UUIDProvider}
import com.thatjessicaiknow.shared.accounts.Accounts
import com.thatjessicaiknow.shared.accounts.Accounts._
import javax.inject._
import com.thatjessicaiknow.shared.shared.SharedMessages
import com.thatjessicaiknow.server.repo.{MakeupRepo, UserRepos}
import com.thatjessicaiknow.server.controllers.routes.Application
import com.thatjessicaiknow.server.repo.MakeupRepo.{MakeupViewRepo, MakeupViewSort}
import com.thatjessicaiknow.shared.makeup.Makeups.{MakeupCriteria, MakeupId, MakeupIdNotEq, MakeupMakeupTypeIdEq, MakeupRepo, MakeupTypeCriteria, MakeupTypeId, MakeupTypeIdEq, MakeupTypeRepo, RankAsc}
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.I18nSupport
import views.html.registerFormView
import views.html.loginView
import views.html.{makeupDetailView => detailView}

import scala.concurrent.{ExecutionContext, Future}

final case class LoginData(

  email    : String,
  password : String
)


class Application @Inject()(userRepo         : UserRepo,
                            loginAttemptRepo : LoginAttemptRepo,
                            blockedIpRepo    : BlockedIpRepo,
                            makeupViewRepo   : MakeupViewRepo,
                            makeupRepo       : MakeupRepo,
                            makeupTypeRepo   : MakeupTypeRepo,
                            uuidProvider     : UUIDProvider,
                            dateTimeProvider : DateTimeProvider,
                            cc               : ControllerComponents,
                            maybeAuthAction  : MaybeAuthAction,
                            hashProvider: HashProvider) (
                            implicit ec: ExecutionContext) extends AbstractController(cc)
                                                          with I18nSupport {


  val registerForm = Form[User](
    mapping(
      "id" -> mapping(
        "value" -> uuid
      )(UserId.apply)(UserId.unapply),
      "email" -> email,
      "password" -> nonEmptyText(minLength = 8,maxLength = 18)
    )(User.apply)(User.unapply)
  )

  val loginForm = Form[LoginData](
    mapping(
      "email"    -> email,
      "password" -> nonEmptyText
    )(LoginData.apply)(LoginData.unapply)
  )

  def index(typeIdEq   : Option[UUID]   = None,
            searchTerm : Option[String] = None,
            limit      : Option[Int]    = None,
            page       : Option[Int]    = None,
            sort       : Option[String] = None,
            order      : Option[String] = None) = maybeAuthAction.async { implicit req =>
  
    val makeupTypeCriteria: Seq[MakeupTypeCriteria[_]] = Seq (
      typeIdEq.map { typeId => MakeupTypeIdEq(MakeupTypeId(typeId)) }
    )
    .filter(m => m.isDefined)
    .map { m => m.get }
    
    val _sort = mapToSort(sort,order)
    
    for {
      makeupViews <- makeupViewRepo.findPage(makeupTypeCriteria = makeupTypeCriteria, searchTerm = searchTerm, limit = limit.getOrElse(100), page = page.getOrElse(1), sort = _sort)
      makeupTypes <- makeupTypeRepo.findAll()
    }
    yield {
  
      val user = User(UserId(UUID.randomUUID()),"email@email.com","password")
    
      Ok(views.html.index(SharedMessages.itWorks,req.maybeLoggedInUer,makeupViews,makeupTypes))
    }
  }
  
  def getDetail(slug: String) = maybeAuthAction.async { implicit req =>
    
    for {
      maybeMakeupView <- makeupViewRepo.findBySlug(slug)
      similarMakeups  <- maybeMakeupView match {
        case None => Future.successful(Seq())
        case Some((makeup,_type)) => makeupRepo.findAll(criteria = Seq(MakeupMakeupTypeIdEq(_type.id),MakeupIdNotEq(makeup.id)), sortOpt = Some(RankAsc))
      }
    }
    yield {
      maybeMakeupView match {
        case None => NotFound("Not found")
        case Some(makeupView) => Ok(detailView(req.maybeLoggedInUer,makeupView,similarMakeups))
      }
    }
  }

  def getRegister = Action { implicit req: Request[_] =>

    val userId: UserId = UserId(uuidProvider.randomUUID)

    val user: User = User(userId,"","")

    Ok(registerFormView(registerForm.fill(user)))
  }

  def postRegister = Action.async { implicit req =>


    registerForm.bindFromRequest().fold(

      formWithErrors => {
        Future.successful{

          BadRequest(registerFormView(formWithErrors))
        }
      },
      user => {

        for {
          maybeUser <- userRepo.findByCriteria(Seq(Accounts.EmailEq(user.email)))
          res <-
            maybeUser match {
              case Some(u) => Future.successful(
                BadRequest(registerFormView(registerForm.fill(user)))
              )
              case None    => userRepo.insert(user,UserRole).map { _ =>

                Redirect(routes.Application.index()).flashing(("msg","User Created"))
              }
            }
        }
        yield {

          res
        }
      }
    )
  }

  def getLogin = Action { implicit req =>

    def defaultLogin = LoginData("","")

    Ok(loginView(loginForm.fill(defaultLogin)))
  }

  def postLogin = Action.async { implicit req =>
    
    loginForm.bindFromRequest().fold(

      formWithErrors => {
        Future.successful{
          BadRequest(loginView(formWithErrors))
        }
      },
      loginData => {

        for {
          blockedIpOpt <- blockedIpRepo.findByCriteria(Seq(BlockedIpIpEq(req.remoteAddress)))
          userOpt <- userRepo.findByCriteria(Seq(EmailEq(loginData.email)))
          result  <-
          
            blockedIpOpt match {
              case Some(_) =>
              
                val _form = loginForm.fill(loginData).withGlobalError("There was a problem handling your request")
  
                Future.successful(BadRequest(loginView(_form)))
              case None =>
                userOpt match {
                  case None => {
                  
                    val loginAttemptId = LoginAttemptId(uuidProvider.randomUUID)
                    
                    val loginAttempt = LoginAttempt(loginAttemptId,req.remoteAddress,dateTimeProvider.now)
                  
                    val blockedIpId = BlockedIpId(uuidProvider.randomUUID)
                    
                    val blockedIp = BlockedIp(blockedIpId,req.remoteAddress)
                  
                    for {
                      _             <- loginAttemptRepo.insert(loginAttempt)
                      loginAttempts <- loginAttemptRepo.findAll(Seq(LoginAttemptIpEq(req.remoteAddress)))
                      _             <- if(loginAttempts.length >= 10) blockedIpRepo.insert(blockedIp) else Future.successful(0)
                    }
                    yield {
                      val _form = loginForm.fill(loginData).withGlobalError("There was a problem handling your request")
  
                      BadRequest(loginView(_form))
                    }
                  }
                  case Some((u,r)) => {
      
                    if (hashProvider.passwordMatches(loginData.password,u.password)) {
                    
                      val sessionData = Map(
                        Authentication.Names.userId -> u.id.value.toString,
                        Authentication.Names.email  -> u.email,
                        Authentication.Names.roles  -> r.map(_r => mapRoleToString(_r)).reduce(_ + "," + _)
                      )
                      
                      val session = Session(sessionData)
        
                      Future.successful(Redirect(routes.Application.index()).withSession(session))
                    }
                    else {
        
                      val loginAttemptId = LoginAttemptId(uuidProvider.randomUUID)
        
                      val ip = req.remoteAddress
        
                      val now = dateTimeProvider.now
        
                      val loginAttempt = LoginAttempt(loginAttemptId,req.remoteAddress,now)
        
                      for {
                        _             <- loginAttemptRepo.insert(loginAttempt)
                        loginAttempts <- loginAttemptRepo.findAll(Seq(LoginAttemptIpEq(ip)))
                        blockedIp      = BlockedIp(BlockedIpId(uuidProvider.randomUUID),ip)
                        _             <- if(loginAttempts.length >= 10) blockedIpRepo.insert(blockedIp) else Future.successful(0)
                      }
                      yield {
          
                        val _form = loginForm.fill(loginData).withGlobalError("There was a problem with your request")
          
                        BadRequest(loginView(_form))
                      }
                    }
                  }
                }
            }
        }
        yield {
          result
        }
      }
    )
  }
  
  def mapRoleToString(role: Role): String = {
    role match {
      case UserRole  => "user"
      case GuestRole => "guest"
      case AdminRole => "admin"
    }
  }
  
  def mapToSort(sortOpt: Option[String], orderOpt: Option[String]): MakeupViewSort = {
  
    (sortOpt,orderOpt) match {
      case (Some(sortStr),Some(orderStr)) if sortStr == "name" && orderStr == "asc" => MakeupRepo.NameAsc
      case (Some(sortStr),Some(orderStr)) if sortStr == "name" && orderStr == "desc" => MakeupRepo.NameDesc
      case (Some(sortStr),Some(orderStr)) if sortStr == "type" && orderStr == "asc" => MakeupRepo.TypeAsc
      case (Some(sortStr),Some(orderStr)) if sortStr == "type" && orderStr == "desc" => MakeupRepo.TypeDesc
      case (Some(sortStr),Some(orderStr)) if sortStr == "rank" && orderStr == "asc" => MakeupRepo.RankAsc
      case (Some(sortStr),Some(orderStr)) if sortStr == "rank" && orderStr == "desc" => MakeupRepo.RankDesc
      case _ => MakeupRepo.IdAsc
    }
  }
}
