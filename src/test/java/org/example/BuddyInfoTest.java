package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuddyInfoTest {

    @Test
    void getName() {
        BuddyInfo buddy = new BuddyInfo("John", "613-555-1234");

        assertEquals("John", buddy.getName());
    }

    @Test
    void getPhoneNumber() {
        BuddyInfo buddy = new BuddyInfo("John", "613-555-1234");

        assertEquals("613-555-1234", buddy.getPhoneNumber());
    }

    @Test
    void setName() {
        BuddyInfo buddy = new BuddyInfo("John", "613-555-1234");

        buddy.setName("Bob");

        assertEquals("Bob", buddy.getName());
    }

    @Test
    void setPhoneNumber() {
        BuddyInfo buddy = new BuddyInfo("John", "613-555-1234");

        buddy.setPhoneNumber("613-555-9999");

        assertEquals("613-555-9999", buddy.getPhoneNumber());
    }
}