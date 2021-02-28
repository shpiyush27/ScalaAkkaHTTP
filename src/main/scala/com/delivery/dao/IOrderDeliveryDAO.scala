package com.delivery.dao

import com.delivery.domain.OrderDeliveryDomain.{Assignment, Courier, Order}

object ICourierDAO {

  trait ICourierRepository[F[_]] {
    def get(id: Long): F[Option[Courier]]

    def getAll(): F[List[Courier]]

    def getAvailableByZone(z: String): F[List[Courier]]

    def create(
                name: String,
                zone: String,
                is_available: Boolean
              ): F[Courier]

    def update(courier: Courier): F[Unit]

    def delete(id: Long): F[Unit]
  }

}
object IOrderDAO {

  trait IOrderRepository[F[_]] {
    def get(id: Long): F[Option[Order]]

    def getAll(): F[List[Order]]

    def create(
                details: String,
                zone: String,
                added_at: String
              ): F[Order]

    def update(order: Order): F[Unit]

    def delete(id: Long): F[Unit]
  }

}
object IAssignmentDAO {
  trait IAssignmentRepository[F[_]] {
    def getByCourier(courier_id: Long): F[Option[Assignment]]
    def getByUnassignedCourier(courier_id: Long): F[List[Assignment]]
    def getByOrder(order_id: Long): F[Option[Assignment]]
    def getAll(): F[List[Assignment]]
    def create(order_id: Long,
               courier_id: Long
              ): F[Assignment]
    def update(assignment: Assignment): F[Unit]
  }
}
