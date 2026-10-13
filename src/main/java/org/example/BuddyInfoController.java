package org.example;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;

@Controller
public class BuddyInfoController {

    private final BuddyInfoRepository buddyInfoRepository;

    public BuddyInfoController(BuddyInfoRepository buddyInfoRepository) {
        this.buddyInfoRepository = buddyInfoRepository;
    }

    @GetMapping("/buddies")
    public String showBuddies(Model model) {
        ArrayList<BuddyInfo> buddies = new ArrayList<>();
        buddyInfoRepository.findAll().forEach(buddies::add);
        model.addAttribute("buddies", buddies);
        return "buddies";
    }
}
