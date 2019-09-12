package com.thatjessicaiknow.shared.crm

import java.util.UUID

import com.thatjessicaiknow.shared.common.Repo._

object Crm {

  final case class CustomerId(value: UUID) extends Id[UUID]

  final case class Customer(

    id   : CustomerId,
    name : String

  ) extends Entity[CustomerId]


  sealed trait CustomerCriteria[A] extends Criteria[A]

  final case class CustomerIdEq     (value: CustomerId) extends CustomerCriteria[CustomerId]
  final case class CustomerIdNotEq  (value: CustomerId) extends CustomerCriteria[CustomerId]
  final case class CustomerNameEq   (value: String)     extends CustomerCriteria[String]
  final case class CustomerNameNotEq(value: String)     extends CustomerCriteria[String]
  final case class CustomerIdIn     (value: Seq[CustomerId]) extends CustomerCriteria[Seq[CustomerId]]


  sealed trait CustomerSort extends Sort

  case object CustomerIdAsc    extends CustomerSort
  case object CustomerIdDesc   extends CustomerSort
  case object CustomerNameAsc  extends CustomerSort
  case object CustomerNameDesc extends CustomerSort

  trait CustomerRepo extends Repo[CustomerId,Customer,CustomerCriteria[_],CustomerSort]


  final case class ContactId(value: UUID) extends Id[UUID]

  final case class Contact(

    id         : ContactId,
    name       : String,
    customerId : Option[CustomerId] = None

  ) extends Entity[ContactId]

  sealed trait ContactCriteria[A] extends Criteria[A]

  final case class ContactIdEq     (value: ContactId) extends ContactCriteria[ContactId]
  final case class ContactIdNotEq  (value: ContactId) extends ContactCriteria[ContactId]
  final case class ContactNameEq   (value: String)    extends ContactCriteria[String]
  final case class ContactNameNotEq(value: String)    extends ContactCriteria[String]


  sealed trait ContactSort extends Sort

  case object ContactIdAsc  extends ContactSort
  case object ContactIdDesc extends ContactSort

  trait ContactRepo extends Repo[ContactId,Contact,ContactCriteria[_],ContactSort]
}
