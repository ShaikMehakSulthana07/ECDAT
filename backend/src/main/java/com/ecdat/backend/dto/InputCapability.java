package com.ecdat.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Metadata descriptor for an input source type and its implementation status.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InputCapability {

    private String type;
    private boolean supported;
    private String displayName;
    private String description;
    private String plannedPhase;

    public InputCapability() {}

    public InputCapability(String type, boolean supported, String displayName, String description, String plannedPhase) {
        this.type = type;
        this.supported = supported;
        this.displayName = displayName;
        this.description = description;
        this.plannedPhase = plannedPhase;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSupported() {
        return supported;
    }

    public void setSupported(boolean supported) {
        this.supported = supported;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlannedPhase() {
        return plannedPhase;
    }

    public void setPlannedPhase(String plannedPhase) {
        this.plannedPhase = plannedPhase;
    }
}
