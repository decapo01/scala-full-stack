package com.thatjessicaiknow.server.modules

import java.util.UUID

import com.thatjessicaiknow.server.providers.{HashProvider, UUIDProvider}
import com.thatjessicaiknow.shared.accounts.Accounts.{AdminRole, User, UserId, UserRepo}
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class DevStart  @Inject()(hashProvider: HashProvider,UUIDProvider: UUIDProvider,userRepo: UserRepo)(implicit ec: ExecutionContext){
  
  val userId = UserId(UUID.fromString("2aaa5f5f-a775-4686-b27a-441afe6cf465"))
  
  val pw = hashProvider.hashPassword("!Q2w3e4r5t")
  
  val user = User(userId,"blah@blah.com",pw)
  
  for {
    maybeUser <- userRepo.findById(userId)
    result    <-
      maybeUser match {
        case Some(user) => Future.successful(Unit)
        case None => userRepo.insert(user,AdminRole)
      }
  }
  yield {
    result
  }
  
}
