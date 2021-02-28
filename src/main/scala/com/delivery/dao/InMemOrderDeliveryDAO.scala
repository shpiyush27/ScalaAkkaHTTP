package com.delivery.dao

import cats.Id
import com.delivery.dao.IAssignmentDAO.IAssignmentRepository
import com.delivery.dao.ICourierDAO.ICourierRepository
import com.delivery.dao.IOrderDAO.IOrderRepository
import com.delivery.domain.OrderDeliveryDomain.{Assignment, Courier, Order}

import scala.collection.immutable.LongMap

object InMemCourierDAO {

  class InMemCourierRepository extends ICourierRepository[Id] {
    private var couriers = LongMap.empty[Courier]
    private var currentId = 1l

    override def get(id: Long): Id[Option[Courier]] =
      couriers.get(id)

    override def getAll(): Id[List[Courier]] =
      couriers.values.toList

    override def getAvailableByZone(z: Char): Id[List[Courier]] =
      couriers.values.filter(o => o.zone.equals(z) && o.is_available).toList

    override def create(name: String,
                        zone: Char,
                        is_available: Boolean): Id[Courier] = {
      val courier = Courier(
        courier_id = currentId,
        name = name,
        zone = zone,
        is_available = is_available
      )
      couriers += courier.courier_id -> courier
      currentId += 1
      courier
    }

    override def update(courier: Courier): Id[Unit] = {
      couriers += (courier.courier_id -> courier)
    }

    override def delete(id: Long): Id[Unit] = {
      couriers = couriers - id
    }
  }

}
object InMemOrderDAO {

  class InMemOrderRepository extends IOrderRepository[Id] {
    private var orders = LongMap.empty[Order]
    private var currentId = 1l

    override def get(id: Long): Id[Option[Order]] =
      orders.get(id)

    override def getAll(): Id[List[Order]] =
      orders.values.toList

    override def create(details: String,
                        zone: Char,
                        added_at: String): Id[Order] = {
      val order = Order(
        order_id = currentId,
        details = details,
        zone = zone,
        added_at = added_at
      )
      orders += order.order_id -> order
      currentId += 1
      order
    }

    override def update(order: Order): Id[Unit] = {
      orders += (order.order_id -> order)
    }

    override def delete(id: Long): Id[Unit] = {
      orders = orders - id
    }
  }

}
object InMemAssignmentDAO {

  class InMemAssignmentRepository extends IAssignmentRepository[Id] {
    private var assignments = LongMap.empty[Assignment]

    override def getByCourier(courier_id: Long): Id[Option[Assignment]] =
      assignments.get(courier_id)

    override def getByOrder(order_id: Long): Id[Option[Assignment]] =
      assignments.get(order_id)

    override def getAll(): Id[List[Assignment]] =
      assignments.values.toList

    override def create(order_id: Long,
                        courier_id: Long): Id[Assignment] = {
      val assignment = Assignment(
        order_id = order_id,
        courier_id = courier_id
      )
      assignments += assignment.order_id -> assignment
      assignment
    }

    override def update(assignment: Assignment): Id[Unit] = {
      assignments += (assignment.order_id -> assignment)
    }

    override def getByUnassignedCourier(courier_id: Long): Id[List[Assignment]] = {
      assignments.values.filter(_.courier_id == -1).toList
    }
  }
}
