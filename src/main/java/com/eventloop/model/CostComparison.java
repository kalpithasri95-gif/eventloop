package com.eventloop.model;

/**
 * Pre-Purchase Decision Support Model.
 * Compares Reuse, Repair, Borrow, Rent, and Buy options with economic reasoning.
 */
public class CostComparison {
    private String resourceName;
    private String category;
    private int requiredQuantity;
    private int availableQuantity;
    private int conditionRating;
    private boolean isExistingAvailable;

    private double reuseCost;
    private double repairCost;
    private double borrowCost;
    private double rentCost;
    private double newPurchaseUnitCost;
    private double totalBuyCost;
    private double potentialPurchaseAvoided;
    private double estimatedSavings;

    private String recommendationOption; // DIRECT_REUSE, REPAIR_THEN_REUSE, BORROW, RENT, BUY, REPURPOSE
    private String recommendationReason;

    public CostComparison() {}

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getRequiredQuantity() { return requiredQuantity; }
    public void setRequiredQuantity(int requiredQuantity) { this.requiredQuantity = requiredQuantity; }

    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }

    public int getConditionRating() { return conditionRating; }
    public void setConditionRating(int conditionRating) { this.conditionRating = conditionRating; }

    public boolean isExistingAvailable() { return isExistingAvailable; }
    public void setExistingAvailable(boolean existingAvailable) { isExistingAvailable = existingAvailable; }

    public double getReuseCost() { return reuseCost; }
    public void setReuseCost(double reuseCost) { this.reuseCost = reuseCost; }

    public double getRepairCost() { return repairCost; }
    public void setRepairCost(double repairCost) { this.repairCost = repairCost; }

    public double getBorrowCost() { return borrowCost; }
    public void setBorrowCost(double borrowCost) { this.borrowCost = borrowCost; }

    public double getRentCost() { return rentCost; }
    public void setRentCost(double rentCost) { this.rentCost = rentCost; }

    public double getNewPurchaseUnitCost() { return newPurchaseUnitCost; }
    public void setNewPurchaseUnitCost(double newPurchaseUnitCost) { this.newPurchaseUnitCost = newPurchaseUnitCost; }

    public double getTotalBuyCost() { return totalBuyCost; }
    public void setTotalBuyCost(double totalBuyCost) { this.totalBuyCost = totalBuyCost; }

    public double getPotentialPurchaseAvoided() { return potentialPurchaseAvoided; }
    public void setPotentialPurchaseAvoided(double potentialPurchaseAvoided) { this.potentialPurchaseAvoided = potentialPurchaseAvoided; }

    public double getEstimatedSavings() { return estimatedSavings; }
    public void setEstimatedSavings(double estimatedSavings) { this.estimatedSavings = estimatedSavings; }

    public String getRecommendationOption() { return recommendationOption; }
    public void setRecommendationOption(String recommendationOption) { this.recommendationOption = recommendationOption; }

    public String getRecommendationReason() { return recommendationReason; }
    public void setRecommendationReason(String recommendationReason) { this.recommendationReason = recommendationReason; }
}
