package com.gone.with.the.wind.weather.service;

import com.gone.with.the.wind.weather.api.WeatherAPI;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class WeatherService implements Service {

  private final Service temperatureService;
  private final Service windspeedService;

  public WeatherService(final Service temperatureService, final Service windspeedService) {
    this.temperatureService = temperatureService;
    this.windspeedService = windspeedService;
  }

  public Future<List<JsonObject>> fetchData(final LocalDateTime start, final LocalDateTime end) {
    return CompositeFuture.join(temperatureService.fetchData(start, end), windspeedService.fetchData(start, end)).map(r -> {
      final Map<String, JsonObject> map1 = new HashMap<>();
      final Map<String, JsonObject> map2 = new HashMap<>();
      final List<JsonObject> temperatures = r.resultAt(0);
      for (JsonObject temperature : temperatures) {
        map1.put(temperature.getString("date"), temperature);
      }
      final List<JsonObject> speeds = r.resultAt(1);
      for (JsonObject speed : speeds) {
        map2.put(speed.getString("date"), speed);
      }
      final Map<String, JsonObject> result = Stream.concat(map1.entrySet().stream(), map2.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, JsonObject::mergeIn));
      return new ArrayList<>(result.values()).stream().sorted(Comparator.comparing(json -> LocalDateTime.parse(json.getString("date"), WeatherAPI.formatter))).collect(Collectors.toList());
    });
  }
}
