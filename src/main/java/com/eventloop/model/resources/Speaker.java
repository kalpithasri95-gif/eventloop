package com.eventloop.model.resources;

import com.eventloop.interfaces.Repairable;
import com.eventloop.interfaces.Reservable;
import com.eventloop.interfaces.Reusable;

/**
 * PA / Active Stage Speaker: Audio/Visual, Reservable, Repairable, Reusable.
 */
public class Speaker extends AudioVisualResource implements Reservable, Repairable, Reusable {

    public Speaker() {
        super();
        this.resourceName = "Portable PA Powered Speaker (300W)";
        this.purchaseCost = 22000.0;
        this.estimatedRepairCost = 1200.0;
    }

    public Speaker(String resourceId, String resourceName, int quantity, double purchaseCost, 
                   String storageBuilding, String storageRoom) {
        super(resourceId, resourceName, quantity, purchaseCost, storageBuilding, storageRoom);
        this.estimatedRepairCost = 1200.0;
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
        return conditionRating <= 2 ? 1800.0 : estimatedRepairCost;
    }

    @Override
    public void markUnderRepair() {
        this.currentStatus = "UNDER_REPAIR";
    }

    @Override
    public boolean canBeReused() {
        return conditionRating >= 2 && !"RETIRED".equalsIgnoreCase(currentStatus);
    }

    @Override
    public String getReuseRecommendation() {
        if (conditionRating >= 4) {
            return "DIRECT_REUSE: Drivers and amplifier in excellent condition. Ready for auditorium acoustics.";
        } else if (conditionRating == 3) {
            return "DIRECT_REUSE: Tested clear audio output. Inspect audio cables before live event.";
        } else {
            return "REPAIR_REQUIRED: Woofer distortion detected. Requires capacitor/coil servicing.";
        }
    }
}
