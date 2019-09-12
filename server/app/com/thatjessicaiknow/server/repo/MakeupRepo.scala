package com.thatjessicaiknow.server.repo

import java.util.UUID

import com.thatjessicaiknow.shared.makeup.Makeup._
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

object MakeupRepo {
  
  trait MakeupComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  
    import profile.api._
    
    implicit lazy val makeupIdMapper = MappedColumnType.base[MakeupId,UUID](
      
      makeupId => makeupId.value,
      uuid     => MakeupId(uuid)
    )
    
    class MakeupTable(tag: Tag) extends Table[Makeup](tag,"makeups"){
    
      def id          = column[MakeupId]      ("id",O.PrimaryKey)
      def name        = column[String]        ("name")
      def description = column[Option[String]]("description")
      def rank        = column[Option[Int]]   ("rank")
      
      def * = (id,name,description,rank).mapTo[Makeup]
    }
    
    
    def buildQuery(item: MakeupTable,criteria: MakeupCriteria[_]): Rep[Boolean] = {
    
      criteria match {
  
        case MakeupIdEq(value) => item.id === value
        case MakeupIdNotEq(value) => item.id =!= value
        case Search(value)        => item.name.like(value.toLowerCase()) || item.description.like(value).getOrElse(false)
      }
    }
    
    def buildSort(item: MakeupTable,sort: MakeupSort): slick.lifted.Ordered = {
    
      sort match {
  
        case MakeupIdAsc  => item.id.asc
        case MakeupIdDesc => item.id.desc
        case NameAsc      => item.name.asc
        case NameDesc     => item.name.desc
        case RankAsc      => item.rank.asc
        case RankDesc     => item.rank.desc
      }
    }
  }
}
