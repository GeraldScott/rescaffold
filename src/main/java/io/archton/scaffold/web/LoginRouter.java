package io.archton.scaffold.web;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

@Path("/login")
public class LoginRouter {

    private static final Logger log = Logger.getLogger(LoginRouter.class);

    @CheckedTemplate(basePath = "")
    public static class Templates {
        public static native TemplateInstance login();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get() {
        log.debug("GET /login - Login page");
        return Templates.login().render();
    }
}