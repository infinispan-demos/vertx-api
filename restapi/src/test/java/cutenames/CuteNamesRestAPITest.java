package cutenames;

import static cutenames.CuteNamesRestAPI.API_ENDPOINT;
import static cutenames.CuteNamesRestAPI.CUTE_NAMES_API_ENDPOINT;

import java.io.IOException;
import java.net.ServerSocket;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpResponse;
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
      webClient = WebClient.wrap(vertx.createHttpClient());
      vertx.deployVerticle(CuteNamesRestAPI.class.getName(), options, context.asyncAssertSuccess());

      // Create a name by default
      RemoteCacheManager cacheManager = new RemoteCacheManager();
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
      webClient.get(port, host, "/").send(ar -> {
         if (ar.succeeded()) {
            HttpResponse<Buffer> response = ar.result();
            String body = response.body().toString("ISO-8859-1");
            context.assertTrue(body.contains("Welcome"));
            context.assertEquals(200, response.statusCode());
            context.assertEquals("text/html", response.headers().get("content-type"));

         } else {
            context.fail("welcome endpoint failed");
         }
         async.complete();
      });
   }

   @Test
   public void non_existing_endpoint(TestContext context) {
      final Async async = context.async();

      webClient.get(port, host, "/nothing").send(ar -> {
         HttpResponse<Buffer> response = ar.result();
         context.assertEquals(404, response.statusCode());
         async.complete();
      });
   }

   @Test
   public void api_endpoint(TestContext context) {
      final Async async = context.async();

      webClient.get(port, host, API_ENDPOINT).send(ar -> {
         HttpResponse<Buffer> response = ar.result();
         context.assertEquals(200, response.statusCode());
         context.assertEquals("application/json", response.headers().get("content-type"));
         context.assertEquals("{\"name\":\"cutenames\",\"version\":1}", response.body().toString());
         async.complete();
      });
   }

   @Test
   public void post_cute_name_endpoint(TestContext context) {
      final Async async = context.async();
      JsonObject body = new JsonObject().put("id", "123").put("name", "Fidelia");
      webClient.post(port, host, CUTE_NAMES_API_ENDPOINT).sendJsonObject(body, ar -> {
         if (ar.succeeded()) {
            HttpResponse<Buffer> response = ar.result();
            context.assertEquals(201, response.statusCode());
            context.assertEquals("Cute name added", response.body().toString());
         } else {
            context.fail(ar.cause());
         }
         async.complete();
      });
   }

   @Test
   public void post_cute_name_with_bad_format(TestContext context) {
      final Async async = context.async();
      JsonObject body = new JsonObject().put("id", 123).put("name", "Bad");
      webClient.post(port, host, CUTE_NAMES_API_ENDPOINT).sendJsonObject(body, ar -> {
         if (ar.succeeded()) {
            HttpResponse<Buffer> response = ar.result();
            context.assertEquals(500, response.statusCode());
         } else {
            context.fail(ar.cause());
         }
         async.complete();
      });
   }

   @Test
   public void put_cute_name_without_id_endpoint(TestContext context) {
      final Async async = context.async();
      JsonObject body = new JsonObject().put("name", "Elaia");
      webClient.post(port, host, CUTE_NAMES_API_ENDPOINT).sendJsonObject(body, ar -> {
         if (ar.succeeded()) {
            HttpResponse<Buffer> response = ar.result();
            context.assertEquals(201, response.statusCode());
            context.assertEquals("Cute name added", response.body().toString());
         } else {
            context.fail(ar.cause());
         }
         async.complete();
      });
   }

   @Test
   public void put_cute_name_without_name(TestContext context) {
      final Async async = context.async();
      JsonObject emptyBody = new JsonObject();
      webClient.post(port, host, CUTE_NAMES_API_ENDPOINT).sendJsonObject(emptyBody, ar -> {
         if (ar.succeeded()) {
            HttpResponse<Buffer> response = ar.result();
            context.assertEquals(400, response.statusCode());
            context.assertEquals("Body is {}. 'id' and 'name' should be provided", response.body().toString());
         } else {
            context.fail(ar.cause());
         }
         async.complete();
      });
   }

   @Test
   public void get_cute_name_with_id(TestContext context) {
      final Async async = context.async();
      webClient.get(port, host, CUTE_NAMES_API_ENDPOINT + "/42").send(ar -> {
         HttpResponse<Buffer> response = ar.result();
         context.assertEquals(200, response.statusCode());
         context.assertEquals("{\"name\":\"Oihana\"}", response.body().toString());
         async.complete();
      });
   }

   @Test
   public void get_cute_name_with_unexisting_id(TestContext context) {
      final Async async = context.async();
      webClient.get(port, host, CUTE_NAMES_API_ENDPOINT + "/666").send(ar -> {
         HttpResponse<Buffer> response = ar.result();
         context.assertEquals(404, response.statusCode());
         context.assertEquals("Cute name 666 not found", response.body().toString());
         async.complete();
      });
   }
}
