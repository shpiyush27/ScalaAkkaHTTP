# Order Delivery Handler Scala Akka HTTP

## Introduction
An Order delivery Handler for below Data model

### Courier
An entity that handles deliveries, defined by the following properties:
- **courier_id** uuid Unique identifier
- **name** string Name of the courier
- **zone** enum ‘N’, ‘S’, ‘E’ or ‘W’
- **is_available** boolean Flag indicating if the current entity is available or not

### Order
An entity that represents something a Courier handles, defined by the following properties:
- **order_id** uuid Unique identifier
- **details** string Details of order
- **zone** enum ‘N’, ‘S’, ‘E’ or ‘W’
- **added_at** timestamp Timestamp indicating whenthe order was placed

### Assignment
An entity that represents an assignment between a courier and an order, representing who will deliver what.
- **courier_id** uuid Unique identifier of courier
- **order_id** uuid Unique identifier of order

#### Tech Used : Spark, Cassandra, Akka, Akka HTTP, Circe, Lightbend Config and ScalaTest libraries.

## REST API Types
1. REST API with Spark and Cassandra found @ URL: `/v2/`
2. In Memory REST API without Database involvement found @ URL: `/v1/`

## 1. REST API with Spark and Cassandra

### Pre-Requisites
- Either the local machine or the docker container should have pre-installed Apache Spark and Cassandra
### Local Machine Setup
#### Installing Cassandra
- wget https://downloads.apache.org/cassandra/4.0-beta4/apache-cassandra-4.0-beta4-bin.tar.gz
- tar -xvzf apache-cassandra-4.0-beta4-bin.tar.gz
- Add these two lines in your .bash_profile `export CASSANDRA_HOME=/Users/user/cassandra/apache-cassandra-4.0-beta4` and  `export PATH=$CASSANDRA_HOME/bin:$PATH`
- Restart terminal and type `cassandra` to start the DB server on localhost
#### Installing SPARK
- Use homebrew to install spark `brew cask install spark`, you can tap on specific versions as well.
#### Spark Cassandra Connector JAR Deployment
- git clone https://github.com/datastax/spark-cassandra-connector.git
- cd spark-cassandra-connector
- sbt/sbt clean
- sbt/sbt assembly
- cp target/scala-2.12/spark-cassandra-connector-assembly-3.0.0-44-ga9c531fd.jar $SPARK_HOME/jars/
- Note: - There should be a copy of `spark-cassandra-connector-assembly-3.0.0-44-ga9c531fd.jar` inside `orderdeliverhandler` project path `orderdeliverhandler/lib` as present in this project


This service offers a HTTP api with the following features:
- Add one or more courier to list of couriers
- Mark courier available or not available
- Add order to the system
- Get the courier assigned for an order
- Get the list of orders a courier has to deliver

**URL**:   `/v2/couriers`

**Method**: `POST`

**Data Parameters in JSON**:
- **courier_id** uuid Unique identifier
- **name** string Name of the courier
- **zone** enum ‘N’, ‘S’, ‘E’ or ‘W’
- **is_available** boolean Flag indicating if the current entity is available or not

#### Success Response

**Code**: 200

**Content**: Courier entity in JSON format

#### Error Response

If one of the required fields is missing:

**Code**: 400

**Content**: Error message

### Update a courier by id.

**URL**:   `/v2/couriers/:id`

**Method**: `PUT`

**Data Parameters in JSON**:

- **name** string Name of the courier
- **zone** enum ‘N’, ‘S’, ‘E’ or ‘W’
- **is_available** boolean Flag indicating if the current entity is available or not


All fields are optional.

#### Success Response

**Code**: 200

**Content**: Courier entity in JSON format

#### Error Response
If no courier by id is found.

**Code**: 404

### Get Courier by id.

**URL**:   `/v2/couriers/:id`

**Method**: `GET`

#### Success Response

**Code**: 200

**Content**: Couriers entity in JSON format

#### Error Response
If no courier by id is found.

**Code**: 404

### List all couriers.

**URL**:   `/v2/couriers`

**Method**: `GET`

#### Success Response

**Code**: 200

**Content**: List of couriers entities in JSON format


### Create a new Order.

**URL**:   `/v2/orders`

**Method**: `POST`

**Data Parameters in JSON**:
- **order_id** uuid Unique identifier
- **details** string Details of order
- **zone** enum ‘N’, ‘S’, ‘E’ or ‘W’
- **added_at** timestamp Timestamp indicating when the order was placed

#### Success Response

**Code**: 200

**Content**: Order entity in JSON format
**Note**: Order is created even if there are no couriers available with -1 courier id assignment. **retryAssignment** rest APi can be used to retry whenever a courier becomes available.

#### Error Response

If one of the required fields is missing:

**Code**: 400

**Content**: Error message

### Update an order by id.

**URL**:   `/v2/orders/:id`

**Method**: `PUT`

**Data Parameters in JSON**:

- **details** string Details of order
- **zone** enum ‘N’, ‘S’, ‘E’ or ‘W’
- **added_at** timestamp Timestamp indicating whenthe order was placed


All fields are optional.

#### Success Response

**Code**: 200

**Content**: Order entity in JSON format

#### Error Response
If no order by id is found.

**Code**: 404

### Get Order by id.

**URL**:   `/v2/orders/:id`

**Method**: `GET`

#### Success Response

**Code**: 200

**Content**: Orders entity in JSON format

#### Error Response
If no order by id is found.

**Code**: 404

### List all orders.

**URL**:   `/v2/orders`

**Method**: `GET`

#### Success Response

**Code**: 200

**Content**: List of orders entities in JSON format

### Get all assignments.

**URL**:   `/v2/assignments`

**Method**: `GET`

#### Success Response

**Code**: 200
**Content**: List of assignments in JSON format

### Retry and get all assignments.

**URL**:   `/v2/retryAssignments`

**Method**: `GET`

#### Success Response

**Code**: 200
**Content**: List of updated/retried assignments in JSON format


### Health check.

**URL**:   `/v2/healthcheck`

**Method**: `GET`

#### Success Response

**Code**: 200

## 2. In Memory REST API without Database involvement

### Create a new Courier.

**URL**:   `/v1/couriers`

**Method**: `POST`

**Data Parameters in JSON**:
- **courier_id** uuid Unique identifier
- **name** string Name of the courier
- **zone** enum ‘N’, ‘S’, ‘E’ or ‘W’
- **is_available** boolean Flag indicating if the current entity is available or not

#### Success Response

**Code**: 200

**Content**: Courier entity in JSON format

#### Error Response

If one of the required fields is missing:

**Code**: 400

**Content**: Error message

### Update a courier by id.

**URL**:   `/v1/couriers/:id`

**Method**: `PUT`

**Data Parameters in JSON**:

- **name** string Name of the courier
- **zone** enum ‘N’, ‘S’, ‘E’ or ‘W’
- **is_available** boolean Flag indicating if the current entity is available or not


All fields are optional.

#### Success Response

**Code**: 200

**Content**: Courier entity in JSON format

#### Error Response
If no courier by id is found.

**Code**: 404

### Get Courier by id.

**URL**:   `/v1/couriers/:id`

**Method**: `GET`

#### Success Response

**Code**: 200

**Content**: Couriers entity in JSON format

#### Error Response
If no courier by id is found.

**Code**: 404

### List all couriers.

**URL**:   `/v1/couriers`

**Method**: `GET`

#### Success Response

**Code**: 200

**Content**: List of couriers entities in JSON format


### Create a new Order.

**URL**:   `/v1/orders`

**Method**: `POST`

**Data Parameters in JSON**:
- **order_id** uuid Unique identifier
- **details** string Details of order
- **zone** enum ‘N’, ‘S’, ‘E’ or ‘W’
- **added_at** timestamp Timestamp indicating when the order was placed
  
#### Success Response

**Code**: 200

**Content**: Order entity in JSON format
**Note**: Order is created even if there are no couriers available with -1 courier id assignment. **retryAssignment** rest APi can be used to retry whenever a courier becomes available.

#### Error Response

If one of the required fields is missing:
  
**Code**: 400

**Content**: Error message

### Update an order by id.

**URL**:   `/v1/orders/:id`

**Method**: `PUT`

**Data Parameters in JSON**:

- **details** string Details of order
- **zone** enum ‘N’, ‘S’, ‘E’ or ‘W’
- **added_at** timestamp Timestamp indicating whenthe order was placed

  
All fields are optional.  
  
#### Success Response

**Code**: 200

**Content**: Order entity in JSON format

#### Error Response
If no order by id is found.

**Code**: 404

### Get Order by id.

**URL**:   `/v1/orders/:id`

**Method**: `GET`

#### Success Response

**Code**: 200

**Content**: Orders entity in JSON format

#### Error Response
If no order by id is found.

**Code**: 404

### List all orders.

**URL**:   `/v1/orders`

**Method**: `GET`

#### Success Response

**Code**: 200

**Content**: List of orders entities in JSON format

### Get all assignments.

**URL**:   `/v1/assignments`

**Method**: `GET`

#### Success Response

**Code**: 200
**Content**: List of assignments in JSON format

### Retry and get all assignments.

**URL**:   `/v1/retryAssignments`

**Method**: `GET`

#### Success Response

**Code**: 200
**Content**: List of updated/retried assignments in JSON format


### Health check.

**URL**:   `/v1/healthcheck`

**Method**: `GET`

#### Success Response

**Code**: 200

  
## Build Docker and REST API service
### Docker

```bash
sbt docker:publishLocal
```

It will build local docker image `orderdeliveryhandler:0.1`

## Run REST API service in docker 
```bash
docker run -it --rm -p "8080:8080" orderdeliveryhandler:0.1
```

## REST API service configuration

By default service bind to `0.0.0.0` interface by `8080` port.It can be changed via environment variables inside application.conf or from outside:
* HTTP_HOST
* HTTP_PORT 
