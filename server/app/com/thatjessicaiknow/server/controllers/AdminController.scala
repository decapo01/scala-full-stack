package com.thatjessicaiknow.server.controllers

import com.thatjessicaiknow.server.actions.Authentication.{AuthenticationAction, AuthorizationAction}
import com.thatjessicaiknow.shared.accounts.Accounts.{AdminRole, UserRole}
import javax.inject.Inject
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

class AdminController @Inject()(
  authenticationAction: AuthenticationAction,
  cc: ControllerComponents)(
  implicit ec: ExecutionContext) extends AbstractController(cc){


  def index = (authenticationAction andThen AuthorizationAction(AdminRole)){ req =>

    Ok(views.html.admin.adminIndex(req.user))
  }

}
