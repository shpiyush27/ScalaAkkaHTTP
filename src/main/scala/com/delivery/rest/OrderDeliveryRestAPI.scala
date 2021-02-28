package com.delivery.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.{Marshaller, ToResponseMarshaller}
import akka.http.scaladsl.server.{HttpApp, Route}
import cats.Id
import com.delivery.dao.InMemAssignmentDAO.InMemAssignmentRepository
import com.delivery.dao.InMemCourierDAO.InMemCourierRepository
import com.delivery.dao.InMemOrderDAO.InMemOrderRepository
import com.delivery.domain.OrderDeliveryDomain.{AssignmentOptional, Courier, CourierOptional, Order, OrderOptional}
import com.delivery.service.AssignmentService.AssignmentService
import com.delivery.service.CourierService.CourierService
import com.delivery.service.OrderService.OrderService
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.util.Random

object OrderDeliveryRestAPI extends HttpApp {
  implicit val system = ActorSystem("orderDeliveryApi")

  implicit def idMarshaller[A](implicit aMarshaller: ToResponseMarshaller[A])
  : ToResponseMarshaller[Id[A]] =
    Marshaller.oneOf(aMarshaller)

  val orderRepo = new InMemOrderRepository
  val orderService = new OrderService(orderRepo)
  val assignmentRepo = new InMemAssignmentRepository
  val assignmentService = new AssignmentService(assignmentRepo)
  val courierRepo = new InMemCourierRepository
  val courierService = new CourierService(courierRepo)


  import FailFastCirceSupport._
  import io.circe.generic.auto._


  override def routes: Route =
    pathPrefix("v1") {
      pathPrefix("orders") {
        rejectEmptyResponse {
          pathEndOrSingleSlash {
            get {
              complete {
                orderService.getAll()
              }
            } ~
              post {
                entity(as[Order]) { o =>
                  complete {
                    val order = orderService.newOrder(
                      details = o.details,
                      zone = o.zone,
                      added_at = o.added_at
                    )
                    val random_courier_gen = new Random
                    val availableCouriers = courierService.getAvailableByZone(o.zone)
                    val randomCourier = if (!availableCouriers.isEmpty) availableCouriers
                      .lift(random_courier_gen.nextInt(availableCouriers.length)) else None
                    val randomCourierId = if (randomCourier.isEmpty) -1L else randomCourier.get.courier_id
                    assignmentService.newAssignment(order.order_id, randomCourierId)
                    courierService.updateCourier(courier_id = randomCourierId, is_available = Some(false))
                    order
                  }
                }
              }
          } ~
            path(LongNumber) { id => {
              get {
                complete {
                  orderService.getOrder(id)
                }
              } ~
                put {
                  entity(as[OrderOptional]) { o =>
                    complete {
                      orderService.updateOrder(
                        order_id = id,
                        details = o.details,
                        zone = o.zone,
                        added_at = o.added_at
                      )
                    }
                  }
                } ~
                delete {
                  complete {
                    orderService.deleteOrder(id)
                  }
                }
            }
            }
        }
      } ~ pathPrefix("couriers") {
        rejectEmptyResponse {
          pathEndOrSingleSlash {
            get {
              complete {
                courierService.getAll()
              }
            } ~
              post {
                entity(as[Courier]) { c =>
                  complete {
                    courierService.newCourier(
                      name = c.name,
                      zone = c.zone,
                      is_available = c.is_available
                    )
                  }
                }
              }
          } ~
            path(LongNumber) { id => {
              get {
                complete {
                  courierService.getCourier(id)
                }
              } ~
                put {
                  entity(as[CourierOptional]) { c =>
                    complete {
                      courierService.updateCourier(
                        courier_id = id,
                        name = c.name,
                        zone = c.zone,
                        is_available = c.is_available
                      )
                    }
                  }
                } ~
                delete {
                  complete {
                    courierService.deleteCourier(id)
                  }
                }
            }
            }
        }
      } ~ pathPrefix("assignments") {
        rejectEmptyResponse {
          pathEndOrSingleSlash {
            get {
              complete {
                assignmentService.getAll()
              }
            }
          }
        }
      } ~ pathPrefix("retryAssignments") {
        rejectEmptyResponse {
          pathEndOrSingleSlash {
            get {
              complete {
                val unassignedOrders = assignmentService.getUnassignedCouriers(-1)
                unassignedOrders.foreach { order =>
                  val orderZone = orderService.getOrder(order.order_id).map(_.zone)
                  if (orderZone.isEmpty) ()
                  else {
                    val random_courier_gen = new Random
                    val availableCouriers = courierService.getAvailableByZone(orderZone.get)
                    val randomCourier = if (!availableCouriers.isEmpty) availableCouriers
                      .lift(random_courier_gen.nextInt(availableCouriers.length)) else None
                    if (randomCourier.isEmpty) ()
                    else {
                      assignmentService.updateAssignment(
                        order_id = order.order_id,
                        courier_id = Option(randomCourier.get.courier_id)
                      )
                      courierService.updateCourier(courier_id = randomCourier.get.courier_id, is_available = Some(false))
                    }
                  }
                }
                assignmentService.getAll()
              }
            }
          }
        }
      } ~
        path("healthcheck") {
          get {
            complete(Unit)
          }
        }
    }

  def run(host: String, port: Int): Unit = {
    startServer(host, port, system)
    system.terminate()
  }

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load().getConfig("http")
    run(config.getString("host"), config.getInt("port"))
  }
}