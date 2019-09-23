package com.thatjessicaiknow.server.repo

import java.time.LocalDateTime
import java.util.UUID

import com.thatjessicaiknow.server.common.AbstractSlickRepo
import com.thatjessicaiknow.shared.accounts.Accounts._
import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

object LoginAttemptRepo {
  
  
  trait LoginAttemptComponent extends DateTimeMapper { self: HasDatabaseConfigProvider[JdbcProfile] =>
    
    import profile.api._
    
    implicit lazy val loginAttemptIdMapper = MappedColumnType.base[LoginAttemptId,UUID](
      bid  => bid.value,
      uuid => LoginAttemptId(uuid)
    )
    
    class LoginAttemptTable(tag: Tag) extends Table[LoginAttempt](tag,"login_attempts"){
      
      def id = column[LoginAttemptId] ("id",O.PrimaryKey)
      def ip = column[String]         ("ip")
      def time = column[LocalDateTime]("time")
      
      def * = (id,ip,time).mapTo[LoginAttempt]
    }
    
    def buildQuery(item: LoginAttemptTable,criteria: LoginAttemptCriteria[_]): Rep[Boolean] = {
      criteria match {
        case LoginAttemptIdEq(id) => item.id === id
        case LoginAttemptIpEq(ip) => item.ip === ip
      }
    }
    
    def buildSort(item: LoginAttemptTable,sort: LoginAttemptSort): slick.lifted.Ordered = {
      
      sort match {
        case LoginAttemptIdAsc => item.id.asc
      }
    }
  }
  
  class LoginAttemptSlickRepo @Inject()(override val dbConfigProvider: DatabaseConfigProvider) (implicit ec: ExecutionContext)
    
    extends AbstractSlickRepo[LoginAttemptId,LoginAttempt,LoginAttemptCriteria[_],LoginAttemptSort](dbConfigProvider)
      with    LoginAttemptRepo
      with    HasDatabaseConfigProvider[JdbcProfile]
      with    LoginAttemptComponent {
    
    import profile.api._
    
    override type TableType = LoginAttemptTable
    
    override def tableQuery: TableQuery[TableType] = TableQuery[TableType]
    
    override def findByIdBaseQuery(id: LoginAttemptId): Query[TableType, LoginAttempt, Seq] = {
      tableQuery.filter(_.id === id)
    }
    
  }
  
}
