package cutenames;

import java.util.UUID;
import java.util.logging.Logger;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;

public class CuteNamesRestAPI extends CacheAccessVerticle {

   public static final String API_ENDPOINT = "/api";
   public static final String CUTE_NAMES_API_ENDPOINT = "/api/cutenames";

   private final Logger logger = Logger.getLogger(CuteNamesRestAPI.class.getName());

   @Override
   protected void initSuccess() {
      String host = config().getString("http.host", "localhost");
      int port = config().getInteger("http.port", 8080);
      logger.info(String.format("Starting CuteNamesRestAPI in %s:%d", host, port));
      Router router = Router.router(vertx);

      router.get("/").handler(rc -> {
         rc.response().putHeader("content-type", "text/html")
               .end("Welcome to CuteNames API Service");
      });

      router.get(API_ENDPOINT).handler(rc -> {
         rc.response().putHeader("content-type", "application/json")
               .end(new JsonObject().put("name", "cutenames").put("version", 1).encode());
      });

      router.route().handler(BodyHandler.create());
      router.post(CUTE_NAMES_API_ENDPOINT).handler(this::handleAddCuteName);
      router.get(CUTE_NAMES_API_ENDPOINT + "/:id").handler(this::handleGetById);

      vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(port);
   }

   private void handleAddCuteName(RoutingContext rc) {
      logger.info("Add called");
      HttpServerResponse response = rc.response();
      JsonObject bodyAsJson = rc.getBodyAsJson();
      if (bodyAsJson != null && bodyAsJson.containsKey("name")) {
         String id = bodyAsJson.containsKey("id") ? bodyAsJson.getString("id") : UUID.randomUUID().toString();
         defaultCache.putAsync(id, bodyAsJson.getString("name"))
               .thenAccept(s -> {
                  logger.info(String.format("Cute name added [%s]", id));
                  response.setStatusCode(HttpResponseStatus.CREATED.code()).end("Cute name added");
               });
      } else {
         response.setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
               .end(String.format("Body is %s. 'id' and 'name' should be provided", bodyAsJson));
      }
   }

   private void handleGetById(RoutingContext rc) {
      String id = rc.request().getParam("id");
      logger.info("Get by id called id=" + id);
      defaultCache.getAsync(rc.request().getParam("id"))
            .thenAccept(value -> {
               String cuteName;
               if (value == null) {
                  cuteName = String.format("Cute name %s not found", id);
                  rc.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
               } else {
                  cuteName = new JsonObject().put("name", value).encode();
               }
               rc.response().end(cuteName);
            });
   }

   @Override
   protected Logger getLogger() {
      return logger;
   }

   public static void main(String[] args) {
      Vertx vertx = Vertx.vertx();
      DeploymentOptions options = new DeploymentOptions()
            .setConfig(new JsonObject()
                  .put("http.port", 8081)
                  .put("infinispan.host", "localhost")
            );
      vertx.deployVerticle(CuteNamesRestAPI.class.getName(), options);
   }
}
