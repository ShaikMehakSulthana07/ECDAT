package com.ecdat.backend.dto;

import com.ecdat.backend.cbom.CBOMDocument;
import com.ecdat.backend.inventory.CryptoAsset;
import com.ecdat.backend.inventory.CryptoInventory;
import com.ecdat.backend.pqc.PQCRecommendation;
import com.ecdat.backend.risk.RiskAssessment;
import com.ecdat.backend.scanner.CryptoFinding;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalysisResponse {
    private String status;
    private String sourcePath;
    private List<CryptoFinding> findings;
    private List<RiskAssessment> riskAssessments;
    private List<PQCRecommendation> pqcRecommendations;
    private List<CryptoAsset> cryptoAssets;
    private CryptoInventory inventory;
    private CBOMDocument cbom;
    private AnalysisSummary summary;

    public AnalysisResponse() {
        this.findings = new ArrayList<>();
        this.riskAssessments = new ArrayList<>();
        this.pqcRecommendations = new ArrayList<>();
        this.cryptoAssets = new ArrayList<>();
    }

    public AnalysisResponse(String status, String sourcePath, List<CryptoFinding> findings,
                            List<RiskAssessment> riskAssessments, List<PQCRecommendation> pqcRecommendations,
                            CBOMDocument cbom, AnalysisSummary summary) {
        this.status = status;
        this.sourcePath = sourcePath;
        this.findings = findings != null ? new ArrayList<>(findings) : new ArrayList<>();
        this.riskAssessments = riskAssessments != null ? new ArrayList<>(riskAssessments) : new ArrayList<>();
        this.pqcRecommendations = pqcRecommendations != null ? new ArrayList<>(pqcRecommendations) : new ArrayList<>();
        this.cryptoAssets = new ArrayList<>();
        this.cbom = cbom;
        this.summary = summary;
    }

    public AnalysisResponse(String status, String sourcePath, List<CryptoFinding> findings,
                            List<RiskAssessment> riskAssessments, List<PQCRecommendation> pqcRecommendations,
                            List<CryptoAsset> cryptoAssets, CryptoInventory inventory,
                            CBOMDocument cbom, AnalysisSummary summary) {
        this.status = status;
        this.sourcePath = sourcePath;
        this.findings = findings != null ? new ArrayList<>(findings) : new ArrayList<>();
        this.riskAssessments = riskAssessments != null ? new ArrayList<>(riskAssessments) : new ArrayList<>();
        this.pqcRecommendations = pqcRecommendations != null ? new ArrayList<>(pqcRecommendations) : new ArrayList<>();
        this.cryptoAssets = cryptoAssets != null ? new ArrayList<>(cryptoAssets) : new ArrayList<>();
        this.inventory = inventory;
        this.cbom = cbom;
        this.summary = summary;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSourcePath() { return sourcePath; }
    public void setSourcePath(String sourcePath) { this.sourcePath = sourcePath; }

    public List<CryptoFinding> getFindings() { return new ArrayList<>(findings); }
    public void setFindings(List<CryptoFinding> findings) {
        this.findings = findings != null ? new ArrayList<>(findings) : new ArrayList<>();
    }

    public List<RiskAssessment> getRiskAssessments() { return new ArrayList<>(riskAssessments); }
    public void setRiskAssessments(List<RiskAssessment> riskAssessments) {
        this.riskAssessments = riskAssessments != null ? new ArrayList<>(riskAssessments) : new ArrayList<>();
    }

    public List<PQCRecommendation> getPqcRecommendations() { return new ArrayList<>(pqcRecommendations); }
    public void setPqcRecommendations(List<PQCRecommendation> pqcRecommendations) {
        this.pqcRecommendations = pqcRecommendations != null ? new ArrayList<>(pqcRecommendations) : new ArrayList<>();
    }

    public List<CryptoAsset> getCryptoAssets() { return new ArrayList<>(cryptoAssets); }
    public void setCryptoAssets(List<CryptoAsset> cryptoAssets) {
        this.cryptoAssets = cryptoAssets != null ? new ArrayList<>(cryptoAssets) : new ArrayList<>();
    }

    public CryptoInventory getInventory() { return inventory; }
    public void setInventory(CryptoInventory inventory) { this.inventory = inventory; }

    public CBOMDocument getCbom() { return cbom; }
    public void setCbom(CBOMDocument cbom) { this.cbom = cbom; }

    public AnalysisSummary getSummary() { return summary; }
    public void setSummary(AnalysisSummary summary) { this.summary = summary; }
}
