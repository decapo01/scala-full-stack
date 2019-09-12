package com.thatjessicaiknow.server.common

import com.thatjessicaiknow.shared.common.Repo._
import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.relational.RelationalProfile

import scala.concurrent.{ExecutionContext, Future}


abstract class AbstractSlickRepo[ID <: Id[_],ENTITY <: Entity[ID],CRITERIA <: Criteria[_],SORT <: Sort] @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
extends HasDatabaseConfigProvider[JdbcProfile]
with    Repo[ID,ENTITY,CRITERIA,SORT]{

  import profile.api._

  type TableType <: RelationalProfile#Table[ENTITY]

  def tableQuery: TableQuery[TableType]

  def findByIdBaseQuery(id: ID): Query[TableType,ENTITY,Seq]

  def findAllQuery(criterion: Seq[CRITERIA]): Query[TableType,ENTITY,Seq] =
    tableQuery.filter{ items =>
      criterion.map(c => buildQuery(items,c))
        .reduceLeftOption(_ && _)
        .getOrElse(LiteralColumn(true))
    }

  def insert(entity: ENTITY): Future[Int] =
      db.run(tableQuery += entity)

  def update(entity: ENTITY): Future[Int] =
      db.run(findByIdBaseQuery(entity.id).update(entity))

  def delete(id: ID): Future[Int] =
      db.run(findByIdBaseQuery(id).delete)

  def findById(id: ID): Future[Option[ENTITY]] =
      db.run(findByIdBaseQuery(id).result.headOption)

  def findByCriteria(criterion: Seq[CRITERIA]): Future[Option[ENTITY]] =
      db.run(findAllQuery(criterion).result.headOption)

  def findAll(criterion: Seq[CRITERIA]): Future[Seq[ENTITY]] =
      db.run(findAllQuery(criterion).result)

  def findPage(criterion: Seq[CRITERIA] = Seq(),limit: Int = 10,page: Int = 1,sort: SORT): Future[Page[ENTITY]] = {

      val itemsFut = db.run(findAllQuery(criterion).drop(limit * (page - 1)).take(limit).result)
      val totalFut = db.run(findAllQuery(criterion).length.result)

      for {
          total <- totalFut
          items <- itemsFut
      }
      yield {

          Page(items = items, total = total, limit = limit, page = page)
      }
  }

  def buildQuery(item: TableType, criterion: CRITERIA): Rep[Boolean]


  //def pkType: BaseTypedType[ID]

  //implicit lazy val _pkType: BaseTypedType[ID] = pkType

  //  val mapTo : ID => IDTYPE
  //  val mapFrom : IDTYPE => ID

  //implicit def idType: BaseColumnType[IDTYPE]

  //  implicit def idMapper: BaseColumnType[ID] = MappedColumnType.base[ID,IDTYPE](
  //    mapTo,
  //    mapFrom
  //  )
}
