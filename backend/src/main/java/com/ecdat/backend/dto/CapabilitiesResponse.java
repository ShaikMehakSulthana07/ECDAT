package com.ecdat.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * API response representing available and roadmap ingestion capabilities.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CapabilitiesResponse {

    private List<InputCapability> inputs;

    public CapabilitiesResponse() {
        this.inputs = new ArrayList<>();
    }

    public CapabilitiesResponse(List<InputCapability> inputs) {
        this.inputs = inputs != null ? new ArrayList<>(inputs) : new ArrayList<>();
    }

    public List<InputCapability> getInputs() {
        return inputs;
    }

    public void setInputs(List<InputCapability> inputs) {
        this.inputs = inputs != null ? new ArrayList<>(inputs) : new ArrayList<>();
    }
}
