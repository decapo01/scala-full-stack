package com.thatjessicaiknow.server.controllers.api.v1

import java.util.UUID

import com.thatjessicaiknow.shared.common.Repo.Page
import com.thatjessicaiknow.shared.crm.Crm.{Customer, CustomerId, CustomerIdAsc, CustomerRepo}
import javax.inject.Inject
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, uuid}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

class CompanyApiV1Controller @Inject()(controllerComponents: ControllerComponents,customerRepo: CustomerRepo)(implicit ec: ExecutionContext)
extends AbstractController(controllerComponents)
with    I18nSupport {

  implicit def customerIdWrites = Json.writes[CustomerId]

  implicit def customerWrites = Json.writes[Customer]

  implicit def pageWrites = Json.writes[Page[Customer]]

  val form: Form[Customer] = Form[Customer](
    mapping(
      "id" -> mapping(
        "value" -> uuid
      )(CustomerId.apply)(CustomerId.unapply),
      "name" -> nonEmptyText
    )(Customer.apply)(Customer.unapply)
  )

  def index = Action.async {
    for {
      customers <- customerRepo.findPage(sort = CustomerIdAsc)
    }
    yield {
      Ok(Json.toJson(customers))
    }
  }

  def get(id: UUID) = Action.async {

    for {
      maybeCustomer <- customerRepo.findById(CustomerId(id))
    }
    yield {
      maybeCustomer match {
        case None => NotFound
        case Some(c) => Ok(Json.toJson(c))
      }
    }
  }

  def postInsert = Action.async { implicit req =>
    form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful{
          BadRequest(Json.toJson(formWithErrors.errorsAsJson))
        }
      },
      customer => {
         for {
          _ <- customerRepo.insert(customer)
         }
         yield {
           Ok(Json.toJson(customer))
         }
      }
    )
  }

  def postUpdate(id: UUID) = Action.async { implicit req =>

    for {
      maybeCustomer <- customerRepo.findById(CustomerId(id))
      res <-
        maybeCustomer match {
          case None => Future.successful(NotFound)
          case Some(c) => {
            form.bindFromRequest().fold(
              formWithErrors => {
                Future.successful{
                  BadRequest(Json.toJson(formWithErrors.errorsAsJson))
                }
              },
              customer => {
                for {
                  _ <- customerRepo.update(customer)
                }
                yield {
                  Ok(Json.toJson(customer))
                }
              }
            )
          }
        }
    }
    yield {
      res
    }
  }

  def postDelete(id: UUID) = Action.async {

    for {
      maybeCustomer <- customerRepo.findById(CustomerId(id))
      res <-
        maybeCustomer match {
          case None    => Future.successful(NotFound)
          case Some(c) => customerRepo.delete(CustomerId(id)).map { _ => Ok(Json.toJson(""))}
        }
    }
    yield {
      res
    }
  }
}
