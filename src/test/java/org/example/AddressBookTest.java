package org.example;

import static org.junit.jupiter.api.Assertions.*;

class AddressBookTest {

    @org.junit.jupiter.api.Test
    void addBuddy() {
        AddressBook addressBook = new AddressBook();
        BuddyInfo buddy = new BuddyInfo("John", "613-555-1234");

        addressBook.addBuddy(buddy);

        assertEquals(1, addressBook.getBuddies().size());
        assertEquals(buddy, addressBook.getBuddies().get(0));
    }

    @org.junit.jupiter.api.Test
    void removeBuddy() {
        AddressBook addressBook = new AddressBook();
        BuddyInfo buddy = new BuddyInfo("John", "613-555-1234");

        addressBook.addBuddy(buddy);
        addressBook.removeBuddy(buddy);

        assertEquals(0, addressBook.getBuddies().size());
    }

    @org.junit.jupiter.api.Test
    void getBuddies() {
        AddressBook addressBook = new AddressBook();
        BuddyInfo buddy1 = new BuddyInfo("John", "613-555-1234");
        BuddyInfo buddy2 = new BuddyInfo("Alice", "613-555-5678");

        addressBook.addBuddy(buddy1);
        addressBook.addBuddy(buddy2);

        assertEquals(2, addressBook.getBuddies().size());
        assertEquals(buddy1, addressBook.getBuddies().get(0));
        assertEquals(buddy2, addressBook.getBuddies().get(1));
    }
}