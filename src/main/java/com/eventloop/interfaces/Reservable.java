package com.eventloop.interfaces;

/**
 * Interface for resources that can be scheduled and reserved for events.
 */
public interface Reservable {
    boolean reserve();
    void release();
}
