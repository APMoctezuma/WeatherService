package servers

import services.WeatherService
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import services.WeatherJsonProtocol.given

class WeatherRoutes(weatherService: WeatherService) {
  
  val routes: Route = {
    path("test") {
      get {
        complete("This is a test")
      }
    } ~
    path("forecast") {
      get {
        parameter("latitude", "longitude") { (latitude, longitude) =>
          val weatherForecast = weatherService.getForecast(latitude, longitude)
          complete(weatherForecast)
        }
      }
    }
  }

}
