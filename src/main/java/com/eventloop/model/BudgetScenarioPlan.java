package com.eventloop.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model representing an AI-generated budget scenario (Plan A, B, or C).
 */
public class BudgetScenarioPlan {
    private String planId; // PLAN_A, PLAN_B, PLAN_C
    private String planName; // e.g. "Plan A — Buy Everything New"
    private String strategyTag; // "MAX_CONVENIENCE", "BALANCED_HYBRID", "MAX_ECONOMY"
    private double totalCost;
    private double budget;
    private double amountSaved;
    private double budgetUtilizationPct;
    private int itemsReused;
    private int itemsRepaired;
    private int itemsBorrowed;
    private int itemsRented;
    private int itemsPurchased;
    private int estimatedLeadTimeDays;
    private int feasibilityScore; // 1-100
    private String recommendationBadge; // e.g. "RECOMMENDED", "EXPENSIVE", "BALANCED"
    private String rationale;
    private List<ItemSourcingDetail> itemBreakdown = new ArrayList<>();

    public static class ItemSourcingDetail {
        private String itemName;
        private int quantity;
        private String sourcingMethod; // DIRECT_REUSE, REPAIR, BORROW, RENT, BUY_NEW
        private double unitCost;
        private double totalCost;
        private String inventorySource; // e.g. "Store Room A", "Vendor FastRent", "ECE Dept"
        private String notes;

        public ItemSourcingDetail() {}

        public ItemSourcingDetail(String itemName, int quantity, String sourcingMethod, double unitCost, double totalCost, String inventorySource, String notes) {
            this.itemName = itemName;
            this.quantity = quantity;
            this.sourcingMethod = sourcingMethod;
            this.unitCost = unitCost;
            this.totalCost = totalCost;
            this.inventorySource = inventorySource;
            this.notes = notes;
        }

        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getSourcingMethod() { return sourcingMethod; }
        public void setSourcingMethod(String sourcingMethod) { this.sourcingMethod = sourcingMethod; }
        public double getUnitCost() { return unitCost; }
        public void setUnitCost(double unitCost) { this.unitCost = unitCost; }
        public double getTotalCost() { return totalCost; }
        public void setTotalCost(double totalCost) { this.totalCost = totalCost; }
        public String getInventorySource() { return inventorySource; }
        public void setInventorySource(String inventorySource) { this.inventorySource = inventorySource; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public BudgetScenarioPlan() {}

    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    public String getStrategyTag() { return strategyTag; }
    public void setStrategyTag(String strategyTag) { this.strategyTag = strategyTag; }
    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }
    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }
    public double getAmountSaved() { return amountSaved; }
    public void setAmountSaved(double amountSaved) { this.amountSaved = amountSaved; }
    public double getBudgetUtilizationPct() { return budgetUtilizationPct; }
    public void setBudgetUtilizationPct(double budgetUtilizationPct) { this.budgetUtilizationPct = budgetUtilizationPct; }
    public int getItemsReused() { return itemsReused; }
    public void setItemsReused(int itemsReused) { this.itemsReused = itemsReused; }
    public int getItemsRepaired() { return itemsRepaired; }
    public void setItemsRepaired(int itemsRepaired) { this.itemsRepaired = itemsRepaired; }
    public int getItemsBorrowed() { return itemsBorrowed; }
    public void setItemsBorrowed(int itemsBorrowed) { this.itemsBorrowed = itemsBorrowed; }
    public int getItemsRented() { return itemsRented; }
    public void setItemsRented(int itemsRented) { this.itemsRented = itemsRented; }
    public int getItemsPurchased() { return itemsPurchased; }
    public void setItemsPurchased(int itemsPurchased) { this.itemsPurchased = itemsPurchased; }
    public int getEstimatedLeadTimeDays() { return estimatedLeadTimeDays; }
    public void setEstimatedLeadTimeDays(int estimatedLeadTimeDays) { this.estimatedLeadTimeDays = estimatedLeadTimeDays; }
    public int getFeasibilityScore() { return feasibilityScore; }
    public void setFeasibilityScore(int feasibilityScore) { this.feasibilityScore = feasibilityScore; }
    public String getRecommendationBadge() { return recommendationBadge; }
    public void setRecommendationBadge(String recommendationBadge) { this.recommendationBadge = recommendationBadge; }
    public String getRationale() { return rationale; }
    public void setRationale(String rationale) { this.rationale = rationale; }
    public List<ItemSourcingDetail> getItemBreakdown() { return itemBreakdown; }
    public void setItemBreakdown(List<ItemSourcingDetail> itemBreakdown) { this.itemBreakdown = itemBreakdown; }
}
