package com.gone.with.the.wind.weather;

import com.gone.with.the.wind.weather.api.WeatherAPI;
import com.gone.with.the.wind.weather.api.Routes;
import com.gone.with.the.wind.weather.service.HTTPClientService;
import com.gone.with.the.wind.weather.service.Service;
import com.gone.with.the.wind.weather.service.WeatherService;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;

public class Server extends AbstractVerticle {

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    final String temperatureHost = config().getString("TEMPERATURE_HOST");
    final String speedHost = config().getString("WINDSPEED_HOST");
    final int temperaturePort = config().getInteger("TEMPERATURE_PORT");
    final int speedPort = config().getInteger("WINDSPEED_PORT");

    final Service temperatureService = new HTTPClientService(vertx, temperatureHost, temperaturePort);
    final Service windspeedService = new HTTPClientService(vertx, speedHost, speedPort);
    final Service weatherService = new WeatherService(temperatureService, windspeedService);
    final WeatherAPI temperatureAPI = new WeatherAPI(temperatureService);
    final WeatherAPI windspeedAPI = new WeatherAPI(windspeedService);
    final WeatherAPI weatherAPI = new WeatherAPI(weatherService);

    final Router router = Router.router(vertx);
    new Routes(router, weatherAPI, temperatureAPI, windspeedAPI).initialiseRoutes();

    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(
        config().getInteger("http.port"),
        result -> {
          if (result.succeeded()) {
            startFuture.complete();
          } else {
            startFuture.fail(result.cause());
          }
        }
      );
  }

  @Override
  public void stop(Future<Void> stopFuture) {
    vertx.close(r -> {
      if (r.succeeded()) {
        stopFuture.complete();
      } else {
        stopFuture.fail(r.cause());
      }
    });
  }
}
