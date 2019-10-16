package com.gone.with.the.wind.weather.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.List;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;

import static java.util.Arrays.asList;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HTTPClientServiceTest {

  private static final String TEMP_EXAMPLE = "{\"date\": \"2018-08-12T00:00:00Z\", \"temp\": 10.46941232124016}";
  private static final Buffer BUFFER = Buffer.buffer(TEMP_EXAMPLE);

  @Mock
  public Vertx vertx;

  @Mock
  private HttpClient httpClient;

  @Mock
  private HttpClientResponse httpClientResponse;

  private HTTPClientService subject;

  @Before
  public void setUp() {
    when(vertx.createHttpClient(any())).thenReturn(httpClient);
    subject = new HTTPClientService(vertx, "localhost", 80);
  }

  @Test
  public void getDataSuccess() {
    when(httpClientResponse.statusCode()).thenReturn(200);
    doAnswer(this::mockRequestCallBack).when(httpClient).get(any(String.class), any(Handler.class));
    doAnswer(this::mockBodyCallback).when(httpClientResponse).bodyHandler(any(Handler.class));
    final LocalDateTime yesterday = LocalDateTime.now().toLocalDate().atStartOfDay().minusDays(1L);
    final LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
    final Future<List<JsonObject>> future = subject.fetchData(yesterday, today);
    await().until(future::isComplete);
    assertTrue(future.succeeded());
    verify(httpClient, times(2)).get(any(String.class), any(Handler.class));
    assertEquals(asList(new JsonObject(TEMP_EXAMPLE), new JsonObject(TEMP_EXAMPLE)), future.result());
  }

  @Test
  public void getDataWithUnsuccessfulStatusCodeFails() {
    when(httpClientResponse.statusCode()).thenReturn(500);
    doAnswer(this::mockRequestCallBack).when(httpClient).get(any(String.class), any(Handler.class));
    final LocalDateTime yesterday = LocalDateTime.now().toLocalDate().atStartOfDay().minusDays(1L);
    final LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
    final Future<List<JsonObject>> future = subject.fetchData(yesterday, today);
    await().until(future::isComplete);
    assertTrue(future.failed());
    verify(httpClient, times(2)).get(any(String.class), any(Handler.class));
    verify(httpClientResponse, times(0)).bodyHandler(any(Handler.class));
  }

  private HttpClientResponse mockBodyCallback(final InvocationOnMock invocation) {
    Object[] args = invocation.getArguments();
    Handler<Buffer> handler = (Handler<Buffer>) args[0];
    handler.handle(BUFFER);
    return httpClientResponse;
  }

  private HttpClientRequest mockRequestCallBack(InvocationOnMock invocation) {
    Object[] args = invocation.getArguments();
    Handler<HttpClientResponse> handler = (Handler<HttpClientResponse>) args[1];
    handler.handle(httpClientResponse);
    return mock(HttpClientRequest.class);
  }
}
