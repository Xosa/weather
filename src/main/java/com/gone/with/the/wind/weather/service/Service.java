package com.gone.with.the.wind.weather.service;

import java.time.LocalDateTime;
import java.util.List;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public interface Service {

  Future<List<JsonObject>> fetchData(final LocalDateTime start, final LocalDateTime end);
}
