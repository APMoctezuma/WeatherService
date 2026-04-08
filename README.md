# Weather Service
## By: Antonio Moctezuma

## What is it?
What this service does is create a Scala server utilizing akka http and exposes
an endpoint which allows the user to give latitude and longitude values that are
passed to the [National Weather Service's Public API](https://www.weather.gov/documentation/services-web-api)
in order to retrieve the current forecast for that area via a Scala case class of
`WeatherModel`.

### WeatherModel
| field            | type     | description                                              | possible values                                                   |
|------------------|----------|----------------------------------------------------------|-------------------------------------------------------------------|
| location         | String   | The location for the forecast in the form of city, state | Any city, state combination within the US                         |
| temperature      | Integer  | The current temperature for the area in Farenheit        | Any valid temperature in the Farenheit scale                      |
| shortDescription | String   | A brief textual forecast summary for the area            | This is taken directly from the call to the Weather Service's API |
| characterization | String   | A single word description of the temperature             | cold, hot, or moderate                                            |

## How to use it?
1. Find the file Main.scala (src/main/scala/Main.scala)
2. Change Url and port if desired. Otherwise, this defaults to running on http://localhost:8080
3. Run the file
4. In your browser of choice, hit the `forecast` endpoint utilizing query parameters like so:
`<url>/forecast?latitude=<your latitude>&longitude=<your longitude>`
5. The page will provide you with the returned `WeatherModel` giving you your desired information