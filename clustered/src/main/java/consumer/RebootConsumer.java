package consumer;

import static commons.Config.BY;
import static commons.Config.REBOOT_ADDRESS;
import static commons.Config.STATUS;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.shareddata.Lock;

public class RebootConsumer extends AbstractVerticle {

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
         vertx.sharedData().getLock("lock", ar -> {
            if (ar.succeeded()) {
               Lock lock = ar.result();
               reboot = true;
               vertx.eventBus().send(REBOOT_ADDRESS, startRebootMessage());
               logger.info(">> Start system reboot ... ");

               vertx.setTimer(3000, h -> {
                  vertx.eventBus().send(REBOOT_ADDRESS, endRebootMessage());
                  logger.info("<< Reboot Over");
                  lock.release();
                  reboot = false;
               });

            } else {
               logger.log(Level.SEVERE, "Should work !", ar.cause());
            }
         });
      }
   }

   private JsonObject startRebootMessage() {
      JsonObject message = new JsonObject();
      message.put(STATUS, "STARTED");
      message.put(BY, id);
      return message;
   }

   private JsonObject endRebootMessage() {
      JsonObject message = new JsonObject();
      message.put(STATUS, "FINISHED");
      message.put(BY, id);
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
