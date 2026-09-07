package com.ecdat.backend.cbom;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CBOMDocument {
    private final String bomFormat = "CycloneDX";
    private final String specVersion = "1.6";
    private String serialNumber;
    private final int version = 1;
    private final List<CBOMComponent> components;
    private final CBOMMetadata metadata;

    public CBOMDocument() {
        this.serialNumber = "urn:uuid:" + UUID.randomUUID().toString();
        this.components = new ArrayList<>();
        this.metadata = new CBOMMetadata();
    }

    public CBOMDocument(List<CBOMComponent> components) {
        this.serialNumber = "urn:uuid:" + UUID.randomUUID().toString();
        this.components = new ArrayList<>(components);
        this.metadata = new CBOMMetadata();
    }

    public String getBomFormat() {
        return bomFormat;
    }

    public String getSpecVersion() {
        return specVersion;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public int getVersion() {
        return version;
    }

    public List<CBOMComponent> getComponents() {
        return new ArrayList<>(components);
    }

    public CBOMMetadata getMetadata() {
        return metadata;
    }

    public void addComponent(CBOMComponent component) {
        this.components.add(component);
    }

    // Setters for Jackson deserialization
    public void setComponents(List<CBOMComponent> components) {
        this.components.clear();
        this.components.addAll(components);
    }

    public void setMetadata(CBOMMetadata metadata) {
        // Metadata is immutable, so we don't set it
    }
}
