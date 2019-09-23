package com.thatjessicaiknow.server.repo

import java.util.UUID

import com.thatjessicaiknow.server.common.AbstractSlickRepo
import com.thatjessicaiknow.shared.crm.Crm._
import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted

import scala.concurrent.ExecutionContext

object CrmRepo {

  sealed trait CustomerComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>

    import profile.api._

    implicit lazy val customerIdMapper = MappedColumnType.base[CustomerId,UUID](
      cId  => cId.value,
      uuid => CustomerId(uuid)
    )

    class CustomerTable(tag: Tag) extends Table[Customer](tag,"customers"){

      def id   = column[CustomerId]("id",O.PrimaryKey)
      def name = column[String]    ("name")

      def * = (id,name).mapTo[Customer]
    }

    def buildQuery(item: CustomerTable,criteria: CustomerCriteria[_]): Rep[Boolean] = {

      criteria match {
        case CustomerIdEq(value)      => item.id === value
        case CustomerIdNotEq(value)   => item.id =!= value
        case CustomerNameEq(value)    => item.name === value
        case CustomerNameNotEq(value) => item.name =!= value
        case CustomerIdIn(value)      => item.id.inSet(value)
      }
    }


    def buildSort(item: CustomerTable, sort: CustomerSort): lifted.Ordered = {

      sort match {
        case CustomerIdAsc    => item.id.asc
        case CustomerIdDesc   => item.id.desc
        case CustomerNameAsc  => item.name.asc
        case CustomerNameDesc => item.name.desc
      }
    }

  }


  class CustomerSlickRepo @Inject()(override val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends AbstractSlickRepo[CustomerId,Customer,CustomerCriteria[_],CustomerSort](dbConfigProvider)
  with    CustomerRepo
  with    CustomerComponent
  with    HasDatabaseConfigProvider[JdbcProfile]{

    import profile.api._

    override type TableType = CustomerTable

    override def tableQuery: TableQuery[TableType] = TableQuery[CustomerTable]

    override def findByIdBaseQuery(id: CustomerId): Query[TableType,Customer,Seq] =
      tableQuery.filter(_.id === id)
  }


  trait ContactComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>

    import profile.api._

    implicit lazy val contractIdMapper = MappedColumnType.base[ContactId,UUID](
      cId  => cId.value,
      uuid => ContactId(uuid)
    )

    implicit lazy val _customerIdMapper = MappedColumnType.base[CustomerId,UUID](
      cId  => cId.value,
      uuid => CustomerId(uuid)
    )

    class ContactTable(tag: Tag) extends Table[Contact](tag,"contacts"){

      def id         = column[ContactId]("id",O.PrimaryKey)
      def name       = column[String]("name")
      def customerId = column[Option[CustomerId]]("customer_id")

      def * = (id,name,customerId).mapTo[Contact]
    }

    def buildQuery(item: ContactTable,criteria: ContactCriteria[_]): Rep[Boolean] = {

      criteria match {
        case ContactIdEq(id)        => item.id   === id
        case ContactIdNotEq(id)     => item.id   =!= id
        case ContactNameEq(name)    => item.name === name
        case ContactNameNotEq(name) => item.name =!= name
      }
    }

    def buildSort(contacts: ContactTable, sort: ContactSort): slick.lifted.Ordered = {

       sort match {
         case ContactIdAsc  => contacts.id.asc
         case ContactIdDesc => contacts.id.desc
       }
    }
  }



  class ContactSlickRepo @Inject()(override val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends AbstractSlickRepo[ContactId,Contact,ContactCriteria[_],ContactSort](dbConfigProvider)
  with    ContactRepo
  with    ContactComponent
  with    HasDatabaseConfigProvider[JdbcProfile] {

    import profile.api._

    override type TableType = ContactTable

    override def tableQuery: TableQuery[TableType] = TableQuery[TableType]

    override def findByIdBaseQuery(id: ContactId): Query[TableType,Contact, Seq] =
      tableQuery.filter(_.id === id)

  }

}
