package services.models

import exceptions.WeatherServiceException

enum Characterization {
  case cold, hot, moderate
}

object Characterization {
  def assignCharacterization(temperature: Integer): Characterization = {
    temperature match {
      case temperature if temperature < 40 => Characterization.cold
      case temperature if temperature >= 40 && temperature < 60 => Characterization.moderate
      case temperature if temperature >= 60 => Characterization.hot
      case _ => throw new WeatherServiceException("Temperature returned is invalid")
    }
  }
}