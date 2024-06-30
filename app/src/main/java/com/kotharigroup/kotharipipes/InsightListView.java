package com.kotharigroup.kotharipipes;

public class InsightListView {
    private String insightName;
    private String insightOndate;
    private int totalPipesCount;
    public InsightListView(String insightName, String insightOndate, int totalPipesCount){
        this.insightName = insightName;
        this.insightOndate = insightOndate;
        this.totalPipesCount = totalPipesCount;
    }

    public String getInsightName() {
        return insightName;
    }
    public String getInsightOndate() {
        return insightOndate;
    }
    public int getTotalPipesCount(){
        return totalPipesCount;
    }
}
