package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;

import org.gitlab4j.api.models.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ic.ac.uk.db_pcr_backend.service.GitLabService;
import ic.ac.uk.db_pcr_backend.service.MaintainanceService;

@RestController
@RequestMapping("/api/maintainance")
@PreAuthorize("hasRole('MAINTAINER')")
public class MaintainanceController {

    @Autowired
    private MaintainanceService maintainanceSvc;





}
