package com.civiccomplaint.complaint;

/**
 * Complaint status enumeration.
 * Tracks the lifecycle of a complaint.
 */
public enum ComplaintStatus {
    /**
     * Complaint has been submitted but not yet reviewed
     */
    PENDING,

    /**
     * Complaint is being actively worked on
     */
    IN_PROGRESS,

    /**
     * Complaint has been resolved
     */
    RESOLVED
}
