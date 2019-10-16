package com.gone.with.the.wind.weather.service;

import com.gone.with.the.wind.weather.api.WeatherAPI;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;

import static java.lang.String.format;

public class HTTPClientService implements Service {

  private static final String CLIENT_TEMPLATE_ENDPOINT = "/?at=%s";

  private final HttpClient httpClient;

  public HTTPClientService(final Vertx vertx, final String host, final int port) {
    final HttpClientOptions options = new HttpClientOptions()
      .setTrustAll(true)
      .setDefaultHost(host)
      .setDefaultPort(port);
    this.httpClient = vertx.createHttpClient(options);
  }

  public Future<List<JsonObject>> fetchData(final LocalDateTime start, final LocalDateTime end) {
    final int days = (int) Duration.between(start, end).toDays();
    final List<Future> data = new ArrayList<>();

    for (int i = 0; i <= days; i++) {
      data.add(httpRequest(start.plusDays(i)));
    }
    return CompositeFuture.join(data)
                .map(r -> r.list()
                  .stream()
                  .map(buffer -> new JsonObject(String.valueOf(buffer)))
                  .sorted(Comparator.comparing(json -> LocalDateTime.parse(json.getString("date"), WeatherAPI.formatter)))
                  .collect(Collectors.toList()));
  }

  private Future<Buffer> httpRequest(final LocalDateTime date) {
    final Future<HttpClientResponse> responseFuture = Future.future();
    final Future<Buffer> bufferFuture = Future.future();
    final String url = format(CLIENT_TEMPLATE_ENDPOINT, date.format(WeatherAPI.formatter));
    responseFuture.setHandler(result -> this.handleResponse(result, bufferFuture));
    final HttpClientRequest httpRequest = httpClient.get(url, responseFuture::complete);
    httpRequest.end();
    return bufferFuture;
  }

  private void handleResponse(final AsyncResult<HttpClientResponse> result, final Future<Buffer> bufferFuture) {
    if (result.succeeded()) {
      final HttpClientResponse response = result.result();
      if (response.statusCode() == 200) {
        response.bodyHandler(bufferFuture::complete);
      } else {
        bufferFuture.fail(response.statusMessage());
      }
    } else {
      bufferFuture.fail(result.cause());
    }
  }
}
