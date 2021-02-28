package com.delivery.dao

import java.util.UUID

import cats.Id
import com.datastax.spark.connector.SomeColumns
import com.delivery.dao.IAssignmentDAO.IAssignmentRepository
import com.delivery.dao.ICourierDAO.ICourierRepository
import com.delivery.dao.IOrderDAO.IOrderRepository
import com.delivery.domain.OrderDeliveryDomain.{Assignment, Courier, Order}
import com.delivery.spark.SparkCassandraContext
import com.datastax.spark.connector._
import com.fasterxml.uuid.Generators

/**
 * Created by ppani on 28/02/21
 */
object CourierDAO {
  val spark = SparkCassandraContext.spark
  import spark.implicits._

  class CourierRepository extends ICourierRepository[Id] {
    override def get(id: Long): Id[Option[Courier]] ={
      val courier = spark.sql(s"SELECT * FROM history.sales.courier where courier_id = $id").toDF()
      courier.as[Courier].collect().headOption
    }


    override def getAll(): Id[List[Courier]] ={
      val courier = spark.sql(s"SELECT * FROM history.sales.courier").toDF()
      courier.as[Courier].collect().toList
    }


    override def getAvailableByZone(z: String): Id[List[Courier]] = {
      getAll().filter(o => o.zone.equals(z) && o.is_available)
    }

    override def create(name: String,
                        zone: String,
                        is_available: Boolean): Id[Courier] = {

      //user define function timeUUID  which will return time based uuid


      val timeUUID = Generators.timeBasedGenerator.generate.timestamp()
      spark.createDataFrame(
        Seq((timeUUID, name, zone.toString, is_available)))
        .toDF("courier_id", "name", "zone", "is_available")
        .rdd.saveToCassandra("sales", "courier", SomeColumns("courier_id", "name", "zone", "is_available"))
      get(timeUUID).get
    }

    override def update(courier: Courier): Id[Unit] = {
      spark.createDataFrame(
        Seq((courier.courier_id, courier.name, courier.zone.toString, courier.is_available)))
        .toDF("courier_id", "name", "zone", "is_available")
        .rdd.saveToCassandra("sales", "courier", SomeColumns("courier_id", "name", "zone", "is_available"))
    }

    override def delete(id: Long): Id[Unit] = {
      get(id).map(courier => spark.createDataFrame(
        Seq((courier.courier_id, courier.name, courier.zone.toString, courier.is_available)))
        .toDF("courier_id", "name", "zone", "is_available")
        .rdd.deleteFromCassandra("sales", "courier", SomeColumns("courier_id", "name", "zone", "is_available")))
    }
  }

}
object OrderDAO {
  val spark = SparkCassandraContext.spark
  import spark.implicits._

  class OrderRepository extends IOrderRepository[Id] {
    override def get(id: Long): Id[Option[Order]] = {
      val courier = spark.sql(s"SELECT * FROM history.sales.order where order_id = $id").toDF()
      courier.as[Order].collect().headOption
    }


    override def getAll(): Id[List[Order]] ={
      val courier = spark.sql(s"SELECT * FROM history.sales.order").toDF()
      courier.as[Order].collect().toList
    }

    override def create(details: String,
                        zone: String,
                        added_at: String): Id[Order] = {

      //user define function timeUUID  which will return time based uuid
      val timeUUID = Generators.timeBasedGenerator.generate.timestamp()
      spark.createDataFrame(
        Seq((timeUUID, details, zone.toString, added_at)))
        .toDF("order_id", "details", "zone", "added_at")
        .rdd.saveToCassandra("sales", "order", SomeColumns("order_id", "details", "zone", "added_at"))
      get(timeUUID).get
    }

    override def update(order: Order): Id[Unit] = {
      spark.createDataFrame(
        Seq((order.order_id, order.details, order.zone.toString, order.added_at)))
        .toDF("order_id", "details", "zone", "added_at")
        .rdd.saveToCassandra("sales", "order", SomeColumns("order_id", "details", "zone", "added_at"))
    }

    override def delete(id: Long): Id[Unit] = {
      get(id).map(order => spark.createDataFrame(
        Seq((order.order_id, order.details, order.zone.toString, order.added_at)))
        .toDF("order_id", "details", "zone", "added_at")
        .rdd.deleteFromCassandra("sales", "order", SomeColumns("order_id", "details", "zone", "added_at")))
    }
  }

}
object AssignmentDAO {
  val spark = SparkCassandraContext.spark
  import spark.implicits._

  class AssignmentRepository extends IAssignmentRepository[Id] {

    override def getByCourier(courier_id: Long): Id[Option[Assignment]] = {
      val courier = spark.sql(s"SELECT * FROM history.sales.assignment where courier_id = $courier_id").toDF()
      courier.as[Assignment].collect().headOption
    }

    override def getByOrder(order_id: Long): Id[Option[Assignment]] = {
      val courier = spark.sql(s"SELECT * FROM history.sales.assignment where order_id = $order_id").toDF()
      courier.as[Assignment].collect().headOption
    }

    override def getAll(): Id[List[Assignment]] = {
      val courier = spark.sql(s"SELECT * FROM history.sales.assignment").toDF()
      courier.as[Assignment].collect().toList
    }

    override def create(order_id: Long,
                        courier_id: Long): Id[Assignment] = {
      spark.createDataFrame(
        Seq((order_id, courier_id)))
        .toDF("order_id", "courier_id")
        .rdd.saveToCassandra("sales", "assignment", SomeColumns("order_id", "courier_id"))
      getByOrder(order_id).get
    }

    override def update(assignment: Assignment): Id[Unit] = {
      spark.createDataFrame(
        Seq((assignment.order_id, assignment.courier_id)))
        .toDF("order_id", "courier_id")
        .rdd.saveToCassandra("sales", "assignment", SomeColumns("order_id", "courier_id"))
    }

    override def getByUnassignedCourier(courier_id: Long): Id[List[Assignment]] = {
      getAll().filter(_.courier_id == -1)
    }
  }
}
