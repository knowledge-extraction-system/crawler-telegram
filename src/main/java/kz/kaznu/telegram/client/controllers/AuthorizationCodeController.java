package kz.kaznu.telegram.client.controllers;

import kz.kaznu.telegram.client.services.AuthorizationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(value = "/authorization")
public class AuthorizationCodeController {

    private final AuthorizationCodeService authorizationCodeService;

    @Autowired
    public AuthorizationCodeController(AuthorizationCodeService authorizationCodeService) {
        this.authorizationCodeService = authorizationCodeService;
    }

    @RequestMapping(value = "/code", method = RequestMethod.POST)
    public void getAuthorizationCode(@RequestParam String code) throws IOException {
        authorizationCodeService.receiveAuthorizationCode(code);
    }

}
