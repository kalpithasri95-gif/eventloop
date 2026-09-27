package com.eventloop.service;

import com.eventloop.model.CostComparison;
import com.eventloop.model.RequirementModel;
import com.eventloop.model.resources.AbstractResource;

public class DecisionSupportService {
    private static DecisionSupportService instance;

    private DecisionSupportService() {}

    public static synchronized DecisionSupportService getInstance() {
        if (instance == null) {
            instance = new DecisionSupportService();
        }
        return instance;
    }

    public CostComparison analyzeOptions(RequirementModel req, AbstractResource resource) {
        CostComparison cc = new CostComparison();
        cc.setResourceName(req.getResourceName());
        cc.setCategory(req.getCategory());
        cc.setRequiredQuantity(req.getQuantity());

        double unitBuyCost = (resource != null && resource.getPurchaseCost() > 0) ? resource.getPurchaseCost() : estimateDefaultUnitCost(req.getCategory());
        cc.setNewPurchaseUnitCost(unitBuyCost);
        double totalBuyCost = unitBuyCost * req.getQuantity();
        cc.setTotalBuyCost(totalBuyCost);

        // Potential Purchase Avoided = Required Quantity * New Purchase Unit Cost
        cc.setPotentialPurchaseAvoided(totalBuyCost);

        if (resource != null) {
            cc.setExistingAvailable(resource.isReservableState() && resource.getAvailableQuantity() >= req.getQuantity());
            cc.setAvailableQuantity(resource.getAvailableQuantity());
            cc.setConditionRating(resource.getConditionRating());

            // Reuse Cost: Direct reuse is free or nominal cleaning
            double reuseCost = (resource.getConditionRating() >= 3) ? 0.0 : 100.0 * req.getQuantity();
            cc.setReuseCost(reuseCost);

            // Repair Cost
            double repCost = resource.getEstimatedRepairCost() > 0 ? resource.getEstimatedRepairCost() : (unitBuyCost * 0.15);
            cc.setRepairCost(repCost);

            // Borrow Cost (Handling & inter-dept cartage)
            double borrowCost = Math.round(Math.min(800.0, unitBuyCost * 0.05 * req.getQuantity()));
            cc.setBorrowCost(borrowCost);

            // Rental Cost (Market vendor day rate)
            double rentCost = Math.round(Math.max(300.0, unitBuyCost * 0.12 * req.getQuantity()));
            cc.setRentCost(rentCost);

            // Evaluate Recommendation
            if ("REPURPOSE".equalsIgnoreCase(resource.getReuseType()) || "REPURPOSE_REQUIRED".equalsIgnoreCase(resource.getCurrentStatus())) {
                cc.setRecommendationOption("REPURPOSE");
                cc.setEstimatedSavings(totalBuyCost);
                cc.setRecommendationReason("Resource has past branding or design constraints. Repurpose material (e.g. reverse side backdrop / craft panels) to avoid purchasing new raw supplies.");
            } else if (resource.getConditionRating() >= 3 && resource.isReservableState()) {
                cc.setRecommendationOption("DIRECT_REUSE");
                cc.setEstimatedSavings(totalBuyCost - reuseCost);
                cc.setRecommendationReason("Existing college inventory is verified and in good condition (" + resource.getConditionRating() + 
                        "/5). Direct reuse is 100% recommended. It saves ₹" + String.format("%,.2f", totalBuyCost) + " with zero purchase expense!");
            } else if (resource.getConditionRating() <= 2 || "UNDER_REPAIR".equalsIgnoreCase(resource.getCurrentStatus())) {
                if (repCost <= rentCost) {
                    cc.setRecommendationOption("REPAIR_THEN_REUSE");
                    cc.setEstimatedSavings(totalBuyCost - repCost);
                    cc.setRecommendationReason("Existing resource requires servicing (est. ₹" + repCost + "). Repairing is more economical than renting (₹" + rentCost + ") and permanently restores institutional assets.");
                } else {
                    cc.setRecommendationOption("RENT");
                    cc.setEstimatedSavings(totalBuyCost - rentCost);
                    cc.setRecommendationReason("Repair cost (₹" + repCost + ") is high and repair turnaround may exceed event deadlines. 1-day rental at ₹" + rentCost + " is recommended for immediate operational readiness.");
                }
            } else if (borrowCost < rentCost) {
                cc.setRecommendationOption("BORROW");
                cc.setEstimatedSavings(totalBuyCost - borrowCost);
                cc.setRecommendationReason("Borrowing from sister college department/lab (cartage ₹" + borrowCost + ") saves ₹" + String.format("%,.2f", totalBuyCost - borrowCost) + " compared to buying new.");
            } else {
                cc.setRecommendationOption("RENT");
                cc.setEstimatedSavings(totalBuyCost - rentCost);
                cc.setRecommendationReason("Short term rental is economical for one-off event needs compared to permanent capital expenditure.");
            }
        } else {
            // No resource in inventory
            cc.setExistingAvailable(false);
            cc.setAvailableQuantity(0);
            cc.setConditionRating(0);
            cc.setReuseCost(0);
            cc.setRepairCost(0);

            double borrowCost = Math.round(unitBuyCost * 0.08 * req.getQuantity());
            double rentCost = Math.round(unitBuyCost * 0.18 * req.getQuantity());
            cc.setBorrowCost(borrowCost);
            cc.setRentCost(rentCost);

            cc.setRecommendationOption("BORROW");
            cc.setEstimatedSavings(totalBuyCost - borrowCost);
            cc.setRecommendationReason("No matching resource in campus inventory. Borrowing from partner department or renting (₹" + rentCost + ") is strongly advised before initiating a capital procurement of ₹" + String.format("%,.2f", totalBuyCost) + ".");
        }

        return cc;
    }

    private double estimateDefaultUnitCost(String category) {
        if (category == null) return 1000.0;
        switch (category) {
            case "Audio/Visual": return 30000.0;
            case "Electrical": return 800.0;
            case "Furniture": return 2500.0;
            case "Equipment": return 5000.0;
            case "Decoration": return 1200.0;
            case "Stationery": return 50.0;
            case "Sports": return 3000.0;
            default: return 1500.0;
        }
    }
}
