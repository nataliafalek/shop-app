package model

import play.api.libs.json._

case class Product(id: Option[Long], color: String, size: String)

case class ProductDTO(id: Option[Long], color: String, size: String, quantity: Int)

case class ProductQuantity(id: Option[Long], productId: Long, quantity: Int)

object ProductQuantity {
  implicit val productQuantityFormat: OFormat[ProductQuantity] = Json.format[ProductQuantity]
}

object ProductDTO {
  implicit val productFormat: OFormat[ProductDTO] = Json.format[ProductDTO]
}
