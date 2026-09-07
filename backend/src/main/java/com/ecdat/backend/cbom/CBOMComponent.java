package com.ecdat.backend.cbom;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CBOMComponent {
    private final String type = "cryptographic-asset";
    private String name;
    private String description;
    private CBOMCryptoProperties cryptoProperties;
    private List<CBOMProperty> properties;

    // Default constructor for Jackson deserialization
    public CBOMComponent() {
        this.properties = new ArrayList<>();
    }

    public CBOMComponent(String name, String description, CBOMCryptoProperties cryptoProperties) {
        this.name = name;
        this.description = description;
        this.cryptoProperties = cryptoProperties;
        this.properties = new ArrayList<>();
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public CBOMCryptoProperties getCryptoProperties() {
        return cryptoProperties;
    }

    public List<CBOMProperty> getProperties() {
        return new ArrayList<>(properties);
    }

    public void addProperty(String name, String value) {
        this.properties.add(new CBOMProperty(name, value));
    }

    // Setters for Jackson deserialization
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCryptoProperties(CBOMCryptoProperties cryptoProperties) {
        this.cryptoProperties = cryptoProperties;
    }

    public void setProperties(List<CBOMProperty> properties) {
        this.properties = properties;
    }
}
