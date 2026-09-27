package com.eventloop.interfaces;

/**
 * Interface for resources that can be serviced, fixed, or refurbished.
 */
public interface Repairable {
    double calculateRepairCost();
    void markUnderRepair();
}
