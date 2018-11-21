package model

import play.api.libs.json.{Json, OFormat}

case class Order(id: Long, name: String, age: Int, date: String)

case class OrderProduct(id: Long, productId: Long, orderId: Long, orderQuantity: Int)

case class OrderProductDTO(name: String, age: Int, date: String, orderProducts: List[ProductQuantity])

case class OrderSummaryDTO(orderId: Long, date: String, name: String, age: Int, orderProducts: List[ProductDTO])


object Order {
  implicit val orderFormat: OFormat[Order] = Json.format[Order]
}

object OrderProductDTO {
  implicit val orderProductDTOFormat: OFormat[OrderProductDTO] = Json.format[OrderProductDTO]
}

object OrderProduct {
  implicit val orderProductFormat: OFormat[OrderProduct] = Json.format[OrderProduct]
}

object OrderSummaryDTO {
  implicit val orderSummaryDTOFormat: OFormat[OrderSummaryDTO] = Json.format[OrderSummaryDTO]

}
