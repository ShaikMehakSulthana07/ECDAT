package com.ecdat.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalyzeRequest {

    @JsonAlias({"sourcePath", "projectPath", "dirPath"})
    private String path;
    
    private ProjectAnalysisContext context;

    public AnalyzeRequest() {
    }

    public AnalyzeRequest(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSourcePath() {
        return path;
    }

    public void setSourcePath(String sourcePath) {
        this.path = sourcePath;
    }

    public ProjectAnalysisContext getContext() {
        return context;
    }

    public void setContext(ProjectAnalysisContext context) {
        this.context = context;
    }
}
