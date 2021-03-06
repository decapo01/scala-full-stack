package com.thatjessicaiknow.server.repo

import java.util.UUID

import com.thatjessicaiknow.server.common.AbstractSlickRepo
import com.thatjessicaiknow.server.repo.MakeupTypeRepo.MakeupTypeComponent
import com.thatjessicaiknow.shared.common.Repo.Page
import com.thatjessicaiknow.shared.makeup.Makeups
import com.thatjessicaiknow.shared.makeup.Makeups.{MakeupCriteria,MakeupTypeCriteria,MakeupTypeId,MakeupId,Makeup,MakeupType,MakeupSort,MakeupRepo,MakeupTypeRepo}
import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}

object MakeupRepo {
  
  trait MakeupComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
    import profile.api._
    
    implicit lazy val makeupIdMapper = MappedColumnType.base[MakeupId,UUID](
      makeupId => makeupId.value,
      uuid     => MakeupId(uuid)
    )
    
    implicit lazy val typeIdMapper = MappedColumnType.base[MakeupTypeId,UUID](
      mId => mId.value,
      uuid => MakeupTypeId(uuid)
    )
    
    class MakeupTable(tag: Tag) extends Table[Makeup](tag,"makeups"){
      def id          = column[MakeupId]      ("id",O.PrimaryKey)
      def typeId      = column[MakeupTypeId]  ("type_id")
      def name        = column[String]        ("name")
      def slug        = column[String]        ("slug")
      def description = column[Option[String]]("description")
      def rank        = column[Option[Int]]   ("rank")
      def link        = column[Option[String]]("link")
      
      def * = (id,typeId,name,slug,description,rank,link).mapTo[Makeup]
    }
    
    implicit def buildQuery(item: MakeupTable,criteria: MakeupCriteria[_]): Rep[Boolean] = {
      criteria match {
        case Makeups.MakeupIdEq(value)           => item.id === value
        case Makeups.MakeupIdNotEq(value)        => item.id =!= value
        case Makeups.SlugEq(value)               => item.slug === value
        case Makeups.MakeupMakeupTypeIdEq(value) => item.typeId === value
        case Makeups.MakeupNameEq(value)         => item.name   === value
        case Makeups.Search(value)               => item.name.like(value.toLowerCase()) || item.description.like(value).getOrElse(false)
        case Makeups.MakeupRankEq(value)         => value.map { v => item.rank.map{ i => i === v }.getOrElse(false) }.getOrElse(false)
      }
    }
    
    def buildSort(item: MakeupTable, sort: MakeupSort): slick.lifted.Ordered = {
      sort match {
        case Makeups.MakeupIdAsc  => item.id.asc
        case Makeups.MakeupIdDesc => item.id.desc
        case Makeups.NameAsc      => item.name.asc
        case Makeups.NameDesc     => item.name.desc
        case Makeups.RankAsc      => item.rank.asc.nullsLast
        case Makeups.RankDesc     => item.rank.desc.nullsLast
      }
    }
  }
  
  class MakeupSlickRepo @Inject()(override val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends AbstractSlickRepo[MakeupId,Makeup,MakeupCriteria[_],MakeupSort](dbConfigProvider)
       with MakeupRepo
       with MakeupComponent
       with HasDatabaseConfigProvider[JdbcProfile] {
       
    import profile.api._
    
    import slick.lifted.TableQuery
       
    override type TableType = MakeupTable
  
    override def tableQuery: profile.api.TableQuery[TableType] = TableQuery[TableType]
  
    override def findByIdBaseQuery(id: MakeupId): profile.api.Query[MakeupTable, Makeup, Seq] =
      tableQuery.filter(_.id === id)
  }
  
  sealed trait MakeupViewSort
  case object IdAsc    extends MakeupViewSort
  case object IdDesc   extends MakeupViewSort
  case object NameAsc  extends MakeupViewSort
  case object NameDesc extends MakeupViewSort
  case object TypeAsc  extends MakeupViewSort
  case object TypeDesc extends MakeupViewSort
  case object RankAsc  extends MakeupViewSort
  case object RankDesc extends MakeupViewSort
  case object DescriptionAsc  extends MakeupViewSort
  case object DescriptionDesc extends MakeupViewSort
  
  
  trait MakeupViewRepo {
  
    def findBySlug(slug: String): Future[Option[(Makeup,MakeupType)]]
    def findById(makeupId: MakeupId): Future[Option[(Makeup,MakeupType)]]
    def findPage(makeupCriteria     : Seq[MakeupCriteria[_]]     = Seq(),
                 makeupTypeCriteria : Seq[MakeupTypeCriteria[_]] = Seq(),
                 searchTerm         : Option[String]             = None,
                 sort               : MakeupViewSort             = IdAsc,
                 limit              : Int                        = 10,
                 page               : Int                        = 1): Future[Page[(Makeup,MakeupType)]]
  }
  
  
  class MakeupViewSlickRepo @Inject()(override val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends MakeupViewRepo
    with    HasDatabaseConfigProvider[JdbcProfile]
    with    MakeupComponent
    with    MakeupTypeComponent {
    
    import profile.api._
    
    import slick.lifted.TableQuery
    val makeupTypes = TableQuery[MakeupTypeTable]
    val makeup = TableQuery[MakeupTable]
  
    override implicit lazy val makeupIdMapper = MappedColumnType.base[MakeupId,UUID](
      makeupId => makeupId.value,
      uuid     => MakeupId(uuid)
    )
  
    override implicit lazy val typeIdMapper = MappedColumnType.base[MakeupTypeId,UUID](
      mId => mId.value,
      uuid => MakeupTypeId(uuid)
    )
    
    val joinQuery =
      for {
        _makeup <- makeup
        _type   <- makeupTypes if _makeup.typeId === _type.id
      }
      yield {
        (_makeup,_type)
      }
      
    def findById(makeupId: MakeupId): Future[Option[(Makeup,MakeupType)]] = {
      val query = joinQuery.filter { case (_makeup,_) => _makeup.id === makeupId }
      db.run(query.result.headOption)
    }
    
    def findBySlug(slug: String): Future[Option[(Makeup,MakeupType)]] = {
      val query = joinQuery.filter { case (_makeup,_) => _makeup.slug === slug }
      db.run(query.result.headOption)
    }
    
    def findPage(makeupCriteria     : Seq[MakeupCriteria[_]]     = Seq(),
                 makeupTypeCriteria : Seq[MakeupTypeCriteria[_]] = Seq(),
                 searchTermOpt      : Option[String]             = None,
                 sort               : MakeupViewSort             = IdAsc,
                 limit              : Int                        = 10,
                 page               : Int                        = 1): Future[Page[(Makeup,MakeupType)]] = {
    
      val query = joinQuery.sortBy {
        case (_makeup,_type) => buildSort(_makeup,_type,sort)
      }
      .filter {
        case (_makeup,_type) =>
          makeupCriteria.map(c => buildQuery(_makeup,c)).reduceLeftOption(_ && _).getOrElse(LiteralColumn(true)) &&
          makeupTypeCriteria.map(c => buildQuery(_type,c)).reduceLeftOption(_ && _).getOrElse(LiteralColumn(true)) &&
          searchTermOpt.map { searchTerm =>
            _makeup.name.toLowerCase.like(s"%${searchTerm.toLowerCase()}%") ||
            _makeup.description.map(des => des.toLowerCase.like(s"%${searchTerm.toLowerCase()}%")).getOrElse(false) ||
            _type.name.toLowerCase.like(s"%${searchTerm.toLowerCase()}%")
          }.reduceLeftOption(_ && _).getOrElse(LiteralColumn(true))
      }
      
      val results = db.run(query.drop(1 - (limit * page)).take(limit).result)
      val total   = db.run(query.length.result)
      
      for {
        r <- results
        t <- total
      }
      yield {
        Page(r,t,limit,page)
      }
    }
      
    def buildSort(makeup: MakeupTable, _type: MakeupTypeTable, sort: MakeupViewSort): slick.lifted.Ordered = {
      sort match {
        case IdAsc           => makeup.id.asc
        case IdDesc          => makeup.id.desc
        case NameAsc         => makeup.name.asc
        case NameDesc        => makeup.name.desc
        case TypeAsc         => _type.name.asc
        case TypeDesc        => _type.name.desc
        case RankAsc         => makeup.rank.asc.nullsLast
        case RankDesc        => makeup.rank.desc.nullsLast
        case DescriptionAsc  => makeup.description.asc.nullsLast
        case DescriptionDesc => makeup.description.desc.nullsLast
      }
    }
    
  }
}
