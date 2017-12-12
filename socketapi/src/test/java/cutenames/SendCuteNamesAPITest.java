package cutenames;

import java.io.IOException;
import java.net.ServerSocket;

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
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class SendCuteNamesAPITest {

   private Vertx vertx;
   private int port;
   private HttpClient httpClient;
   private String host;

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
      httpClient = vertx.createHttpClient();
      vertx.deployVerticle(SendCuteNamesAPI.class.getName(), options, context.asyncAssertSuccess());
   }

   @After
   public void after(TestContext context) {
      vertx.close(context.asyncAssertSuccess());
   }

   @Test
   public void sockets() {
      Configuration configuration = new ConfigurationBuilder().addServer()
            .host(host)
            .port(11222)
            .build();

      RemoteCacheManager client = new RemoteCacheManager(configuration);
      RemoteCache<Integer, String> defaultCache = client.getCache();
      defaultCache.put(42, "Oihana");

      httpClient.websocket(port, host, "/eventbus/websocket", ws -> {
         System.out.println("Connected");
         sendPing(ws);

         // Send pings periodically to avoid the websocket connection being closed
         vertx.setPeriodic(5000, id -> {
            sendPing(ws);
         });

         // Register
         JsonObject msg = new JsonObject().put("type", "register").put("address", SendCuteNamesAPI.CUTE_NAMES_ADDRESS);
         ws.writeFrame(WebSocketFrame.textFrame(msg.encode(), true));

         ws.handler(buff -> {
            System.out.println(buff);
            JsonObject json = new JsonObject(buff.toString()).getJsonObject("body");
            System.out.println(json);
         });
      });
   }

   static void sendPing(WebSocket ws) {
      JsonObject msg = new JsonObject().put("type", "ping");
      ws.writeFrame(WebSocketFrame.textFrame(msg.encode(), true));
   }

}
