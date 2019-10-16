package com.gone.with.the.wind.weather.api;

import io.vertx.ext.web.Router;

public class Routes {

  private static final String WEATHER_ROUTE = "/weather";
  private static final String TEMPERATURE_ROUTE = "/temperatures";
  private static final String SPEED_ROUTE = "/speeds";

  private final WeatherAPI weatherAPI;
  private final WeatherAPI temperatureAPI;
  private final WeatherAPI windspeedAPI;
  private final Router router;

  public Routes(final Router router, final WeatherAPI weatherAPI, final WeatherAPI temperatureAPI, final WeatherAPI windspeedAPI) {
    this.weatherAPI = weatherAPI;
    this.temperatureAPI = temperatureAPI;
    this.windspeedAPI = windspeedAPI;
    this.router = router;
  }

  public void initialiseRoutes() {
    router.route(WEATHER_ROUTE);
    router.route(TEMPERATURE_ROUTE);
    router.route(SPEED_ROUTE);

    router.get(WEATHER_ROUTE).handler(weatherAPI::getData);
    router.get(TEMPERATURE_ROUTE).handler(temperatureAPI::getData);
    router.get(SPEED_ROUTE).handler(windspeedAPI::getData);
  }
}
