package services

import exceptions.WeatherServiceException
import services.models.Characterization
import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat, RootJsonFormat, deserializationError}
import sttp.model.{StatusCode, Uri}

case class WeatherModel(location: String, temperature: Int, shortDescription: String, characterization: Characterization)
object WeatherJsonProtocol extends DefaultJsonProtocol {
  implicit object CharacterizationFormat extends JsonFormat[Characterization] {
    def write(obj: Characterization): JsValue = JsString(obj.toString)
    def read(json: JsValue): Characterization = json match {
      case JsString(s) => Characterization.valueOf(s)
      case _ => deserializationError("Expected String value for Characterization")
    }
  }
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
    val characterization: Characterization = temperature match {
      case temperature if temperature < 40 => Characterization.cold
      case temperature if temperature >= 40 && temperature < 60 => Characterization.moderate
      case temperature if temperature >= 60 => Characterization.hot
      case _ => throw new WeatherServiceException("Temperature returned is invalid")
    }
    WeatherModel(locationInfo, temperature, shortDescription, characterization)
  }
}
