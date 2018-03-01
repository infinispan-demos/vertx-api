package consumer;

import java.util.UUID;
import java.util.logging.Logger;

import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;

public class RebootConsumer extends AbstractVerticle {

   public static final String REBOOT_ADDRESS = "reboot";
   private final Logger logger = Logger.getLogger(RebootConsumer.class.getName());
   private boolean reboot = false;
   private String id = "ID-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

   @Override
   public void start(Future<Void> startFuture) throws Exception {
      logger.info("Reboot verticle " + id + " started");
      vertx.eventBus().consumer("ids", message -> {
         int id = ((Integer) message.body()).intValue();

         if (id == 0) {
            launchReboot();
         }
      });

      startFuture.complete();
   }

   private void launchReboot() {
      if (!reboot) {
         reboot = true;
         vertx.eventBus().send(REBOOT_ADDRESS, startRebootMessage());
         logger.info(">> Start system reboot ... ");

         vertx.setTimer(3000, h -> {
            vertx.eventBus().send(REBOOT_ADDRESS, endRebootMessage());
            logger.info("<< Reboot Over");
            reboot = false;
         });
      }
   }

   private JsonObject startRebootMessage() {
      JsonObject message = new JsonObject();
      message.put("status", "STARTED");
      message.put("by", id);
      return message;
   }

   private JsonObject endRebootMessage() {
      JsonObject message = new JsonObject();
      message.put("status", "FINISHED");
      message.put("by", id);
      return message;
   }

   public static void main(String[] args) {
      VertxOptions vertxOptions = new VertxOptions().setClustered(true);
      Vertx.clusteredVertx(vertxOptions, ar -> {
         if (ar.failed()) {
            System.err.println("Cannot create vert.x instance : " + ar.cause());
         } else {
            Vertx vertx = ar.result();
            vertx.deployVerticle(RebootConsumer.class.getName());
         }
      });
   }
}
