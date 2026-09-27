package com.eventloop.model.resources;

import com.eventloop.interfaces.Reusable;

/**
 * RegistrationKit: Stationery, Reusable.
 * Document folders, clipboards, or seminar delegate kit materials.
 */
public class RegistrationKit extends StationeryResource implements Reusable {

    public RegistrationKit() {
        super();
        this.resourceName = "Executive Delegate Folder & Kit";
        this.purchaseCost = 120.0;
        this.reuseType = "DIRECT_REUSE";
    }

    public RegistrationKit(String resourceId, String resourceName, int quantity, double purchaseCost, 
                           String storageBuilding, String storageRoom) {
        super(resourceId, resourceName, quantity, purchaseCost, storageBuilding, storageRoom);
        this.reuseType = "DIRECT_REUSE";
    }

    @Override
    public boolean canBeReused() {
        return conditionRating >= 3 && !"RETIRED".equalsIgnoreCase(currentStatus);
    }

    @Override
    public String getReuseRecommendation() {
        if (conditionRating >= 4) {
            return "DIRECT_REUSE: Clean folder binder and pen slots. Reuse directly with new event agenda prints.";
        } else {
            return "REPURPOSE: Minor scuff marks. Use for internal volunteer/committee paperwork instead of delegates.";
        }
    }
}
