package com.eventloop.model.resources;

import com.eventloop.interfaces.Repairable;
import com.eventloop.interfaces.Reservable;
import com.eventloop.interfaces.Reusable;

/**
 * ExtensionCable resource: Electrical, Reservable, Repairable, Reusable.
 * Damaged electrical equipment must NOT be reservable until repaired and verified.
 */
public class ExtensionCable extends ElectricalResource implements Reservable, Repairable, Reusable {

    public ExtensionCable() {
        super();
        this.resourceName = "Heavy Duty Extension Spike/Cable";
        this.purchaseCost = 750.0;
        this.estimatedRepairCost = 150.0;
    }

    public ExtensionCable(String resourceId, String resourceName, int quantity, double purchaseCost, 
                          String storageBuilding, String storageRoom) {
        super(resourceId, resourceName, quantity, purchaseCost, storageBuilding, storageRoom);
        this.estimatedRepairCost = 150.0;
    }

    @Override
    public boolean reserve() {
        // Strict electrical safety rule: If condition <= 2, block immediately
        if (conditionRating <= 2 || !isReservableState()) {
            return false;
        }
        if (availableQuantity > 0) {
            availableQuantity--;
            if (availableQuantity == 0) {
                currentStatus = "RESERVED";
            }
            return true;
        }
        return false;
    }

    @Override
    public void release() {
        if (availableQuantity < quantity) {
            availableQuantity++;
            if ("RESERVED".equalsIgnoreCase(currentStatus) && availableQuantity > 0) {
                currentStatus = "AVAILABLE";
            }
        }
    }

    @Override
    public double calculateRepairCost() {
        // Plug replacement / insulation rewiring
        return estimatedRepairCost;
    }

    @Override
    public void markUnderRepair() {
        this.currentStatus = "UNDER_REPAIR";
    }

    @Override
    public boolean canBeReused() {
        return conditionRating >= 3 && !"UNDER_REPAIR".equalsIgnoreCase(currentStatus);
    }

    @Override
    public String getReuseRecommendation() {
        if (conditionRating >= 3) {
            return "DIRECT_REUSE: Electrical insulation intact. Safe for indoor/outdoor stage power.";
        } else if (conditionRating == 2) {
            return "REPAIR_REQUIRED: Loose socket or frayed outer insulation. Needs rewiring (est. ₹" + calculateRepairCost() + ") before electrical clearance.";
        } else {
            return "RETIRE: Internal shorting hazard. Discard safely.";
        }
    }
}
