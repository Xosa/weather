package com.gone.with.the.wind.weather;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class ServerTest {

  private Vertx vertx = Vertx.vertx();

  @After
  public void tearDown() {
    vertx.close();
  }

  @Test
  public void deployServer(final TestContext context) {
    ConfigStoreOptions fileStore = new ConfigStoreOptions()
      .setType("file")
      .setConfig(new JsonObject().put("path", "./config/config-local.json"));

    ConfigStoreOptions sysPropsStore = new ConfigStoreOptions().setType("sys");

    ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(fileStore).addStore(sysPropsStore);
    ConfigRetriever configRetriever = ConfigRetriever.create(vertx, options);

    DeploymentOptions deploymentOptions = new DeploymentOptions();
    configRetriever.getConfig(json -> {
        if (json.succeeded()) {
          deploymentOptions.setConfig(json.result());
          vertx.deployVerticle(Server.class.getName(),
            deploymentOptions,
            context.asyncAssertSuccess()
          );
        }
      }
    );
  }
}
