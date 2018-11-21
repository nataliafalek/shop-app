package model

import akka.http.scaladsl.model.headers.Date
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Repositories @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class ProductTable(tag: Tag) extends Table[Product](tag, "product") {

    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def color = column[String]("color")

    def size = column[String]("size")

    def * = (id, color, size) <> ((Product.apply _).tupled, Product.unapply)
  }

  private class ProductQuantityTable(tag: Tag) extends Table[ProductQuantity](tag, "product_quantity") {

    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def productId = column[Long]("productId")

    def quantity = column[Int]("quantity")

    def * = (id, productId, quantity) <> ((ProductQuantity.apply _).tupled, ProductQuantity.unapply)
  }

  private class OrderTable(tag: Tag) extends Table[Order](tag, "order") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def age = column[Int]("age")

    def date = column[String]("date")

    def * = (id, name, age, date) <> ((Order.apply _).tupled, Order.unapply)
  }

  private class OrderProductTable(tag: Tag) extends Table[OrderProduct](tag, "order_product") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def productId = column[Long]("productId")

    def orderId = column[Long]("orderId")

    def orderQuantity = column[Int]("orderQuantity")

    def * = (id, productId, orderId, orderQuantity) <> ((OrderProduct.apply _).tupled, OrderProduct.unapply)
  }

  private val product = TableQuery[ProductTable]
  private val productQuantity = TableQuery[ProductQuantityTable]
  private val orderProduct = TableQuery[OrderProductTable]
  private val order = TableQuery[OrderTable]

  def createOrder(name: String, age: Int, date: String): Future[Order] = db.run {
    (order.map(o => (o.name, o.age, o.date))
      returning order.map(_.id)
      into { case ((name, age, date), id) => Order(id = id, name = name, age = age, date = date) }
      ) += (name, age, date)
  }

  def createOrderProduct(productId: Long, orderId: Long, orderQuantity: Int): Future[OrderProduct] = db.run {
    (orderProduct.map(o => (o.productId, o.orderId, o.orderQuantity))
      returning orderProduct.map(_.id)
      into { case ((productId, orderId, orderQuantity), id) =>
      OrderProduct(id = id, productId = productId, orderId = orderId, orderQuantity = orderQuantity)
    }
      ) += (productId, orderId, orderQuantity)
  }

  def decreaseQuantity(productId: Long, orderQuantity: Int): Future[Unit] = {
    val product = findProductQuantityByProductId(productId)
    product.flatMap {
      product =>
        db.run {
          val query = productQuantity.filter(_.productId === productId).map(_.quantity)
          query.update(product.quantity - orderQuantity)
        }.map(_ => ())
    }
  }

  def listOrderSummaryDTO(): Future[Seq[OrderSummaryDTO]] = {
    db.run {
      orderProduct
        .join(product).on(_.productId === _.id)
        .join(order).on(_._1.orderId === _.id)
        .result
        .map { orderSummary =>
          orderSummary.groupBy(_._2.id).mapValues { seqProducts =>
            val order = seqProducts.head._2
            val products = seqProducts.map { case ((orderProduct, product), order) =>
              ProductDTO(product.id, product.color, product.size, orderProduct.orderQuantity)
            }.toList
            OrderSummaryDTO(order.id, order.date, order.name, order.age, products)
          }.values.toList
        }
    }
  }

  def listProductDTO(): Future[Seq[ProductDTO]] = {
    db.run {
      productQuantity.join(product).on {
        _.productId === _.id
      }.result.map { productsWithQuantities =>
        productsWithQuantities.map { case (pq, p) =>
          ProductDTO(p.id, p.color, p.size, pq.quantity)
        }
      }
    }
  }


  def listOrderProduct(): Future[Seq[OrderProduct]] = db.run {
    orderProduct.result
  }

  def findProductQuantityByProductId(productId: Long): Future[ProductQuantity] = db.run {
    productQuantity.filter(_.productId === productId).result.head
  }

}