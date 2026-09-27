package com.eventloop.model.resources;

import com.eventloop.interfaces.Reservable;
import com.eventloop.interfaces.Reusable;

/**
 * Chair resource: Furniture, Reservable, Reusable.
 */
public class Chair extends FurnitureResource implements Reservable, Reusable {

    public Chair() {
        super();
        this.resourceName = "Cushioned Event Chair";
        this.purchaseCost = 1200.0;
        this.estimatedRepairCost = 150.0;
    }

    public Chair(String resourceId, String resourceName, int quantity, double purchaseCost, 
                 String storageBuilding, String storageRoom) {
        super(resourceId, resourceName, quantity, purchaseCost, storageBuilding, storageRoom);
        this.estimatedRepairCost = 150.0;
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
    public boolean canBeReused() {
        return conditionRating >= 2 && !"RETIRED".equalsIgnoreCase(currentStatus);
    }

    @Override
    public String getReuseRecommendation() {
        if (conditionRating >= 4) {
            return "DIRECT_REUSE: Clean fabric cushion, sturdy legs. Excellent for auditorium or VIP seating.";
        } else if (conditionRating >= 2) {
            return "DIRECT_REUSE: Normal wear. Suitable for participant seating.";
        } else {
            return "REPAIR_REQUIRED: Cushion tear or uneven legs. Needs re-upholstery.";
        }
    }
}
