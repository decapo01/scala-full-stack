package com.thatjessicaiknow.server.modules

import com.google.inject.AbstractModule
import com.thatjessicaiknow.server.repo.CrmRepo.{ContactSlickRepo, CustomerSlickRepo}
import com.thatjessicaiknow.server.repo.UserRepos.UserSlickRepo
import com.thatjessicaiknow.server.providers._
import com.thatjessicaiknow.server.repo.BlockedIpSlickRepo
import com.thatjessicaiknow.server.repo.LoginAttemptRepo.LoginAttemptSlickRepo
import com.thatjessicaiknow.server.repo.MakeupRepo.{MakeupSlickRepo, MakeupViewRepo, MakeupViewSlickRepo}
import com.thatjessicaiknow.server.repo.MakeupTypeRepo.MakeupTypeSlickRepo
import com.thatjessicaiknow.shared.accounts.Accounts.{BlockedIpRepo, LoginAttempt, LoginAttemptRepo, UserRepo}
import com.thatjessicaiknow.shared.crm.Crm.{ContactRepo, CustomerRepo}
import com.thatjessicaiknow.shared.makeup.Makeups.{MakeupRepo, MakeupTypeRepo}
import javax.inject.Inject

class DiModule @Inject() extends AbstractModule{

  override def configure(): Unit = {

    bind(classOf[UserRepo])        .to(classOf[UserSlickRepo])
    bind(classOf[UUIDProvider])    .to(classOf[UUIDProviderDefault])
    bind(classOf[CustomerRepo])    .to(classOf[CustomerSlickRepo])
    bind(classOf[ContactRepo])     .to(classOf[ContactSlickRepo])
    bind(classOf[DateTimeProvider]).to(classOf[DateTimeProviderImpl])
    bind(classOf[LoginAttemptRepo]).to(classOf[LoginAttemptSlickRepo])
    bind(classOf[BlockedIpRepo])   .to(classOf[BlockedIpSlickRepo])
    bind(classOf[MakeupTypeRepo])  .to(classOf[MakeupTypeSlickRepo])
    bind(classOf[HashProvider])    .to(classOf[HashProviderImpl])
    bind(classOf[MakeupRepo])      .to(classOf[MakeupSlickRepo])
    bind(classOf[MakeupViewRepo])  .to(classOf[MakeupViewSlickRepo])
  }
}
