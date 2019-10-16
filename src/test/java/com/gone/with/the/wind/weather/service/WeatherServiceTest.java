package com.gone.with.the.wind.weather.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.List;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import static java.util.Arrays.asList;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WeatherServiceTest {

  @Mock
  private Service temperatureService;

  @Mock
  private Service windspeedService;

  private Service subject;

  @Before
  public void setUp() {
    subject = new WeatherService(temperatureService, windspeedService);
  }

  @Test
  public void getData() {
    final JsonObject temp1 = new JsonObject().put("date", "2018-08-12T00:00:00Z").put("temp", 10.46941232124016);
    final JsonObject temp2 = new JsonObject().put("date", "2018-08-11T00:00:00Z").put("temp", 12.846649233810274);
    final JsonObject temp3 = new JsonObject().put("date", "2018-08-10T00:00:00Z").put("temp", 12.846649233810274);
    when(temperatureService.fetchData(any(), any())).thenReturn(Future.succeededFuture(asList(temp3, temp1, temp2)));
    final JsonObject speed1 = new JsonObject().put("date", "2018-08-12T00:00:00Z").put("north", -17.989980201472466).put("west", 16.300917971882726);
    final JsonObject speed2 = new JsonObject().put("date", "2018-08-11T00:00:00Z").put("north", 9.276759379231528).put("west", -5.227840470132605);
    final JsonObject speed3 = new JsonObject().put("date", "2018-08-10T00:00:00Z").put("north", 9.276759379231528).put("west", -5.227840470132605);
    when(windspeedService.fetchData(any(), any())).thenReturn(Future.succeededFuture(asList(speed3, speed1, speed2)));
    final Future<List<JsonObject>> future = subject.fetchData(LocalDateTime.of(2018, 8, 11, 0, 0, 0), LocalDateTime.of(2018, 8, 12, 0, 0, 0));
    await().until(future::isComplete);
    assertTrue(future.succeeded());
    assertEquals(asList(temp3.put("north", 9.276759379231528).put("west", -5.227840470132605), temp2.put("north", 9.276759379231528).put("west", -5.227840470132605), temp1.put("north", -17.989980201472466).put("west", 16.300917971882726)), future.result());
  }

}
