package com.civiccomplaint.user;

/**
 * User role enumeration.
 * Defines the two types of users in the system.
 */
public enum Role {
    /**
     * Regular citizen who can create and view their own complaints
     */
    CITIZEN,

    /**
     * Administrator who can view and manage all complaints
     */
    ADMIN,

    /**
     * Super Administrator with full system access
     */
    SUPER_ADMIN
}
