package com.delivery.domain

object OrderDeliveryDomain {

  object Zone extends Enumeration {
    type Zone = Value
    val N, S, E, W = Value

/* // TODO
   import akka.http.scaladsl.unmarshalling.Unmarshaller

    val stringToZone = Unmarshaller.strict[String, Zone] {
      case "N" => Zone.N
      case "S" => Zone.S
      case "E" => Zone.E
      case "W" => Zone.W
    }*/
  }

  case class Courier(courier_id: Long,
                     name: String,
                     zone: String,
                     is_available: Boolean)

  case class CourierOptional(courier_id: Long,
                     name: Option[String],
                     zone: Option[String],
                     is_available: Option[Boolean])

  case class Order(order_id: Long,
                   details: String,
                   zone: String,
                   added_at: String)

  case class OrderOptional(order_id: Long,
                   details: Option[String],
                   zone: Option[String],
                   added_at: Option[String])

  case class Assignment(order_id: Long,
                        courier_id: Long)

  case class AssignmentOptional(order_id: Long,
                        courier_id: Option[Long])

}
