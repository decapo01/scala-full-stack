package com.thatjessicaiknow.shared.makeup

import java.util.UUID

import com.thatjessicaiknow.shared.common.Repo._

object Makeups {


  final case class MakeupTypeId(value: UUID) extends Id[UUID]
  
  final case class MakeupType(
    id: MakeupTypeId,
    name: String
  
  ) extends Entity[MakeupTypeId]
  
  
  sealed trait MakeupTypeCriteria[A] extends Criteria[A]
  
  final case class MakeupTypeIdEq(value: MakeupTypeId) extends MakeupTypeCriteria[MakeupTypeId]
  final case class MakeupTypeIdNotEq(value: MakeupTypeId) extends MakeupTypeCriteria[MakeupTypeId]
  final case class MakeupTypeNameEq(value: String)     extends MakeupTypeCriteria[String]
  final case class MakeupTypeSearch(value: String)     extends MakeupTypeCriteria[String]
  
  
  sealed trait MakeupTypeSort extends Sort
  
  final case object MakeupTypeIdAsc  extends MakeupTypeSort
  final case object MakeupTypeIdDesc extends MakeupTypeSort
  final case object MakeupTypeNameAsc    extends MakeupTypeSort
  final case object MakeupTypeNameDesc   extends MakeupTypeSort
  
  
  def mapTypeSort(sortStr: String): MakeupTypeSort = {
    
    sortStr match {
      case s: String if s == "idAsc"    => MakeupTypeIdAsc
      case s: String if s == "idDesc"   => MakeupTypeIdAsc
      case s: String if s == "nameAsc"  => MakeupTypeNameAsc
      case s: String if s == "nameDesc" => MakeupTypeNameDesc
      case _                            => MakeupTypeIdAsc
    }
  }
  
  
  trait MakeupTypeRepo extends Repo[MakeupTypeId,MakeupType,MakeupTypeCriteria[_],MakeupTypeSort]
  
  
  final case class MakeupId(value: UUID) extends Id[UUID]
  
  final case class Makeup(
  
    id          : MakeupId,
    typeId      : MakeupTypeId,
    name        : String,
    description : Option[String],
    rank        : Option[Int],
    
  ) extends Entity[MakeupId]
  
  
  sealed trait MakeupCriteria[A] extends Criteria[A]
  
  final case class MakeupIdEq(value: MakeupId) extends MakeupCriteria[MakeupId]
  final case class MakeupIdNotEq(value: MakeupId) extends MakeupCriteria[MakeupId]
  final case class Search(value: String) extends MakeupCriteria[String]
  
  
  sealed trait MakeupSort extends Sort
  
  case object MakeupIdAsc extends MakeupSort
  case object MakeupIdDesc extends MakeupSort
  case object NameAsc extends MakeupSort
  case object NameDesc extends MakeupSort
  case object RankAsc extends MakeupSort
  case object RankDesc extends MakeupSort
  
  trait MakeupRepo extends Repo[MakeupId,Makeup,MakeupCriteria[_],MakeupSort]
}
