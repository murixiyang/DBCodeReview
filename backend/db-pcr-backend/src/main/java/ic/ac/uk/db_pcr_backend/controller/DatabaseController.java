package ic.ac.uk.db_pcr_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ic.ac.uk.db_pcr_backend.service.DatabaseService;

@RestController
@RequestMapping("/api")
public class DatabaseController {

    @Autowired
    private DatabaseService databaseSvc;

}
