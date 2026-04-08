package services

import exceptions.WeatherServiceException
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import sttp.model.{StatusCode, Uri}

case class WeatherModel(location: String, temperature: Int, shortDescription: String, characterization: String)
object WeatherJsonProtocol extends DefaultJsonProtocol {
  implicit val weatherFormat: RootJsonFormat[WeatherModel] = jsonFormat4(services.WeatherModel.apply)
}

class WeatherService(helper: WeatherServiceHelper) {
  private val weatherAPIPointsEndpoint: String = "https://api.weather.gov/points/%s,%s"

  def getForecast(latitude: String, longitude: String): WeatherModel = {
    var latitudeAsDouble: Double = 0.0
    var longitudeAsDouble: Double = 0.0
    try {
      latitudeAsDouble = latitude.toDouble
      longitudeAsDouble = longitude.toDouble
    } catch {
      case e: NumberFormatException => throw new WeatherServiceException("Invalid input for latitude: %s or longitude: %s".format(latitude, longitude))
    }

    val headers: Map[String, String] = Map.apply("accept" -> "application/geo-json", "user-agent" -> "antonio.moctezuma")
    val returns: (String, String) = getForecastEndpoint(latitudeAsDouble, longitudeAsDouble, headers)
    val forecastEndpoint: String = returns(0)
    val locationInfo: String = returns(1)

    val response = helper.makeQuickGetCall(Uri.unsafeParse(forecastEndpoint), headers)
    if (!response.code.equals(StatusCode.Ok)) {
      throw new WeatherServiceException("Unable to retrieve forecast from the endpoint: %s".format(forecastEndpoint))
    }
    val responseAsJson: ujson.Value = ujson.read(response.body)
    buildWeatherObject(responseAsJson, locationInfo)
  }

  private def getForecastEndpoint(latitude: Double, longitude: Double, headers: Map[String, String]): (String, String) = {
    val myUri: String = weatherAPIPointsEndpoint.format(latitude.toString, longitude.toString)
    val weatherUri = Uri.unsafeParse(myUri)
    val response = helper.makeQuickGetCall(weatherUri, headers)
    if(!response.code.equals(StatusCode.Ok)) {
      throw new WeatherServiceException("Could not find information for values latitude %s and longitude %s".format(latitude.toString, longitude.toString))
    }
    val responseAsJson: ujson.Value = ujson.read(response.body)
    val locationInfo: String =
      responseAsJson("properties")("relativeLocation")("properties")("city").str
        + ","
        + responseAsJson("properties")("relativeLocation")("properties")("state").str
    (responseAsJson("properties")("forecast").str, locationInfo)
  }

  private def buildWeatherObject(value: ujson.Value, locationInfo: String): WeatherModel = {
    val temperature: Int = value("properties")("periods")(0)("temperature").num.toInt
    val shortDescription: String = value("properties")("periods")(0)("shortForecast").str
    val characterization: String = temperature match {
      case temperature if temperature < 40 => "cold"
      case temperature if (temperature >= 40 && temperature < 60) => "moderate"
      case temperature if temperature >= 60 => "hot"
      case _ => throw new WeatherServiceException("Temperature returned is invalid")
    }
    WeatherModel(locationInfo, temperature, shortDescription, characterization)
  }
}
