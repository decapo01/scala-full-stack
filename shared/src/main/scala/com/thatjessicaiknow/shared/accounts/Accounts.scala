package com.thatjessicaiknow.shared.accounts

import java.time.LocalDateTime
import java.util.UUID

import com.thatjessicaiknow.shared.common.Repo._

import scala.concurrent.Future

object Accounts {

  final case class UserId(value: UUID) extends Id[UUID]

  final case class User(

    id       : UserId,
    email    : String,
    password : String

  ) extends Entity[UserId]


  sealed trait UserCriteria[A] extends Criteria[A]

  final case class IdEq(value: UserId)    extends UserCriteria[UserId]
  final case class IdNotEq(value: UserId) extends UserCriteria[UserId]
  final case class EmailEq(value: String) extends UserCriteria[String]

  sealed trait UserSort extends Sort

  case object IdAsc  extends UserSort
  case object IdDesc extends UserSort

  //trait UserRepo extends Repo[UserId,User,UserCriteria[_],UserSort]


  sealed trait Role
  case object AdminRole extends Role
  case object UserRole  extends Role
  case object GuestRole extends Role


  trait UserRepo {

    def insert(user: User,role: Role): Future[Unit]

    def update(user: User): Future[Unit]

    def addRole(user: User, role: Role): Future[Unit]

    def removeRole(userId: UserId, role: Role): Future[Unit]

    def delete(userId: UserId): Future[Unit]

    def findById(userId: UserId): Future[Option[(User,Seq[Role])]]

    def findByCriteria(criteria: Seq[UserCriteria[_]]): Future[Option[(User,Seq[Role])]]

    def findAllByCriteria(criteria: Seq[UserCriteria[_]]): Future[Seq[(User,Seq[Role])]]

    def findTotal(criteria: Seq[UserCriteria[_]]): Future[Int]

    def findPage(criteria: Seq[UserCriteria[_]],limit: Int = 10, page: Int = 1, sort: UserSort = IdAsc): Future[Page[(User,Seq[Role])]]
  }

  final case class LoginAttemptId(value: UUID) extends Id[UUID]

  final case class LoginAttempt(

    id   : LoginAttemptId,
    ip   : String,
    time : LocalDateTime

  ) extends Entity[LoginAttemptId]

  sealed trait LoginAttemptCriteria[A] extends Criteria[A]

  final case class LoginAttemptIdEq(value: LoginAttemptId) extends LoginAttemptCriteria[LoginAttemptId]
  final case class LoginAttemptIpEq(value: String)         extends LoginAttemptCriteria[String]


  sealed trait LoginAttemptSort extends Sort

  case object LoginAttemptIdAsc extends LoginAttemptSort

  trait LoginAttemptRepo extends Repo[LoginAttemptId,LoginAttempt,LoginAttemptCriteria[_],LoginAttemptSort]


  final case class BlockedIpId(value: UUID) extends Id[UUID]

  final case class BlockedIp(

    id : BlockedIpId,
    ip : String

  ) extends Entity[BlockedIpId]

  sealed trait BlockedIpCriteria[A] extends Criteria[A]

  final case class BlockedIpIdEq(value: BlockedIpId) extends BlockedIpCriteria[BlockedIpId]
  final case class BlockedIpIpEq(value: String)      extends BlockedIpCriteria[String]

  sealed trait BlockedIpSort extends Sort

  case object BlockedIpIdAsc extends BlockedIpSort

  trait BlockedIpRepo extends Repo[BlockedIpId,BlockedIp,BlockedIpCriteria[_],BlockedIpSort]
}
