package io.archton.scaffold.web;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

@Path("/")
public class HomeRouter {

    private static final Logger log = Logger.getLogger(HomeRouter.class);


    @CheckedTemplate(basePath = "")
    public static class Templates {
        public static native TemplateInstance home();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get() {
        log.debug("GET / - Home page");
        return Templates.home().render();
    }
}
