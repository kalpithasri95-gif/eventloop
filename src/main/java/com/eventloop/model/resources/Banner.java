package com.eventloop.model.resources;

import com.eventloop.interfaces.Reusable;

/**
 * Base Banner resource: Decoration, Reusable.
 */
public class Banner extends DecorationResource implements Reusable {

    public Banner() {
        super();
        this.resourceName = "Generic College Banner Frame/Fabric";
        this.purchaseCost = 1500.0;
        this.estimatedRepairCost = 200.0;
        this.reuseType = "DIRECT_REUSE";
    }

    public Banner(String resourceId, String resourceName, int quantity, double purchaseCost, 
                  String storageBuilding, String storageRoom) {
        super(resourceId, resourceName, quantity, purchaseCost, storageBuilding, storageRoom);
        this.reuseType = "DIRECT_REUSE";
    }

    @Override
    public boolean canBeReused() {
        return !"RETIRED".equalsIgnoreCase(currentStatus);
    }

    @Override
    public String getReuseRecommendation() {
        return "DIRECT_REUSE: Generic institutional branding can be reused directly for all formal college events.";
    }
}
