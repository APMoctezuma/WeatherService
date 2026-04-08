package servers

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import services.{WeatherService, WeatherServiceHelper}

object WeatherServiceApp {
  def startServer(host: String, port: Int) (using system: ActorSystem[?]): Unit = {
    import system.executionContext
    
    val weatherServiceHelper: WeatherServiceHelper = new WeatherServiceHelper()
    val weatherService: WeatherService = new WeatherService(weatherServiceHelper)
    val weatherRoutes: WeatherRoutes = new WeatherRoutes(weatherService)
    
    val server = Http()
      .newServerAt(host, port)
      .bind(weatherRoutes.routes)
    
    server.map { _ =>
      println("Successfully started server on %s:%s".format(host, port))
    } recover { case ex =>
      println("Failed to start server due to: " + ex.getMessage)
    }
  }
}
