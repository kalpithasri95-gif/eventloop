package com.eventloop.model.resources;

import com.eventloop.interfaces.Reservable;
import com.eventloop.interfaces.Reusable;

/**
 * StandeeFrame: Equipment, Reservable, Reusable.
 * Reusable aluminum roll-up/standee frame that saves buying new hardware every event.
 */
public class StandeeFrame extends EquipmentResource implements Reservable, Reusable {

    public StandeeFrame() {
        super();
        this.resourceName = "Aluminium Roll-up Standee Frame (6x3 ft)";
        this.purchaseCost = 1800.0;
        this.estimatedRepairCost = 250.0;
    }

    public StandeeFrame(String resourceId, String resourceName, int quantity, double purchaseCost, 
                        String storageBuilding, String storageRoom) {
        super(resourceId, resourceName, quantity, purchaseCost, storageBuilding, storageRoom);
        this.estimatedRepairCost = 250.0;
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
        if (conditionRating >= 3) {
            return "DIRECT_REUSE: Standee spring mechanism and locking pin fully functional. Only print flex graphic and slide into existing frame!";
        } else {
            return "REPAIR_REQUIRED: Top clip loose. Replace clip screw before mounting flex.";
        }
    }
}
