package com.eventloop.model.resources;

import com.eventloop.interfaces.Reservable;
import com.eventloop.interfaces.Reusable;

/**
 * Table resource: Furniture, Reservable, Reusable.
 */
public class Table extends FurnitureResource implements Reservable, Reusable {

    public Table() {
        super();
        this.resourceName = "Folding Banquet Table (6ft)";
        this.purchaseCost = 2800.0;
        this.estimatedRepairCost = 400.0;
    }

    public Table(String resourceId, String resourceName, int quantity, double purchaseCost, 
                 String storageBuilding, String storageRoom) {
        super(resourceId, resourceName, quantity, purchaseCost, storageBuilding, storageRoom);
        this.estimatedRepairCost = 400.0;
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
            return "DIRECT_REUSE: Surface and folding hinges in pristine shape. Perfect for stage/registration.";
        } else if (conditionRating >= 2) {
            return "DIRECT_REUSE_WITH_COVER: Minor surface stains. Use table skirting/cover for professional look.";
        } else {
            return "REPAIR_REQUIRED: Leg lock wobbly. Tighten bracket before reuse.";
        }
    }
}
