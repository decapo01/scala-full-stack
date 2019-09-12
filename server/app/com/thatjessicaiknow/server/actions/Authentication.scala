package com.thatjessicaiknow.server.actions

import java.util.UUID

import com.thatjessicaiknow.shared.accounts.Accounts.{AdminRole, Role, UserRole}
import javax.inject.Inject
import play.api.mvc._
import play.api.mvc.Results.Forbidden

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object Authentication {

  object Names {
  
    val userId = "userId"
    
    val email = "email"
    
    val roles = "roles"
  }

  sealed trait AuthUser {
    val id    : UUID
    val email : String
    val roles : Seq[Role]
    
    def hasRoles(roles: Seq[Role]): Boolean
  }

  
  final case class LoggedInUser(id: UUID, email: String, roles: Seq[Role]) extends AuthUser {
  
    def hasRoles(roles: Seq[Role]): Boolean =
      this.roles.intersect(roles).nonEmpty
  }
  
  case object NoUser extends AuthUser {
  
    val id = UUID.fromString("85e76d6c-cd29-456d-a91b-6b9610d983bb")
    
    val email = ""
    
    val roles = Seq()
    
    def hasRoles(roles: Seq[Role]) = false
  }
  
  sealed trait AuthRequestAttempt[A] extends WrappedRequest[A] {
  
    val user: AuthUser
  
  }
  
  final case class FailedAttemptReq[A] (req: Request[A]) extends WrappedRequest[A](req) with AuthRequestAttempt[A]{
  
    val user: AuthUser = NoUser
  }
  
  final case class PassedAuthRequest[A](user: LoggedInUser,req: Request[A]) extends WrappedRequest[A](req) with AuthRequestAttempt[A]
  
  class AuthenticationAction @Inject()(val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext) extends ActionBuilder[AuthRequestAttempt,AnyContent] with ActionTransformer[Request,AuthRequestAttempt]{
    
  
    override protected def transform[A](request: Request[A]): Future[AuthRequestAttempt[A]] = {
    
      val userEmailOpt = request.session.get(Names.email)
      val userIdOpt    = request.session.get(Names.userId)
      val rolesOpt     = request.session.get(Names.roles)
      
      (userEmailOpt,userIdOpt,rolesOpt) match {
        case (Some(_email),Some(idStr),Some(rolesStr)) => {
          
          val loggedInUserAttempt = Try {
          
            val roleAttempts = parseRoles(rolesStr)
            
            val roles = roleAttempts.map(_.getOrElse(throw new RuntimeException("bad role")))
          
            val loggedInUser =
              LoggedInUser(
                id    = UUID.fromString(idStr),
                email = _email,
                roles = roles
              )
              
            loggedInUser
          }
          
          loggedInUserAttempt match {
            case Failure(e) => Future.successful(FailedAttemptReq(request))
            case Success(user) => Future.successful(PassedAuthRequest(user,request))
          }
        }
        case _ => Future.successful(FailedAttemptReq(request))
      }
    }
  }
  
  def parseRoles(value: String): Seq[Try[Role]] = {
    value.split(',').map(s => mapToRole(s))
  }
  
  def mapToRole(str: String): Try[Role] = {
    str match {
      case s: String if s == "user" => Success(UserRole)
      case s: String if s == "admin" => Success(AdminRole)
      case _ => Failure(new RuntimeException("bad role"))
    }
  }
  
  
  object AuthorizationAction {
    
    def apply(rolesAllowed: Role*)(implicit ec: ExecutionContext) = new ActionFilter[AuthRequestAttempt] {
  
      override def executionContext: ExecutionContext = ec
    
      def filter[A](input: AuthRequestAttempt[A]) = {
      
        input match {
          case FailedAttemptReq(_) => Future.successful(Some(Forbidden("Forbidden")))
          case PassedAuthRequest(user,req) =>
            if(user.hasRoles(rolesAllowed))
              Future.successful(None)
            else
              Future.successful(Some(Forbidden("Forbidden")))
        }
      }
    }
  }
}
