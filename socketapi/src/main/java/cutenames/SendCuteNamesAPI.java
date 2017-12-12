package cutenames;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.infinispan.client.hotrod.annotation.ClientCacheEntryCreated;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.event.ClientCacheEntryCreatedEvent;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.sockjs.SockJSHandler;

public class SendCuteNamesAPI extends CacheAccessVerticle {

   public static final String CUTE_NAMES_ADDRESS = "cute-names";
   private final Logger logger = Logger.getLogger(SendCuteNamesAPI.class.getName());

   @Override
   protected void initSuccess() {
      logger.info("Starting SendCuteNamesAPI");
      Router router = Router.router(vertx);

      router.get("/").handler(rc -> {
         rc.response().putHeader("content-type", "text/html")
               .end("Welcome to Send Cute Names API Service");
      });

      SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
      BridgeOptions options = new BridgeOptions();
      options.addOutboundPermitted(new PermittedOptions().setAddress(CUTE_NAMES_ADDRESS));
      sockJSHandler.bridge(options);
      router.route("/eventbus/*").handler(sockJSHandler);

      vertx.createHttpServer()
            .requestHandler(router::accept)
            .rxListen(config().getInteger("http.port", 8080))
            .doOnSuccess(server -> logger.info("HTTP server started"))
            .doOnError(t -> logger.log(Level.SEVERE, "HTTP server failed to start", t))
            .subscribe();
   }

   @Override
   protected void addConfigToCache() {
      logger.info("Added cute names listener");
      defaultCache.addClientListener(new CuteNamesListener());
   }

   @Override
   protected Logger getLogger() {
      return logger;
   }

   @ClientListener
   public final class CuteNamesListener {
      @ClientCacheEntryCreated
      @SuppressWarnings("unused")
      public void created(ClientCacheEntryCreatedEvent<String> e) {
         defaultCache.getAsync(e.getKey()).whenComplete((n, ex) -> {
            logger.info("Publish name " + n);
            vertx.eventBus().publish(CUTE_NAMES_ADDRESS, n);
         });
      }
   }

   public static void main(String[] args) {
      Vertx vertx = Vertx.vertx();
      DeploymentOptions options = new DeploymentOptions()
            .setConfig(new JsonObject()
                  .put("http.port", 8082)
                  .put("infinispan.host", "localhost")
            );
      vertx.deployVerticle(SendCuteNamesAPI.class.getName(), options);
   }
}
