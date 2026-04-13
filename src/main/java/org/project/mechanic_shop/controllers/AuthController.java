package org.project.mechanic_shop.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.project.mechanic_shop.common.responses.ApiResponse;
import org.project.mechanic_shop.config.security.TokenService;
import org.project.mechanic_shop.config.security.UserPrincipal;
import org.project.mechanic_shop.dto.login_dto.LoginDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    String successMessage = "success";

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody @Valid LoginDto data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var userPrincipal = (UserPrincipal) auth.getPrincipal();
        assert userPrincipal != null;
        var token = tokenService.generateToken(userPrincipal.getUsername());



        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), successMessage, token));
    }


}