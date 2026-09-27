package com.eventloop.model.resources;

import com.eventloop.interfaces.Repairable;
import com.eventloop.interfaces.Reservable;
import com.eventloop.interfaces.Reusable;

/**
 * Projector resource demonstrating implementation of multiple interfaces:
 * Resource (via AudioVisualResource), Reservable, Repairable, and Reusable.
 */
public class Projector extends AudioVisualResource implements Reservable, Repairable, Reusable {

    public Projector() {
        super();
        this.resourceName = "HD Projector";
        this.purchaseCost = 45000.0;
        this.estimatedRepairCost = 1500.0;
    }

    public Projector(String resourceId, String resourceName, int quantity, double purchaseCost, 
                     String storageBuilding, String storageRoom) {
        super(resourceId, resourceName, quantity, purchaseCost, storageBuilding, storageRoom);
        this.estimatedRepairCost = 1500.0;
    }

    @Override
    public boolean reserve() {
        if (!isReservableState()) {
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
        // Optical lens cleaning/lamp replacement estimate
        return conditionRating < 3 ? Math.max(2500.0, estimatedRepairCost * 1.5) : estimatedRepairCost;
    }

    @Override
    public void markUnderRepair() {
        this.currentStatus = "UNDER_REPAIR";
    }

    @Override
    public boolean canBeReused() {
        return !"RETIRED".equalsIgnoreCase(currentStatus) && conditionRating >= 2;
    }

    @Override
    public String getReuseRecommendation() {
        if (conditionRating >= 4) {
            return "DIRECT_REUSE: Optics and lamp are in excellent condition. Directly reusable without servicing.";
        } else if (conditionRating == 3) {
            return "DIRECT_REUSE: Fair condition. Check HDMI cable and filter before event.";
        } else if (conditionRating == 2) {
            return "REPAIR_REQUIRED: Lamp dim or color aberration. Requires minor service (est. ₹" + calculateRepairCost() + ") before safe reuse.";
        } else {
            return "RETIRE: Major hardware failure. Unit not economical to repair.";
        }
    }

    @Override
    public void inspect() {
        super.inspect();
        // Electrical and optic check
        if (conditionRating <= 2) {
            markUnderRepair();
        }
    }
}
