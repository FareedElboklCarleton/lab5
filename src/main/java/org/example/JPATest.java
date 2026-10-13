package org.example;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class JPATest {

    private final BuddyInfoRepository buddyInfoRepository;
    private final AddressBookRepository addressBookRepository;

    public JPATest(BuddyInfoRepository buddyInfoRepository,
                   AddressBookRepository addressBookRepository) {
        this.buddyInfoRepository = buddyInfoRepository;
        this.addressBookRepository = addressBookRepository;
    }

    @Transactional
    public void performJPA() {
        // Save the buddies first because the relationship doesn't cascade saves
        BuddyInfo buddy1 = buddyInfoRepository.save(new BuddyInfo("John", "613-555-1234"));
        BuddyInfo buddy2 = buddyInfoRepository.save(new BuddyInfo("Jane", "613-555-5678"));

        AddressBook addressBook = new AddressBook();
        addressBook.addBuddy(buddy1);
        addressBook.addBuddy(buddy2);
        addressBookRepository.save(addressBook);

        Iterable<AddressBook> results = addressBookRepository.findAll();

        System.out.println("List of address books\n----------------");

        for (AddressBook book : results) {
            System.out.println("AddressBook id=" + book.getId());

            for (BuddyInfo buddy : book.getBuddies()) {
                System.out.println(
                        buddy.getName()
                                + " - "
                                + buddy.getPhoneNumber()
                                + " (id=" + buddy.getId() + ")"
                );
            }
        }
    }
}
