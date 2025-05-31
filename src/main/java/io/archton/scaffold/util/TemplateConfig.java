package io.archton.scaffold.util;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.time.Year;

/**
 * Utility class for providing configuration values to templates.
 */
@ApplicationScoped
public class TemplateConfig {

    @ConfigProperty(name = "quarkus.application.version", defaultValue = "unknown")
    String applicationVersion;

    /**
     * Gets the current year for display in templates.
     * 
     * @return the current year as an integer
     */
    public int getCurrentYear() {
        return Year.now().getValue();
    }

    /**
     * Gets the application version from configuration.
     * 
     * @return the application version as a string
     */
    public String getApplicationVersion() {
        return applicationVersion;
    }
}
