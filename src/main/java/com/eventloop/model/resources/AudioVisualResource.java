package com.eventloop.model.resources;

/**
 * Base class for Audio/Visual equipment (Projectors, PA Systems, Screens, Mics).
 */
public class AudioVisualResource extends AbstractResource {
    public AudioVisualResource() {
        super();
        this.category = "Audio/Visual";
    }

    public AudioVisualResource(String resourceId, String resourceName, int quantity, double purchaseCost, 
                               String storageBuilding, String storageRoom) {
        super(resourceId, resourceName, "Audio/Visual", quantity, purchaseCost, storageBuilding, storageRoom);
    }
}
