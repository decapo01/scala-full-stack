package com.thatjessicaiknow.shared.common

import scala.concurrent.Future

object Repo {

  trait Id[A] {

    val value: A
  }

  trait Entity[ID <: Id[_]] {

    val id: ID
  }

  trait Criteria[A] {

    val value: A
  }

  trait Sort

  case object IdAsc  extends Sort
  case object IdDesc extends Sort


  final case class Page[A](
    items: Seq[A],
    total: Int,
    limit: Int,
    page: Int
  )

  trait Repo[ID <: Id[_], ENTITY <: Entity[ID], CRITERIA <: Criteria[_], SORT <: Sort] {

    def insert(entity: ENTITY): Future[Int]

    def update(entity: ENTITY): Future[Int]

    def delete(id: ID): Future[Int]

    def findById(id: ID): Future[Option[ENTITY]]

    def findByCriteria(criteria: Seq[CRITERIA]): Future[Option[ENTITY]]

    def findAll(criteria: Seq[CRITERIA] = Seq()): Future[Seq[ENTITY]]

    //def findTotal(criteria: Seq[CRITERIA] = Seq()): Future[Int]

    def findPage(criteria: Seq[CRITERIA] = Seq(), limit: Int = 10, page: Int = 1, sort: SORT): Future[Page[ENTITY]]
  }

}
