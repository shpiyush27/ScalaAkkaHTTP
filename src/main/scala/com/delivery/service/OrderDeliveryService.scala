package com.delivery.service

import cats.Monad
import cats.data.OptionT
import com.delivery.dao.IAssignmentDAO.IAssignmentRepository
import com.delivery.dao.ICourierDAO.ICourierRepository
import com.delivery.dao.IOrderDAO.IOrderRepository
import com.delivery.domain.OrderDeliveryDomain.{Assignment, Courier, Order}

/**
 * Created by ppani on 28/02/21
 */
object CourierService {

  class CourierService[F[_]](courierRepo: ICourierRepository[F])(
    implicit M: Monad[F]) {
    def newCourier(name: String,
                   zone: Char,
                   is_available: Boolean): F[Courier] =
      courierRepo.create(name, zone, is_available)

    def updateCourier(courier_id: Long,
                      name: Option[String] = None,
                      zone: Option[Char] = None,
                      is_available: Option[Boolean] = None
                     ): F[Option[Courier]] = {
      (for {
        r <- OptionT(courierRepo.get(courier_id))
        u <- OptionT.pure {
          var res = r
          name.foreach(v => res = res.copy(name = v))
          zone.foreach(v => res = res.copy(zone = v))
          is_available.foreach(v => res = res.copy(is_available = v))
          res
        }
        _ <- OptionT.liftF(courierRepo.update(u))
      } yield u).value
    }

    def getCourier(id: Long): F[Option[Courier]] = courierRepo.get(id)

    def getAll(): F[List[Courier]] = courierRepo.getAll()

    def getAvailableByZone(z: Char): F[List[Courier]] = courierRepo.getAvailableByZone(z)


    def deleteCourier(id: Long): F[Option[Courier]] = {
      (for {
        r <- OptionT(courierRepo.get(id))
        _ <- OptionT.liftF(courierRepo.delete(r.courier_id))
      } yield r).value
    }
  }

}

object OrderService {

  class OrderService[F[_]](orderRepo: IOrderRepository[F])(
    implicit M: Monad[F]) {
    def newOrder(details: String,
                 zone: Char,
                 added_at: String): F[Order] =
      orderRepo.create(details, zone, added_at)

    def updateOrder(order_id: Long,
                    details: Option[String] = None,
                    zone: Option[Char] = None,
                    added_at: Option[String] = None
                   ): F[Option[Order]] = {
      (for {
        r <- OptionT(orderRepo.get(order_id))
        u <- OptionT.pure {
          var res = r
          details.foreach(v => res = res.copy(details = v))
          zone.foreach(v => res = res.copy(zone = v))
          added_at.foreach(v => res = res.copy(added_at = v))
          res
        }
        _ <- OptionT.liftF(orderRepo.update(u))
      } yield u).value
    }

    def getOrder(id: Long): F[Option[Order]] = orderRepo.get(id)

    def getAll(): F[List[Order]] = orderRepo.getAll()

    def deleteOrder(id: Long): F[Option[Order]] = {
      (for {
        r <- OptionT(orderRepo.get(id))
        _ <- OptionT.liftF(orderRepo.delete(r.order_id))
      } yield r).value
    }
  }

}

object AssignmentService {

  class AssignmentService[F[_]](assignmentRepo: IAssignmentRepository[F])(
    implicit M: Monad[F]) {
    def newAssignment(order_id: Long,
                      courier_id: Long): F[Assignment] =
      assignmentRepo.create(order_id, courier_id)

    def getOrder(id: Long): F[Option[Assignment]] = assignmentRepo.getByOrder(id)

    def getCourier(id: Long): F[Option[Assignment]] = assignmentRepo.getByCourier(id)
    def getUnassignedCouriers(id: Long): F[List[Assignment]] = assignmentRepo.getByUnassignedCourier(id)

    def getAll(): F[List[Assignment]] = assignmentRepo.getAll()
    def updateAssignment(order_id: Long,
                         courier_id: Option[Long]): F[Option[Assignment]] = {
      (for {
        r <- OptionT(assignmentRepo.getByOrder(order_id))

        u <- OptionT.pure {
          var res = r
          courier_id.foreach(v => res = res.copy(courier_id = v))
          res
        }
        _ <- OptionT.liftF(assignmentRepo.update(u))
      } yield u).value
    }
  }

}
