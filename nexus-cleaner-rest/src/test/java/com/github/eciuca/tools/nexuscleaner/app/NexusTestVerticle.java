package com.github.eciuca.tools.nexuscleaner.app;

import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.Router;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

public class NexusTestVerticle extends AbstractVerticle{
    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);

        router
                .route("/nexus/content/repositories/releases/:groupId/:artifactId")
                .handler(req -> {
                    String groupId = req.pathParam("groupId");
                    String artifactId = req.pathParam("artifactId");

                    String nexusResponse = get(groupId, artifactId);

                    req.response().end(nexusResponse);
                });
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8081);
    }

    public String get(@PathParam("groupId") String groupId, @PathParam("artifactId") String artifactId) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<metadata>\n" +
                "  <groupId>" + groupId + "</groupId>\n" +
                "  <artifactId>" + artifactId + "</artifactId>\n" +
                "  <versioning>\n" +
                "    <release>1.8</release>\n" +
                "    <versions>\n" +
                "      <version>1.1</version>\n" +
                "      <version>1.2</version>\n" +
                "      <version>1.3</version>\n" +
                "      <version>1.4</version>\n" +
                "      <version>1.5</version>\n" +
                "      <version>1.6</version>\n" +
                "      <version>1.7</version>\n" +
                "      <version>1.8</version>\n" +
                "    </versions>\n" +
                "    <lastUpdated>20170428122549</lastUpdated>\n" +
                "  </versioning>\n" +
                "</metadata>" ;
    }
}
