package com.eventloop.model.resources;

/**
 * Base class for Decoration resources (Banners, Backdrop frames, Lighting decors).
 */
public class DecorationResource extends AbstractResource {
    public DecorationResource() {
        super();
        this.category = "Decoration";
    }

    public DecorationResource(String resourceId, String resourceName, int quantity, double purchaseCost, 
                              String storageBuilding, String storageRoom) {
        super(resourceId, resourceName, "Decoration", quantity, purchaseCost, storageBuilding, storageRoom);
    }
}
