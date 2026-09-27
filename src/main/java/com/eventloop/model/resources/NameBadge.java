package com.eventloop.model.resources;

import com.eventloop.interfaces.Reusable;

/**
 * NameBadge resource: Stationery/Registration, Reusable.
 * Reusable transparent badge holders and lanyards.
 */
public class NameBadge extends StationeryResource implements Reusable {

    public NameBadge() {
        super();
        this.resourceName = "PVC Name Badge Holder with Lanyard";
        this.purchaseCost = 25.0;
        this.unit = "pcs";
        this.reuseType = "DIRECT_REUSE";
    }

    public NameBadge(String resourceId, String resourceName, int quantity, double purchaseCost, 
                     String storageBuilding, String storageRoom) {
        super(resourceId, resourceName, quantity, purchaseCost, storageBuilding, storageRoom);
        this.unit = "pcs";
        this.reuseType = "DIRECT_REUSE";
    }

    @Override
    public boolean canBeReused() {
        return conditionRating >= 3 && !"RETIRED".equalsIgnoreCase(currentStatus);
    }

    @Override
    public String getReuseRecommendation() {
        return "DIRECT_REUSE: Badge clips and clear PVC sleeves intact. Do not purchase new badge kits; simply print new paper card inserts!";
    }
}
