package io.archton.scaffold.util;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.time.Year;

/**
 * Utility class for providing configuration values and common data to templates.
 */
@ApplicationScoped
public class TemplateConfig {

    @ConfigProperty(name = "quarkus.application.version", defaultValue = "unknown")
    String applicationVersion;

    /**
     * Record to hold template variables.
     */
    public record TemplateVars(int currentYear, String applicationVersion) {}

    /**
     * Gets the template variables for use in templates.
     * 
     * @return a TemplateVars record containing the current year and application version
     */
    public TemplateVars getTemplateVars() {
        return new TemplateVars(getCurrentYear(), getApplicationVersion());
    }

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
