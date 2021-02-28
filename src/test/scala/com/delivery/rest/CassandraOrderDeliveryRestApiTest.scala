package com.delivery.rest

import java.sql.Timestamp

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.delivery.domain.OrderDeliveryDomain.{Assignment, Courier, Order}
import com.delivery.spark.SparkCassandraContext
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.syntax._
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration._

/**
 * Created by ppani on 28/02/21
 */
class CassandraOrderDeliveryRestApiTest extends FlatSpec with Matchers with ScalatestRouteTest {
  implicit val timeout: RouteTestTimeout = RouteTestTimeout(10.seconds)

  override def afterAll(): Unit = SparkCassandraContext.spark.stop()
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
      "/v2/couriers",
      HttpEntity(ContentTypes.`application/json`,
        testCourier.asJson.noSpaces)
    ) ~> CassandraOrderDeliveryRestAPI.routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Courier] shouldBe testCourier.copy(courier_id = responseAs[Courier].courier_id)
    }

    Post(
      "/v2/couriers",
      HttpEntity(ContentTypes.`application/json`,
        testCourier.asJson.noSpaces)
    ) ~> CassandraOrderDeliveryRestAPI.routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Courier] shouldBe testCourier.copy(courier_id = responseAs[Courier].courier_id)
    }
  }

  it should "update entity via PUT request" in {
    val resp = Get("/v2/couriers") ~> CassandraOrderDeliveryRestAPI.routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[List[Courier]]
    }
      Put(
        s"/v2/couriers/${resp.head.courier_id}",
        HttpEntity(ContentTypes.`application/json`,
          testCourier.copy(zone = "S").asJson.noSpaces)
      ) ~> CassandraOrderDeliveryRestAPI.routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Courier] shouldBe testCourier.copy(courier_id = resp.head.courier_id,
          zone = "S")
      }
  }

  "Orders rest api" should "create order via POST request" in {
    Post(
      "/v2/orders",
      HttpEntity(ContentTypes.`application/json`,
        testOrder.asJson.noSpaces)
    ) ~> CassandraOrderDeliveryRestAPI.routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Order] shouldBe testOrder.copy(order_id = responseAs[Order].order_id)
    }

    Post(
      "/v2/orders",
      HttpEntity(ContentTypes.`application/json`,
        testOrder.asJson.noSpaces)
    ) ~> CassandraOrderDeliveryRestAPI.routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Order] shouldBe testOrder.copy(order_id = responseAs[Order].order_id)
    }
  }

  it should "update entity via PUT request" in {
    val resp = Get("/v2/orders") ~> CassandraOrderDeliveryRestAPI.routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[List[Order]]
    }
      Put(
        s"/v2/orders/${resp.head.order_id}",
        HttpEntity(ContentTypes.`application/json`,
          testOrder.copy(zone = "S").asJson.noSpaces)
      ) ~> CassandraOrderDeliveryRestAPI.routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[Order] shouldBe testOrder.copy(order_id = resp.head.order_id,
          zone = "S")
      }
  }

  it should "return entity not found by id via GET request" in {
    Get("/v2/orders/3") ~> Route.seal(CassandraOrderDeliveryRestAPI.routes) ~> check {
      status shouldEqual StatusCodes.NotFound
    }
  }

  "Assignments rest api" should "show right assignments via GET request" in {
    Get("/v2/assignments") ~> CassandraOrderDeliveryRestAPI.routes ~> check {
      println(responseAs[List[Assignment]])
      status shouldEqual StatusCodes.OK
      println(responseAs[List[Assignment]])
      responseAs[List[Assignment]].size > 0 shouldBe(true)
    }
  }

  "Retry Assignments rest api" should "show right assignments via GET request" in {
    Post(
      "/v2/couriers",
      HttpEntity(ContentTypes.`application/json`,
        testCourier.asJson.noSpaces)
    ) ~> CassandraOrderDeliveryRestAPI.routes ~> check {
      status shouldEqual StatusCodes.OK
      println(s"Unassigned Courier: ${responseAs[Courier]}")
      responseAs[Courier] shouldBe testCourier.copy(courier_id = responseAs[Courier].courier_id)
    }
    Get("/v2/retryAssignments") ~> CassandraOrderDeliveryRestAPI.routes ~> check {
      status shouldEqual StatusCodes.OK
      println(responseAs[List[Assignment]])
    }
    Get("/v2/couriers") ~> CassandraOrderDeliveryRestAPI.routes ~> check {
      status shouldEqual StatusCodes.OK
      println(responseAs[List[Courier]])
    }
  }

}
