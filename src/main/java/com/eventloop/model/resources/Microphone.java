package com.eventloop.model.resources;

import com.eventloop.interfaces.Repairable;
import com.eventloop.interfaces.Reservable;
import com.eventloop.interfaces.Reusable;

/**
 * Microphone: Audio/Visual, Reservable, Repairable, Reusable.
 */
public class Microphone extends AudioVisualResource implements Reservable, Repairable, Reusable {

    public Microphone() {
        super();
        this.resourceName = "Wireless UHF Handheld Microphone Set";
        this.purchaseCost = 6500.0;
        this.estimatedRepairCost = 600.0;
    }

    public Microphone(String resourceId, String resourceName, int quantity, double purchaseCost, 
                      String storageBuilding, String storageRoom) {
        super(resourceId, resourceName, quantity, purchaseCost, storageBuilding, storageRoom);
        this.estimatedRepairCost = 600.0;
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
        return conditionRating <= 2 ? 900.0 : estimatedRepairCost;
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
            return "DIRECT_REUSE: Frequency sync stable and cartridge clean. Ready for podium/anchor use.";
        } else if (conditionRating == 3) {
            return "DIRECT_REUSE: Operational. Replace 9V/AA batteries before event.";
        } else {
            return "REPAIR_REQUIRED: Switch contact intermittent. Requires cleaning and soldering.";
        }
    }
}
