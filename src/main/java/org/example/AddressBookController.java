package org.example;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;

@Controller
public class AddressBookController {

    private final AddressBookRepository addressBookRepository; // The repository we use to interact with database

    public AddressBookController(AddressBookRepository addressBookRepository) {
        this.addressBookRepository = addressBookRepository;
    }

    @GetMapping("/addressbooks")
    @Transactional(readOnly = true)
    public String showAddressBooks(Model model) {
        ArrayList<AddressBook> addressBooks = new ArrayList<>();
        addressBookRepository.findAll().forEach(addressBooks::add);
        model.addAttribute("addressBooks", addressBooks);
        return "addressbooks";
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
        return "addressbook"; // Return addressbook.html
    }
}
