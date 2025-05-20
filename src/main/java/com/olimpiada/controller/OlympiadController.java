package com.olimpiada.controller;

import com.olimpiada.entity.Olympiad;
import com.olimpiada.service.OlympiadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/olympiads")
public class OlympiadController {

    @Autowired
    private OlympiadService olympiadService;

    @GetMapping("/olympiad/list")
    public String listOlympiads(Model model) {
        model.addAttribute("olympiads", olympiadService.findAll());
        return "olympiads/list";
    }
} 