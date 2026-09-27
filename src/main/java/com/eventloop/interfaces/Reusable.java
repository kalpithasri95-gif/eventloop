package com.eventloop.interfaces;

/**
 * Interface for resources that support reuse or repurposing evaluation.
 */
public interface Reusable {
    boolean canBeReused();
    String getReuseRecommendation();
}
