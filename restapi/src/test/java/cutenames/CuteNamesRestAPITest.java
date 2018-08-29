package cutenames;

import static cutenames.CuteNamesRestAPI.API_ENDPOINT;
import static cutenames.CuteNamesRestAPI.CUTE_NAMES_API_ENDPOINT;

import java.io.IOException;
import java.net.ServerSocket;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;

@RunWith(VertxUnitRunner.class)
public class CuteNamesRestAPITest {

   private Vertx vertx;
   private int port;
   private WebClient webClient;
   private String host;

   private RemoteCache<String, String> defaultCache;

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
      webClient = WebClient.create(vertx);
      vertx.deployVerticle(CuteNamesRestAPI.class.getName(), options, context.asyncAssertSuccess());

      Configuration configuration = new ConfigurationBuilder().addServer()
            .host("localhost")
            .port(11222)
            // We add this to make it possible to access from mac to the docker server.
            // Read https://blog.infinispan.org/2018/03/accessing-infinispan-inside-docker-for.html
            .clientIntelligence(ClientIntelligence.BASIC)
            .build();
      // Create a name by default
      RemoteCacheManager cacheManager = new RemoteCacheManager(configuration);
      defaultCache = cacheManager.getCache();
      defaultCache.put("42", "Oihana");
   }

   @After
   public void after(TestContext context) {
      defaultCache.clear();
      defaultCache.stop();
      vertx.close(context.asyncAssertSuccess());
   }

   @Test
   public void welcome_endpoint(TestContext context) {
      final Async async = context.async();
      webClient.get(port, host, "/").send(context.asyncAssertSuccess(response -> {
         String body = response.body().toString("ISO-8859-1");
         context.assertTrue(body.contains("Welcome"));
         context.assertEquals(200, response.statusCode());
         context.assertEquals("text/html", response.headers().get("content-type"));
         async.complete();
      }));
   }

   @Test
   public void non_existing_endpoint(TestContext context) {
      final Async async = context.async();

      webClient.get(port, host, "/nothing").send(context.asyncAssertSuccess(response -> {
         context.assertEquals(404, response.statusCode());
         async.complete();
      }));
   }

   @Test
   public void api_endpoint(TestContext context) {
      final Async async = context.async();

      webClient.get(port, host, API_ENDPOINT).send(context.asyncAssertSuccess(response -> {
         context.assertEquals(200, response.statusCode());
         context.assertEquals("application/json", response.headers().get("content-type"));
         context.assertEquals("{\"name\":\"cutenames\",\"version\":1}", response.body().toString());
         async.complete();
      }));
   }

   @Test
   public void post_cute_name_endpoint(TestContext context) {
      final Async async = context.async();
      JsonObject body = new JsonObject().put("id", "123").put("name", "Fidelia");
      webClient.post(port, host, CUTE_NAMES_API_ENDPOINT).sendJsonObject(body, context.asyncAssertSuccess(response -> {
         context.assertEquals(201, response.statusCode());
         context.assertEquals("Cute name added", response.body().toString());
         async.complete();
      }));
   }

   @Test
   public void post_cute_name_with_bad_format(TestContext context) {
      final Async async = context.async();
      JsonObject body = new JsonObject().put("id", 123).put("name", "Bad");
      webClient.post(port, host, CUTE_NAMES_API_ENDPOINT).sendJsonObject(body, context.asyncAssertSuccess(response -> {
         context.assertEquals(500, response.statusCode());
         async.complete();
      }));
   }

   @Test
   public void put_cute_name_without_id_endpoint(TestContext context) {
      final Async async = context.async();
      JsonObject body = new JsonObject().put("name", "Elaia");
      webClient.post(port, host, CUTE_NAMES_API_ENDPOINT).sendJsonObject(body, context.asyncAssertSuccess(response -> {
         context.assertEquals(201, response.statusCode());
         context.assertEquals("Cute name added", response.body().toString());
         async.complete();
      }));
   }

   @Test
   public void put_cute_name_without_name(TestContext context) {
      final Async async = context.async();
      JsonObject emptyBody = new JsonObject();
      webClient.post(port, host, CUTE_NAMES_API_ENDPOINT).sendJsonObject(emptyBody, context.asyncAssertSuccess(response -> {
         context.assertEquals(400, response.statusCode());
         context.assertEquals("Body is {}. 'id' and 'name' should be provided", response.body().toString());
         async.complete();
      }));
   }

   @Test
   public void get_cute_name_with_id(TestContext context) {
      final Async async = context.async();
      webClient.get(port, host, CUTE_NAMES_API_ENDPOINT + "/42").send(context.asyncAssertSuccess(response -> {
         context.assertEquals(200, response.statusCode());
         context.assertEquals("{\"name\":\"Oihana\"}", response.body().toString());
         async.complete();
      }));
   }

   @Test
   public void get_cute_name_with_unexisting_id(TestContext context) {
      final Async async = context.async();
      webClient.get(port, host, CUTE_NAMES_API_ENDPOINT + "/666").send(context.asyncAssertSuccess(response -> {
         context.assertEquals(404, response.statusCode());
         context.assertEquals("Cute name 666 not found", response.body().toString());
         async.complete();
      }));
   }
}
