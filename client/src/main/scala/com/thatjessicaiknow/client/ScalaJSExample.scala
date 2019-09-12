package com.thatjessicaiknow.client

import java.util.UUID

import com.thatjessicaiknow.shared.accounts.Accounts.{User, UserId}
import com.thatjessicaiknow.shared.common.Repo.Page
import com.thatjessicaiknow.shared.crm.Crm.{Customer, CustomerId}
import com.thatjessicaiknow.shared.shared.SharedMessages
import org.scalajs.{dom => _dom}
import scalatags.JsDom.all._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.ReactDOM
import japgolly.scalajs.react.extra.router.StaticDsl.Route
import play.api.libs.json.Json
import com.thatjessicaiknow.commonjs.repo.TestCommonJs

case class State(msg: String)


object Data {

  val customers = Seq(
    Customer(CustomerId(UUID.randomUUID()),"ABC Construction"),
    Customer(CustomerId(UUID.randomUUID()),"Shoney's"),
  )
}

object Views {

  def customerTable(customers: Seq[Customer]) = {
      table( `class` := "table table-bordered",
        thead(
          tr(
            th("Id"),
            th("Name")
          )
        ),
        tbody(
          for(customer <- customers) yield {
            tr(
              td(customer.id.value.toString),
              td(customer.name)
            )
          }
        )
      )
  }
}

object ReactViews {

  def customerRows(customers: Seq[Customer]) = {
    for(customer <- customers) yield {
      <.tr(
        <.td(customer.id.value.toString),
        <.td(customer.name)
      )
    }
  }

  def customerTable(customers: Seq[Customer]) = {

    <.table(
      ^.cls :="table table-bordered",
      <.thead(
        <.tr(
          <.th("Id"),
          <.th("Name"))),
      <.tbody(
        customerRows(customers).toReactFragment
      )
    )
  }
}

sealed trait MyPage
case object Home                         extends MyPage
case object JustHello                    extends MyPage
case object CustomersPage                extends MyPage
case class  CustomerPage(id: CustomerId) extends MyPage


object Routes {

  val makeup    = "/makup"
  val makupById = makeup + "/:id"

  val makeupCategory     = "/makeup-categories"
  val makeupCategoryById = makeupCategory + "/:id"
}

object AppRouter {

  def layout(c: RouterCtl[MyPage], r: Resolution[MyPage]) =
    <.div(
      <.h1("Admin"),
      r.render()
    )

  implicit val baseUrl = BaseUrl.fromWindowOrigin

  def homeView =
    <.div(
      <.h1("Hello"),
      <.a(
        ^.href := "./#JustHello",
        "click here"
      )
    )

  val routerConfig = RouterConfigDsl[MyPage].buildConfig { dsl =>


    import dsl._

    val rootUrl = root + "/admin"

    (emptyRule
    | staticRoute(rootUrl      ,Home     ) ~> render(homeView)
    | staticRoute("/#JustHello",JustHello) ~> render(<.h1("Just hello"))
    )
    .notFound(redirectToPage(Home)(Redirect.Replace))
    .renderWith(layout)
  }

  val justHelloRoute = BaseUrl.fromWindowOrigin / "JustHello"

  val router = Router(baseUrl,routerConfig)
}

object ScalaJSExample {

  def main(args: Array[String]): Unit = {

    implicit def customerIdWrites = Json.reads[CustomerId]

    implicit def customerWrites = Json.reads[Customer]

    implicit def pageWrites = Json.reads[Page[Customer]]

    val userId = UserId(UUID.randomUUID())

    val user = User(userId, "blah@blah.com", "123453wkqkwlq")

    println(user.email)

    println(TestCommonJs.blah)

    //_dom.document.getElementById("scalajsShoutOut").textContent = SharedMessages.itWorks

    val container = _dom.document.getElementById("app")

    //container.appendChild(Views.customerTable(Data.customers).render)

    val noArgs =
      ScalaComponent
      .builder[Unit]("Hello")
      .renderStatic(<.div("hello"))
      .build

    val customerTableComponent =
      ScalaComponent
          .builder[Seq[Customer]]("Customers")
          .render_P((customers) => ReactViews.customerTable(customers))
          .build

    noArgs()

    customerTableComponent(Data.customers).renderIntoDOM(container)

    AppRouter.router().renderIntoDOM(container)
  }
}
