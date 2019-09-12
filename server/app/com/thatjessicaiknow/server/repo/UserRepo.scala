package com.thatjessicaiknow.server.repo

import java.util.UUID

import com.thatjessicaiknow.shared.accounts.Accounts._
import com.thatjessicaiknow.shared.common.Repo.Page
import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted

import scala.concurrent.{ExecutionContext, Future}

object UserRepos {

  trait UserComponent { self : HasDatabaseConfigProvider[JdbcProfile] =>

    import profile.api._

    implicit lazy val userIdMapper = MappedColumnType.base[UserId,UUID](
      userId => userId.value,
      uuid   => UserId(uuid)
    )


    class UserTable(tag: Tag) extends Table[User](tag,"users"){

      def id       = column[UserId]("id",O.PrimaryKey)
      def email    = column[String]("email")
      def password = column[String]("password")

      def * = (id,email,password).mapTo[User]
    }

    def makeQuery(item: UserTable,criteria: UserCriteria[_]): Rep[Boolean] = {

      criteria match {
        case IdEq(id)    => item.id === id
        case IdNotEq(id) => item.id =!= id
        case EmailEq(e)  => item.email === e
      }
    }

    def makeSort(item: UserTable, sort: UserSort): lifted.Ordered = {

      sort match {
        case IdAsc  => item.id.asc
        case IdDesc => item.id.desc
      }
    }
  }


  final case class RoleRow(userId: UserId,name: String)


  trait RoleComponent { self : HasDatabaseConfigProvider[JdbcProfile] =>

    import profile.api._

    implicit lazy val _userIdMapper = MappedColumnType.base[UserId,UUID](
      userId => userId.value,
      uuid   => UserId(uuid)
    )

    class RoleTable(tag: Tag) extends Table[RoleRow](tag,"roles"){

      def userId = column[UserId]("user_id")
      def name   = column[String]("name")

      def * = (userId,name).mapTo[RoleRow]
    }

    def mapToRole(roleRow: RoleRow): Role = {

      roleRow.name match {

        case name: String if name == "user"  => UserRole
        case name: String if name == "guest" => GuestRole
        case name: String if name == "admin" => AdminRole
        case _                               => GuestRole
      }
    }

    def mapFromRole(role: Role,userId: UserId): RoleRow = {

      role match {

        case UserRole  => RoleRow(userId,"user")
        case GuestRole => RoleRow(userId,"guest")
        case AdminRole => RoleRow(userId,"admin")
      }
    }
  }


  class UserSlickRepo @Inject()(

    override protected val dbConfigProvider: DatabaseConfigProvider
  )(

    implicit ec: ExecutionContext

  ) extends UserRepo with UserComponent with RoleComponent with HasDatabaseConfigProvider[JdbcProfile] {

    import profile.api._

    val users = TableQuery[UserTable]

    val roles = TableQuery[RoleTable]

    implicit lazy val __userIdMapper = MappedColumnType.base[UserId,UUID](
      userId => userId.value,
      uuid   => UserId(uuid)
    )


    def insert(user: User,role: Role): Future[Unit] = {

      val roleRow = mapFromRole(role,user.id)

      val insertUser = users += user

      val insertRole = roles += roleRow

      db.run(DBIO.seq(insertUser,insertRole).transactionally)
    }


    def update(user: User): Future[Unit] = {

      db.run(users.update(user)).map(_ => Unit)
    }


    def addRole(user: User, role: Role): Future[Unit] = {

      val roleRow = mapFromRole(role,user.id)

      db.run(roles += roleRow).map(_ => Unit)
    }


    def removeRole(userId: UserId, role: Role): Future[Unit] = {

      db.run(roles.filter(_.userId === userId).delete).map(_ => Unit)
    }


    def delete(userId: UserId): Future[Unit] = {

      db.run(users.filter(_.id === userId).delete).map(_ => Unit)
    }


    def filterQuery(criteria: Seq[UserCriteria[_]],userSort: UserSort = IdAsc): Query[UserTable,User,Seq] = {

      users.filter{ u =>
        criteria
        .map(c => makeQuery(u,c))
        .reduceLeftOption(_ && _)
        .getOrElse(LiteralColumn(true))
      }
      .sortBy(u => makeSort(u,userSort))
    }


    def findById(userId: UserId): Future[Option[(User,Seq[Role])]] = {

      for {
        userOpt <- db.run(filterQuery(Seq(IdEq(userId))).result.headOption)
        _roles  <-
          userOpt match {
            case Some(u) => db.run(roles.filter(_.userId === u.id).result)
            case None    => Future.successful(Seq())
          }
      }
      yield {

        userOpt match {
          case None    => None
          case Some(u) => Some((u,_roles.map(mapToRole)))
        }
      }
    }

    def findByCriteria(criteria: Seq[UserCriteria[_]]): Future[Option[(User,Seq[Role])]] = {

      for {

        _users  <- db.run(filterQuery(criteria).result.headOption)
        userIds = _users.map(_.id)
        _roles <- db.run(roles.filter(_.userId.inSet(userIds)).result)
      }
        yield {

          _users.map { u =>

            val rolesForUser = _roles.filter(_.userId == u.id).map(mapToRole)

            (u,rolesForUser)
          }

        }
    }


    def findAllByCriteria(criteria: Seq[UserCriteria[_]]): Future[Seq[(User,Seq[Role])]] = {

      for {

        _users  <- db.run(filterQuery(criteria).result)
        userIds = _users.map(_.id)
        _roles <- db.run(roles.filter(_.userId.inSet(userIds)).result)
      }
      yield {

        _users.map { u =>

          val rolesForUser = _roles.filter(_.userId == u.id).map(mapToRole)

          (u,rolesForUser)
        }

      }
    }

    def findTotal(criteria: Seq[UserCriteria[_]]): Future[Int] = {

      for {

        total <- db.run(filterQuery(criteria).length.result)
      }
      yield {

        total
      }
    }

    def findPage(criteria: Seq[UserCriteria[_]],limit: Int = 10, page: Int = 1, sort: UserSort = IdAsc): Future[Page[(User,Seq[Role])]] = {

      val usersFut = db.run(filterQuery(criteria,sort).drop(limit * (page - 1)).take(limit).result)

      val totalFut = findTotal(criteria)

      for {
        _users <- usersFut
        _roles <- db.run(roles.filter(_.userId.inSet(_users.map(u => u.id))).result)
        _total <- totalFut
      }
      yield {

        val userRoles = _users.map { u =>

          val _r = _roles.filter(_.userId == u.id).map(mapToRole)

          (u,_r)
        }

        Page(userRoles,_total,limit,page)
      }
    }
  }
}
