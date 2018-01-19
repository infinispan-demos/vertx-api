package producer;

import java.util.concurrent.ThreadLocalRandom;

import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;

public class RandomIdsProducer extends AbstractVerticle {

   @Override
   public void start(Future<Void> startFuture) throws Exception {
      System.out.println("RandomIdsProducer verticle started");

      vertx.setPeriodic(1000, x -> {
         int randomId = ThreadLocalRandom.current().nextInt(0, 3);
         System.out.println("Sending data to 'ids' -> " + randomId);
         vertx.eventBus().send("ids", randomId);
      });
      startFuture.complete();
   }

   public static void main(String[] args) {
      Vertx.clusteredVertx(new VertxOptions().setClustered(true), ar -> {
         if (ar.failed()) {
            System.err.println("Cannot create vert.x instance : " + ar.cause());
         } else {
            Vertx vertx = ar.result();
            vertx.deployVerticle(RandomIdsProducer.class.getName());
         }
      });
   }
}
