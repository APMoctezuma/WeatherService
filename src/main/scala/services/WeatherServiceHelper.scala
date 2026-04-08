package services

import sttp.client4.quick.*
import sttp.client4.Response
import sttp.model.Uri

class WeatherServiceHelper {
  def makeQuickGetCall(uri: Uri, headers: Map[String, String]): Response[String] = {
    quickRequest
      .headers(headers)
      .get(uri)
      .send()
  }
}
