package com.thatjessicaiknow.server.repo

import java.util.UUID

import com.thatjessicaiknow.server.common.AbstractSlickRepo
import com.thatjessicaiknow.shared.makeup.Makeups._
import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted

import scala.concurrent.ExecutionContext

object MakeupTypeRepo {


  trait MakeupTypeComponent {
    self: HasDatabaseConfigProvider[JdbcProfile] =>

    import profile.api._

    implicit lazy val makeupTypeIdMapper = MappedColumnType.base[MakeupTypeId, UUID](
      id => id.value,
      uuid => MakeupTypeId(uuid)
    )

    class MakeupTypeTable(tag: Tag) extends Table[MakeupType](tag, "makeup_types") {
      def id = column[MakeupTypeId]("id", O.PrimaryKey)
      def name = column[String]("name")

      def * = (id, name).mapTo[MakeupType]
    }

    def buildQuery(item: MakeupTypeTable, criteria: MakeupTypeCriteria[_]): Rep[Boolean] =
      criteria match {
        case MakeupTypeIdEq(id) => item.id === id
        case MakeupTypeIdNotEq(id) => item.id =!= id
        case MakeupTypeNameEq(name) => item.name === name
        case MakeupTypeSearch(searchTerm) => item.name.like(searchTerm.toLowerCase())
      }

    def buildSort(item: MakeupTypeTable, sort: MakeupTypeSort): lifted.Ordered =
      sort match {
        case MakeupTypeIdAsc => item.id.asc
        case MakeupTypeIdDesc => item.id.desc
        case MakeupTypeNameAsc => item.name.asc
        case MakeupTypeNameDesc => item.name.desc
      }
  }


  class MakeupTypeSlickRepo @Inject()(
    override val dbConfigProvider: DatabaseConfigProvider
  )
    (implicit ec: ExecutionContext) extends AbstractSlickRepo[MakeupTypeId, MakeupType, MakeupTypeCriteria[_], MakeupTypeSort](dbConfigProvider)
    with MakeupTypeRepo
    with MakeupTypeComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

    import profile.api._

    override type TableType = MakeupTypeTable

    override def tableQuery: profile.api.TableQuery[TableType] = lifted.TableQuery[TableType]

    override def findByIdBaseQuery(id: MakeupTypeId): Query[TableType, MakeupType, Seq] =
      tableQuery.filter(_.id === id)
  }

}
