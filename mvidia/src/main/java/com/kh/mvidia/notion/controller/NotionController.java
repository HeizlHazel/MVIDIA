package com.kh.mvidia.notion.controller;

import com.kh.mvidia.notion.model.service.NotionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class NotionController {

    @Autowired
    private NotionServiceImpl nService;


}
