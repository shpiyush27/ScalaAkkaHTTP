package com.delivery.spark

import org.apache.spark._
import org.apache.log4j._
import org.apache.spark.sql.SparkSession

object SparkCassandraContext {

  Logger.getLogger("org").setLevel(Level.ERROR)
  Logger.getLogger("com").setLevel(Level.ERROR)
  val conf=new SparkConf()
  conf.set("spark.master","local[*]")
  conf.set("spark.app.name","exampleApp")
  val sc=new SparkContext(conf)

  val spark = SparkSession
    .builder
    .appName("SQL Spark")
    .master("local[*]")
    .config("spark.sql.warehouse.dir", "")
    .config("spark.sql.catalog.history", "com.datastax.spark.connector.datasource.CassandraCatalog")
    .config("spark.cassandra.connection.host", "localhost")
    .config("spark.sql.extensions", "com.datastax.spark.connector.CassandraSparkExtensions")
    .getOrCreate()

  spark.sql("CREATE DATABASE IF NOT EXISTS history.sales WITH DBPROPERTIES (class='SimpleStrategy',replication_factor='1')")
  spark.sql("CREATE TABLE IF NOT EXISTS history.sales.order (order_id LONG, details STRING, zone STRING, added_at TIMESTAMP) USING cassandra PARTITIONED BY (order_id)")
  spark.sql("CREATE TABLE IF NOT EXISTS history.sales.courier (courier_id LONG, name STRING, zone STRING, is_available BOOLEAN) USING cassandra PARTITIONED BY (courier_id)")
  spark.sql("CREATE TABLE IF NOT EXISTS history.sales.assignment (order_id LONG, courier_id LONG) USING cassandra PARTITIONED BY (order_id)")
  //List all keyspaces
  spark.sql("SHOW NAMESPACES FROM history").show(false)
  //List tables under keyspace sales
  spark.sql("SHOW TABLES FROM history.sales").show(false)

  /*//Create some sales records, write them into Cassandra table sales.salesfact
  spark.createDataFrame(Seq((0,"2020-09-06 10:00:00","TV","200.00"),(1,"2020-09-06 11:00:00","Laptop","500.00"))).toDF("key","sales_date","product","value").rdd.saveToCassandra("sales", "salesfact", SomeColumns("key", "sale_date", "product", "value"))
  //Query data from Cassandra by Spark SQL, using window function that is not available on CQL
  spark.sql("SELECT product, sum(value) over (partition by product) total_sales FROM history.sales.salesfact").show(false)*/
}
