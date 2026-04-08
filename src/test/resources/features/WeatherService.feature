Feature: Test Weather Service

  Background:
    Given a weather service with a mocked helper

  Scenario: User Gives Non Doubles
    Given input values of "lat" and "long"
    When "forecast" is called
    Then an exception should be thrown with the message "Invalid input for latitude: lat or longitude: long"

  Scenario: User Gives Values That Don't Match Anywhere
    Given input values of "43.1789" and "88.1175"
    And the national weather service "does not" find a matching area
    """
    """
    When "forecast" is called
    Then an exception should be thrown with the message "Could not find information for values latitude 43.1789 and longitude 88.1175"

  Scenario: Unable To Retrieve Forecast
    Given input values of "43.1789" and "-88.1175"
    And the national weather service "does" find a matching area
    """
    {
      "properties": {
        "relativeLocation": {
          "properties": {
            "city": "Menomonee Falls",
            "state": "WI"
          }
        },
        "forecast": "https://api.weather.gov/gridpoints/MKX/81,71/forecast"
      }
    }
    """
    And the national weather service "cannot" find a forecast
    """
    """
    When "forecast" is called
    Then an exception should be thrown with the message "Unable to retrieve forecast from the endpoint: https://api.weather.gov/gridpoints/MKX/81,71/forecast"
    
  Scenario: Happy Path
    Given input values of "43.1789" and "-88.1175"
    And the national weather service "does" find a matching area
    """
    {
      "properties": {
        "relativeLocation": {
          "properties": {
            "city": "Menomonee Falls",
            "state": "WI"
          }
        },
        "forecast": "https://api.weather.gov/gridpoints/MKX/81,71/forecast"
      }
    }
    """
    And the national weather service "can" find a forecast
    """
    {
      "properties": {
        "periods": [
          {
            "temperature": 70,
            "shortForecast": "sunny"
          }
        ]
      }
    }
    """
    When "forecast" is called
    Then no error should have been thrown
    And a weather model with the following info should have been returned
      | location           | temperature | shortDescription | characterization |
      | Menomonee Falls,WI | 70          | sunny            | hot              |
