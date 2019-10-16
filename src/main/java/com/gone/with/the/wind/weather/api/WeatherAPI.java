package com.gone.with.the.wind.weather.api;

import com.gone.with.the.wind.weather.service.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;

public class WeatherAPI {

  public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
  private static final LocalDateTime LOWER_BOUND = LocalDateTime.of(1900, 1, 1, 0, 0, 0);

  private final Service service;

  public WeatherAPI(final Service service) {
    this.service = service;
  }

  protected void getData(final RoutingContext context) {
    final MultiMap queryParams = context.queryParams();
    if (!queryParams.isEmpty()) {
      final String startParam = queryParams.get("start");
      final String endParam = queryParams.get("end");
      if (startParam != null && endParam != null) {
        final LocalDateTime start = LocalDateTime.parse(startParam, formatter).toLocalDate().atStartOfDay();
        final LocalDateTime end = LocalDateTime.parse(endParam, formatter).toLocalDate().atStartOfDay();
        if (isValidRange(start, end)) {
          service.fetchData(start, end).setHandler(result -> {
            if (result.succeeded()) {
              context.response().setStatusCode(200).end(result.result().toString());
            } else {
              context.response().setStatusCode(500).end(result.cause().getMessage());
            }
          });
        } else {
          context.response().setStatusCode(400).end("Invalid date range");
        }
      } else {
        context.response().setStatusCode(400).end("Invalid request, missing start and/or end date");
      }
    } else {
      context.response().setStatusCode(400).end("Invalid request, missing start and/or end date");
    }
  }

  private static boolean isValidRange(final LocalDateTime start, final LocalDateTime end) {
    return start.compareTo(LOWER_BOUND) >= 0 && end.compareTo(LocalDateTime.now().toLocalDate().atStartOfDay()) <= 0;
  }
}
