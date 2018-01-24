package consumer;

import java.util.logging.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

public class Monitoring extends AbstractVerticle {

   private final Logger logger = Logger.getLogger(Monitoring.class.getName());

   @Override
   public void start(Future<Void> startFuture) throws Exception {
      logger.info("Reboot monitor started");
      vertx.eventBus().consumer("reboot", message -> {
         JsonObject reboot = (JsonObject) message.body();
         logger.info(("Status " + reboot.getString("status") + " by " + reboot.getString("by")));
      });
      startFuture.complete();
   }

   public static void main(String[] args) {
      VertxOptions vertxOptions = new VertxOptions().setClustered(true);
      Vertx.clusteredVertx(vertxOptions, ar -> {
         if (ar.failed()) {
            System.err.println("Cannot create vert.x instance : " + ar.cause());
         } else {
            Vertx vertx = ar.result();
            vertx.deployVerticle(Monitoring.class.getName());
         }
      });
   }
}
