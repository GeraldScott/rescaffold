package io.archton.scaffold.util;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.Year;

/**
 * Utility class for providing configuration values to templates.
 */
@ApplicationScoped
public class TemplateConfig {
    
    /**
     * Gets the current year for display in templates.
     * 
     * @return the current year as an integer
     */
    public int getCurrentYear() {
        return Year.now().getValue();
    }
}