package com.ecdat.backend.cbom;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CBOMMetadata {
    private CBOMTool tool;

    public CBOMMetadata() {
        this.tool = new CBOMTool();
    }

    public CBOMTool getTool() {
        return tool;
    }

    public void setTool(CBOMTool tool) {
        this.tool = tool;
    }
}
