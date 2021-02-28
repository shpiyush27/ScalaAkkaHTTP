package com.delivery.rest

import java.sql.Timestamp

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.delivery.domain.OrderDeliveryDomain.{Assignment, Courier, Order}
import io.circe.syntax._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration._

/**
 * Created by ppani on 28/02/21
 */
class InMemOrderDeliveryRestApiTest extends FlatSpec with Matchers with ScalatestRouteTest {
  implicit val timeout: RouteTestTimeout = RouteTestTimeout(10.seconds)

  import FailFastCirceSupport._
  import io.circe.generic.auto._

  val testOrder = Order(
    order_id = 1L,
    details = "Classic Chicken Burger",
    zone = "N",
    added_at = new Timestamp(System.currentTimeMillis()).toString
  )

  val testCourier = Courier(
    courier_id = 1L,
    name = "John",
    zone = "N",
    is_available = true)

  val testAssignment = Assignment(
    courier_id = 1,
    order_id = 1
  )

  "Couriers rest api" should "create courier via POST request" in {
    Post(
      "/v1/couriers",
      HttpEntity(ContentTypes.`application/json`,
        testCourier.asJson.noSpaces)
    ) ~> InMemOrderDeliveryRestAPI.routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Courier] shouldBe testCourier
    }

    Post(
      "/v1/couriers",
      HttpEntity(ContentTypes.`application/json`,
        testCourier.asJson.noSpaces)
    ) ~> InMemOrderDeliveryRestAPI.routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Courier] shouldBe testCourier.copy(courier_id = 2)
    }
  }

  it should "update entity via PUT request" in {
    Put(
      "/v1/couriers/2",
      HttpEntity(ContentTypes.`application/json`,
        testCourier.copy(zone = "S").asJson.noSpaces)
    ) ~> InMemOrderDeliveryRestAPI.routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Courier] shouldBe testCourier.copy(courier_id = 2,
        zone = "S")
    }
  }

  it should "return entity by id via GET request" in {
    Get("/v1/couriers/1") ~> InMemOrderDeliveryRestAPI.routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Courier] shouldBe testCourier
    }

    Get("/v1/couriers/3") ~> Route.seal(InMemOrderDeliveryRestAPI.routes) ~> check {
      status shouldEqual StatusCodes.NotFound
    }
  }

  it should "return all entities via GET request" in {
    Get("/v1/couriers") ~> InMemOrderDeliveryRestAPI.routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[List[Courier]] shouldBe
        testCourier :: testCourier.copy(courier_id = 2, zone = "S") :: Nil
    }
  }

  "Orders rest api" should "create order via POST request" in {
    Post(
      "/v1/orders",
      HttpEntity(ContentTypes.`application/json`,
        testOrder.asJson.noSpaces)
    ) ~> InMemOrderDeliveryRestAPI.routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Order] shouldBe testOrder
    }

    Post(
      "/v1/orders",
      HttpEntity(ContentTypes.`application/json`,
        testOrder.asJson.noSpaces)
    ) ~> InMemOrderDeliveryRestAPI.routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Order] shouldBe testOrder.copy(order_id = 2)
    }
  }

  it should "update entity via PUT request" in {
    Put(
      "/v1/orders/2",
      HttpEntity(ContentTypes.`application/json`,
        testOrder.copy(details = "Classic Egg Burger").asJson.noSpaces)
    ) ~> InMemOrderDeliveryRestAPI.routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Order] shouldBe testOrder.copy(order_id = 2,
        details = "Classic Egg Burger")
    }
  }

  it should "return entity by id via GET request" in {
    Get("/v1/orders/1") ~> InMemOrderDeliveryRestAPI.routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Order] shouldBe testOrder
    }

    Get("/v1/orders/3") ~> Route.seal(InMemOrderDeliveryRestAPI.routes) ~> check {
      status shouldEqual StatusCodes.NotFound
    }
  }

  it should "return all entities via GET request" in {
    Get("/v1/orders") ~> InMemOrderDeliveryRestAPI.routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[List[Order]] shouldBe
        testOrder :: testOrder.copy(order_id = 2, details = "Classic Egg Burger") :: Nil
    }
  }

  "Assignments rest api" should "show right assignments via GET request" in {
    Get("/v1/assignments") ~> InMemOrderDeliveryRestAPI.routes ~> check {
      println(responseAs[List[Assignment]])
      status shouldEqual StatusCodes.OK
      responseAs[List[Assignment]] shouldBe
        testAssignment :: testAssignment.copy(order_id = 2, courier_id = -1) :: Nil
    }
  }

  "Retry Assignments rest api" should "show right assignments via GET request" in {
    Post(
      "/v1/couriers",
      HttpEntity(ContentTypes.`application/json`,
        testCourier.asJson.noSpaces)
    ) ~> InMemOrderDeliveryRestAPI.routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Courier] shouldBe testCourier.copy(courier_id = 3)
    }
    Get("/v1/retryAssignments") ~> InMemOrderDeliveryRestAPI.routes ~> check {
      println(responseAs[List[Assignment]])
      status shouldEqual StatusCodes.OK
      responseAs[List[Assignment]] shouldBe
        testAssignment :: testAssignment.copy(order_id = 2, courier_id = 3) :: Nil
    }
    Get("/v1/couriers") ~> InMemOrderDeliveryRestAPI.routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[List[Courier]] shouldBe
        testCourier.copy(is_available = false) :: testCourier.copy(courier_id = 2, zone = "S") :: testCourier.copy(courier_id = 3 , is_available = false) :: Nil
    }
  }

}
