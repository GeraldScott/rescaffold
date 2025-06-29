package io.archton.scaffold.util;

import io.quarkus.qute.TemplateGlobal;
import org.eclipse.microprofile.config.ConfigProvider;
import java.time.Year;

/**
 * Global template variables accessible in all Qute templates.
 */
@TemplateGlobal
public class TemplateGlobals {

    /**
     * Gets the current year for display in templates.
     * 
     * @return the current year as an integer
     */
    public static int currentYear() {
        return Year.now().getValue();
    }

    /**
     * Gets the application version from configuration.
     * 
     * @return the application version as a string
     */
    public static String applicationVersion() {
        return ConfigProvider.getConfig()
                .getOptionalValue("quarkus.application.version", String.class)
                .orElse("unknown");
    }
}
