package com.ecdat.backend.inventory;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CryptoInventory {
    private List<CryptoAsset> assets;
    private int totalAssets;
    private Map<AssetCategory, Integer> categoryBreakdown;
    private Map<LifecycleStatus, Integer> lifecycleBreakdown;
    private Map<CryptoUsageCategory, Integer> usageBreakdown;
    private String generatedAt;

    public CryptoInventory() {
        this.assets = new ArrayList<>();
        this.categoryBreakdown = new EnumMap<>(AssetCategory.class);
        this.lifecycleBreakdown = new EnumMap<>(LifecycleStatus.class);
        this.usageBreakdown = new EnumMap<>(CryptoUsageCategory.class);
        this.generatedAt = Instant.now().toString();
    }

    public CryptoInventory(List<CryptoAsset> assets) {
        this();
        if (assets != null) {
            this.assets = new ArrayList<>(assets);
            this.totalAssets = this.assets.size();
            computeBreakdowns();
        }
    }

    public void addAsset(CryptoAsset asset) {
        if (asset != null) {
            this.assets.add(asset);
            this.totalAssets = this.assets.size();
            computeBreakdowns();
        }
    }

    private void computeBreakdowns() {
        this.categoryBreakdown.clear();
        this.lifecycleBreakdown.clear();
        this.usageBreakdown.clear();

        for (CryptoAsset asset : assets) {
            AssetCategory cat = asset.getAssetCategory() != null ? asset.getAssetCategory() : AssetCategory.UNKNOWN;
            categoryBreakdown.put(cat, categoryBreakdown.getOrDefault(cat, 0) + 1);

            LifecycleStatus life = asset.getLifecycleStatus() != null ? asset.getLifecycleStatus() : LifecycleStatus.UNKNOWN;
            lifecycleBreakdown.put(life, lifecycleBreakdown.getOrDefault(life, 0) + 1);

            CryptoUsageCategory usage = asset.getUsageCategory() != null ? asset.getUsageCategory() : CryptoUsageCategory.UNKNOWN;
            usageBreakdown.put(usage, usageBreakdown.getOrDefault(usage, 0) + 1);
        }
    }

    public List<CryptoAsset> getAssets() { return assets; }
    public void setAssets(List<CryptoAsset> assets) {
        this.assets = assets != null ? new ArrayList<>(assets) : new ArrayList<>();
        this.totalAssets = this.assets.size();
        computeBreakdowns();
    }

    public int getTotalAssets() { return totalAssets; }
    public void setTotalAssets(int totalAssets) { this.totalAssets = totalAssets; }

    public Map<AssetCategory, Integer> getCategoryBreakdown() { return categoryBreakdown; }
    public void setCategoryBreakdown(Map<AssetCategory, Integer> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }

    public Map<LifecycleStatus, Integer> getLifecycleBreakdown() { return lifecycleBreakdown; }
    public void setLifecycleBreakdown(Map<LifecycleStatus, Integer> lifecycleBreakdown) { this.lifecycleBreakdown = lifecycleBreakdown; }

    public Map<CryptoUsageCategory, Integer> getUsageBreakdown() { return usageBreakdown; }
    public void setUsageBreakdown(Map<CryptoUsageCategory, Integer> usageBreakdown) { this.usageBreakdown = usageBreakdown; }

    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }
}
