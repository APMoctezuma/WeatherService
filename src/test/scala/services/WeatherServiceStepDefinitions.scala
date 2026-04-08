package services

import io.cucumber.datatable.DataTable
import io.cucumber.scala.{EN, ScalaDsl}
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito.{mock, when}
import services.models.Characterization
import sttp.client4.Response
import sttp.model.{StatusCode, Uri}

class WeatherServiceStepDefinitions extends ScalaDsl with EN {

  var mockedHelper: WeatherServiceHelper = mock(classOf[WeatherServiceHelper])
  var weatherService: WeatherService = null
  var latitude: String = ""
  var longitude: String = ""
  var errorMessage: String = ""
  var weatherModel: WeatherModel = null
  var returnedResponse: Response[String] = null
  val headers: Map[String, String] = Map.apply("accept" -> "application/geo-json", "user-agent" -> "antonio.moctezuma")
  var returnedLocation: String = ""

  Given("a weather service with a mocked helper") { () =>
    weatherService = new WeatherService(mockedHelper)
  }

  Given("input values of {string} and {string}") { (lat: String, lon: String) =>
    latitude = lat
    longitude = lon
  }

  Given("the national weather service {string} find a matching area") { (flag: String, body: String) =>
    val pointsUri: Uri = Uri.unsafeParse("https://api.weather.gov/points/%s,%s".format(latitude, longitude))
    if(flag.equals("does")) {
      returnedResponse = new Response[String](body, StatusCode.Ok, "", Seq.empty, null, null)
      when(mockedHelper.makeQuickGetCall(pointsUri, headers)).thenReturn(returnedResponse)
      val city: String = ujson.read(returnedResponse.body)("properties")("relativeLocation")("properties")("city").str
      val state: String = ujson.read(returnedResponse.body)("properties")("relativeLocation")("properties")("state").str
      returnedLocation = city + "," + state
    } else if(flag.equals("does not")) {
      when(mockedHelper.makeQuickGetCall(pointsUri, headers)).thenReturn(new Response[String]("", StatusCode.NotFound, "", Seq.empty, null, null))
    }
  }

  Given("the national weather service {string} find a forecast") { (flag: String, body: String) =>
    val responseJson: ujson.Value = ujson.read(returnedResponse.body)
    val forecastUri: Uri = Uri.unsafeParse(responseJson("properties")("forecast").str)
    if(flag.equals("can")) {
      returnedResponse = new Response[String](body, StatusCode.Ok, "", Seq.empty, null, null)
      when(mockedHelper.makeQuickGetCall(forecastUri, headers)).thenReturn(returnedResponse)
    } else if(flag.equals("cannot")) {
      when(mockedHelper.makeQuickGetCall(forecastUri, headers)).thenReturn(new Response[String]("", StatusCode.NotFound, "", Seq.empty, null, null))
    }
  }

  When("{string} is called") { (methodName: String) =>
    if(methodName.equals("forecast")) {
      try {
        weatherModel = weatherService.getForecast(latitude, longitude)
      } catch {
        case e: Exception =>
          errorMessage = e.getMessage
      }
    }
  }

  Then("an exception should be thrown with the message {string}") { (expectedMessage: String) =>
    assertEquals(expectedMessage, errorMessage)
  }

  Then("no error should have been thrown") { () =>
    assertEquals("", errorMessage)
  }

  Then("a weather model with the following info should have been returned") { (dataTable: DataTable) =>
    val columns: java.util.List[java.util.Map[String, String]] = dataTable.asMaps()
    val expectedLocation: String = columns.get(0).get("location")
    val expectedTemperature: Int = columns.get(0).get("temperature").toInt
    val expectedDescription: String = columns.get(0).get("shortDescription")
    val expectedCharacterization: Characterization = Characterization.valueOf(columns.get(0).get("characterization"))
    val expectedWeatherModel: WeatherModel = WeatherModel(expectedLocation, expectedTemperature, expectedDescription, expectedCharacterization)
    
    val responseJson: ujson.Value = ujson.read(returnedResponse.body)
    val actualLocation: String = returnedLocation
    val actualTemperature: Int = responseJson("properties")("periods")(0)("temperature").num.toInt
    val actualShortDescription: String = responseJson("properties")("periods")(0)("shortForecast").str
    val actualCharacterization: Characterization = actualTemperature match {
      case temperature if temperature < 40 => Characterization.cold
      case temperature if temperature >= 40 && temperature < 60 => Characterization.moderate
      case temperature if temperature >= 60 => Characterization.hot
    }
    val actualWeatherModel: WeatherModel = WeatherModel(actualLocation, actualTemperature, actualShortDescription, actualCharacterization)

    assertEquals(expectedWeatherModel, actualWeatherModel)
  }

}
