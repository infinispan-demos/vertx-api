package cutenames;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.UUID;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class SendCuteNamesAPITest {

   private Vertx vertx;
   private int port;
   private String host;
   private RemoteCacheManager client;

   @Before
   public void setUp(TestContext context) throws IOException {
      vertx = Vertx.vertx();
      ServerSocket socket = new ServerSocket(0);
      host = "127.0.0.1";
      port = socket.getLocalPort();
      socket.close();
      DeploymentOptions options = new DeploymentOptions()
            .setConfig(new JsonObject()
                  .put("http.port", port)
                  .put("infinispan.host", host)
            );
      vertx.deployVerticle(SendCuteNamesAPI.class.getName(), options, context.asyncAssertSuccess());
      Configuration configuration = new ConfigurationBuilder().addServer()
            .host(host)
            .port(11222)
            .build();
      client = new RemoteCacheManager(configuration);
   }

   @After
   public void after(TestContext context) {
      vertx.close(context.asyncAssertSuccess());
      client.getCache().clear();
   }

   @Test
   public void publish_name_working(TestContext context) throws InterruptedException {
      Async async = context.async();

      RemoteCache<String, String> defaultCache = client.getCache();
      defaultCache.put(UUID.randomUUID().toString(), "Elaia");

      EventBus eb = vertx.eventBus();
      eb.consumer(SendCuteNamesAPI.CUTE_NAMES_ADDRESS, message -> {
         context.assertEquals("Elaia", message.body());
         async.complete();
      });
   }

}
