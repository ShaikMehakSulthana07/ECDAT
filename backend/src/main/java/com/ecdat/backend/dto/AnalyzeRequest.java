package com.ecdat.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalyzeRequest {

    @JsonAlias({"sourcePath", "projectPath", "dirPath"})
    private String path;
    
    private String repositoryUrl;
    private String applicationName;
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

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public ProjectAnalysisContext getContext() {
        return context;
    }

    public void setContext(ProjectAnalysisContext context) {
        this.context = context;
    }
}
