package com.thatjessicaiknow.server.repo

import java.util.UUID

import com.thatjessicaiknow.server.common.AbstractSlickRepo
import com.thatjessicaiknow.shared.accounts.Accounts._
import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext


trait BlockedIpComponent extends DateTimeMapper { self : HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._
  
  implicit lazy val blockedIpIdMapper = MappedColumnType.base[BlockedIpId,UUID](
    blockedIpId => blockedIpId.value,
    uuid        => BlockedIpId(uuid)
  )
  
  class BlockedIpTable(tag: Tag) extends Table[BlockedIp](tag,"blocked_ips"){
  
    def id = column[BlockedIpId]("id",O.PrimaryKey)
    def ip = column[String]     ("ip")
    
    def * = (id,ip).mapTo[BlockedIp]
  }
  
  def buildQuery(item: BlockedIpTable,criteria: BlockedIpCriteria[_]) = {
  
    criteria match {
      case BlockedIpIdEq(id) => item.id === id
      case BlockedIpIpEq(ip) => item.ip === ip
    }
  }
}


class BlockedIpSlickRepo @Inject()(override val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
extends AbstractSlickRepo[BlockedIpId,BlockedIp,BlockedIpCriteria[_],BlockedIpSort](dbConfigProvider)
with    BlockedIpRepo
with    BlockedIpComponent
with    HasDatabaseConfigProvider[JdbcProfile]{

  import profile.api._

  override type TableType = BlockedIpTable
  
  override def tableQuery: TableQuery[TableType] = TableQuery[TableType]
  
  override def findByIdBaseQuery(id: BlockedIpId): Query[TableType,BlockedIp,Seq] =
    tableQuery.filter{ _.id === id }
}