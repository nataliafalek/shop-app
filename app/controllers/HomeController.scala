package controllers

import javax.inject._
import model.Product
import play.api.libs.json.Json
import play.api.mvc._

@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def appSummary = Action {
    Ok(Json.obj("content" -> "Scala Play Angular Seed"))
  }

  def postTest = Action {
    Ok(Json.obj("content" -> "Post Request Test => Data Sending Success"))
  }

  def getProducts = Action {
    implicit val productFormat = Json.format[Product]
    val products1 = Product("zielony", "S")
    val products2 = Product("zielony", "M")
    val products3 = Product("czerwony", "L")
    val products4 = Product("niebieski", "M")
    val products = List(products1, products2, products3, products4)
    Ok(Json.toJson("products" -> products))
  }
}
