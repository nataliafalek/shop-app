package controllers

import javax.inject._
import model.{OrderProductDTO, ProductQuantity, Repositories}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeController @Inject()(cc: ControllerComponents, productRepository: Repositories)
                              (implicit ec: ExecutionContext) extends AbstractController(cc) {

  def getProducts: Action[AnyContent] = Action.async {
    productRepository.listProductDTO().map { products =>
      Ok(Json.toJson(products))
    }
  }

  def sendOrder = Action.async(parse.json) { request =>
    val order = request.body.validate[OrderProductDTO].get
    val validationResult = invalidOrdersQuantity(order.orderProducts)
    validationResult.flatMap { result =>
      if (!result.isEmpty) {
        Future.successful(BadRequest(Json.toJson(result)))
      } else {
        createOrderAndDecreaseProductQuantity(order)
      }
    }
  }

  private def createOrderAndDecreaseProductQuantity(order: OrderProductDTO) = {
    val orderDataId = productRepository.createOrder(order.name, order.age, order.date).map(o => o.id)
    orderDataId.flatMap { orderId =>
      Future.sequence(order.orderProducts.map { op =>
        productRepository.createOrderProduct(op.productId, orderId, op.quantity)
          .map { _ =>
            productRepository.decreaseQuantity(op.productId, op.quantity)
          }
      })
    }.map { _ =>
      Ok(Json.obj())
    }
  }

  def getOrderProducts = Action.async {
    productRepository.listOrderProduct().map { orderProduct =>
      Ok(Json.toJson(orderProduct))
    }
  }

  def getOrderSummary = Action.async {
    productRepository.listOrderSummaryDTO().map { orderSummary =>
      Ok(Json.toJson(orderSummary))
    }
  }

  def invalidOrdersQuantity(orderProducts: List[ProductQuantity]): Future[List[ProductQuantity]] = {
    Future.sequence(
      orderProducts.map { op =>
        isOrderQuantityLessOrEqualProductQuantity(op).map { isValid =>
          if (isValid) {
            List.empty
          } else {
            List(op)
          }
        }
      }
    ).map(_.flatten)
  }

  def isOrderQuantityLessOrEqualProductQuantity(orderProduct: ProductQuantity): Future[Boolean] = {
    val productQuantity = productRepository.findProductQuantityByProductId(orderProduct.productId).map(pq => pq.quantity)
    productQuantity.map(pq => pq >= orderProduct.quantity)
  }
}
