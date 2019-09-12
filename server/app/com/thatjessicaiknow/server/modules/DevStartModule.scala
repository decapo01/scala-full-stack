package com.thatjessicaiknow.server.modules

import java.util.UUID

import com.google.inject.AbstractModule
import com.thatjessicaiknow.server.providers.{HashProvider, UUIDProvider}
import com.thatjessicaiknow.shared.accounts.Accounts.{AdminRole, User, UserId, UserRepo}
import javax.inject.Inject
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{ExecutionContext, Future}

class DevStartModule  @Inject()() extends AbstractModule {
  
  override def configure(): Unit = {
  
    bind(classOf[DevStart]).asEagerSingleton()
  }
}
