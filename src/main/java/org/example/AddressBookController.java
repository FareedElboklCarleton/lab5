package org.example;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;

@Controller
public class AddressBookController {

    private final AddressBookRepository addressBookRepository; // The repository we use to interact with database
    private final BuddyInfoRepository buddyInfoRepository;

    public AddressBookController(AddressBookRepository addressBookRepository,
                                 BuddyInfoRepository buddyInfoRepository) {
        this.addressBookRepository = addressBookRepository;
        this.buddyInfoRepository = buddyInfoRepository;
    }

    // Allows the form to set only the buddy’s name, phone number, and address
    // The database still responsible for assigning the ID.
    @InitBinder("buddy")
    public void bindBuddyFields(WebDataBinder binder) {
        binder.setAllowedFields("name", "phoneNumber", "address");
    }

    @GetMapping({"/", "/addressbooks"})
    @Transactional(readOnly = true)
    public String showAddressBooks(Model model) {
        ArrayList<AddressBook> addressBooks = new ArrayList<>();
        addressBookRepository.findAll().forEach(addressBooks::add);
        model.addAttribute("addressBooks", addressBooks);
        return "addressbooks";
    }

    // Creates an empty book, saves it, and sends your browser back to /
    // Refreshed list includes the new book
    @PostMapping("/addressbooks")
    public String createAddressBook() {
        addressBookRepository.save(new AddressBook());
        return "redirect:/";
    }

    // The webpage controller, different to the HTTP operations that Spring Generates based off of the repository operations
    @GetMapping("/addressbooks/{id}") // The path
    @Transactional(readOnly = true) // Read only interaction with DB
    public String showAddressBook(@PathVariable("id") Integer id, Model model) {
        // Get the address book of specified id (or return 404)
        AddressBook addressBook = addressBookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address book not found"));

        // Update the model with the data
        model.addAttribute("addressBookId", addressBook.getId());
        model.addAttribute("buddies", new ArrayList<>(addressBook.getBuddies()));
        model.addAttribute("buddy", new BuddyInfo());
        return "addressbook"; // Return addressbook.html
    }

    @PostMapping("/addressbooks/{id}/buddies")
    @Transactional
    public String createBuddy(@PathVariable("id") Integer id,
                              @ModelAttribute("buddy") BuddyInfo buddy) {
        AddressBook addressBook = addressBookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address book not found"));

        // Save the buddy first because the address book relationship does not cascade saves.
        BuddyInfo savedBuddy = buddyInfoRepository.save(buddy);
        addressBook.addBuddy(savedBuddy);
        addressBookRepository.save(addressBook);

        return "redirect:/addressbooks/" + id;
    }
}
