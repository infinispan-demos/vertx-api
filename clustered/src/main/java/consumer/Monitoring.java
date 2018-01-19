package consumer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

public class Monitoring extends AbstractVerticle {

   @Override
   public void start(Future<Void> startFuture) throws Exception {
      System.out.println("Reboot monitor");
      vertx.eventBus().consumer("reboot", message -> {
         JsonObject reboot = (JsonObject) message.body();
         System.out.println("Status " + reboot.getString("status") + " by " + reboot.getString("by"));
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
