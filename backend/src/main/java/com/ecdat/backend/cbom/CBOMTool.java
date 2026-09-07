package com.ecdat.backend.cbom;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CBOMTool {
    private String vendor = "ECDAT";
    private String name = "Enterprise Cryptographic Discovery & Analysis Tool";
    private String version = "0.0.1-SNAPSHOT";

    // Default constructor for Jackson deserialization
    public CBOMTool() {
    }

    public String getVendor() {
        return vendor;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    // Setters for Jackson deserialization
    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
