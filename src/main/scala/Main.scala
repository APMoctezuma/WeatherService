import akka.actor.typed.ActorSystem
import akka.actor.typed.javadsl.Behaviors
import servers.WeatherServiceApp

object Main {
  @main def run(): Unit = {
    given ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "WeatherService")

      WeatherServiceApp.startServer("localhost", 8080)
  }
}
