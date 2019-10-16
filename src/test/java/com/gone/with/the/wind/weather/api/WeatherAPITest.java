package com.gone.with.the.wind.weather.api;

import com.gone.with.the.wind.weather.service.Service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WeatherAPITest {

  private static final String START_DATE = "2018-08-12T00:00:00Z";
  private static final String END_DATE = "2018-08-13T00:00:00Z";
  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
  private static final JsonObject RANDOM_TEMPERATURE = new JsonObject("{\"temp\":10.46941232124016,\"date\":\"2018-08-12T00:00:00Z\"}");
  private static final LocalDateTime LOWER_BOUND = LocalDateTime.of(1900, 1, 1, 0, 0, 0);

  @Mock
  private Service service;

  @Captor
  private ArgumentCaptor<LocalDateTime> dates;

  @Mock
  private HttpServerResponse httpServerResponse;

  @Mock
  private RoutingContext routingContext;

  private WeatherAPI subject;

  @Before
  public void setUp() {
    when(routingContext.response()).thenReturn(httpServerResponse);
    when(httpServerResponse.setStatusCode(200)).thenReturn(httpServerResponse);
    when(httpServerResponse.setStatusCode(400)).thenReturn(httpServerResponse);
    when(httpServerResponse.setStatusCode(500)).thenReturn(httpServerResponse);
    subject = new WeatherAPI(service);
  }

  @Test
  public void fetchDataSuccess() {
    when(service.fetchData(LocalDateTime.parse(START_DATE, formatter), LocalDateTime.parse(END_DATE, formatter))).thenReturn(Future.succeededFuture(Collections.singletonList(RANDOM_TEMPERATURE)));
    when(routingContext.queryParams()).thenReturn(MultiMap.caseInsensitiveMultiMap().set("start", START_DATE).set("end", END_DATE));
    subject.getData(routingContext);
    verify(service).fetchData(dates.capture(), dates.capture());
    final List<LocalDateTime> list = dates.getAllValues();
    assertEquals(asList(LocalDateTime.parse(START_DATE, formatter), LocalDateTime.parse(END_DATE, formatter)), list);
    verify(httpServerResponse).end(Collections.singletonList(RANDOM_TEMPERATURE).toString());
  }

  @Test
  public void fetchDataFailureReturns500WithMessage() {
    when(service.fetchData(LocalDateTime.parse(START_DATE, formatter), LocalDateTime.parse(END_DATE, formatter))).thenReturn(Future.failedFuture("ERROR"));
    when(routingContext.queryParams()).thenReturn(MultiMap.caseInsensitiveMultiMap().set("start", START_DATE).set("end", END_DATE));
    subject.getData(routingContext);
    verify(service).fetchData(dates.capture(), dates.capture());
    final List<LocalDateTime> list = dates.getAllValues();
    assertEquals(asList(LocalDateTime.parse(START_DATE, formatter), LocalDateTime.parse(END_DATE, formatter)), list);
    verify(httpServerResponse).setStatusCode(500);
    verify(httpServerResponse).end("ERROR");
  }

  @Test
  public void laterThanTodayReturns400WithMessage() {
    when(routingContext.queryParams()).thenReturn(MultiMap.caseInsensitiveMultiMap().set("start", START_DATE).set("end", LocalDateTime.now().plusDays(1L).format(formatter)));
    subject.getData(routingContext);
    verifyZeroInteractions(service);
    verify(httpServerResponse).setStatusCode(400);
    verify(httpServerResponse).end("Invalid date range");
  }

  @Test
  public void earlierThanLowerBoundReturns400WithMessage() {
    when(routingContext.queryParams()).thenReturn(MultiMap.caseInsensitiveMultiMap().set("start", LOWER_BOUND.minusSeconds(1L).format(formatter)).set("end", END_DATE));
    subject.getData(routingContext);
    verifyZeroInteractions(service);
    verify(httpServerResponse).setStatusCode(400);
    verify(httpServerResponse).end("Invalid date range");
  }

  @Test
  public void noParamsReturn400WithMessage() {
    when(routingContext.queryParams()).thenReturn(MultiMap.caseInsensitiveMultiMap());
    subject.getData(routingContext);
    verifyZeroInteractions(service);
    verify(httpServerResponse).setStatusCode(400);
    verify(httpServerResponse).end("Invalid request, missing start and/or end date");
  }

  @Test
  public void missingParamsReturn400WithMessage() {
    when(routingContext.queryParams()).thenReturn(MultiMap.caseInsensitiveMultiMap().set("start", START_DATE));
    subject.getData(routingContext);
    verifyZeroInteractions(service);
    verify(httpServerResponse).setStatusCode(400);
    verify(httpServerResponse).end("Invalid request, missing start and/or end date");
  }

}
