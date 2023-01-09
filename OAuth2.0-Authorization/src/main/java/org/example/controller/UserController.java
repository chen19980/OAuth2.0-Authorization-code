package org.example.controller;


import lombok.RequiredArgsConstructor;
import org.example.Entity.User;
import org.example.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.example.Entity.Token;

import java.net.URI;
import java.util.Base64;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-security-oauth2-example
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 10/01/18
 * Time: 16.06
 * To change this template use File | Settings | File Templates.
 */

@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    //todo 註冊要用
    @GetMapping(value = "/user_list")
    public List<User> listUser() {
        return userService.findAll();
    }

    @PostMapping(value = "/user_create")
    public User create(@RequestBody User user) {
        return userService.save(user);
    }

    @DeleteMapping(value = "/user/{id}")
    public String delete(@PathVariable(value = "id") Long id) {
        userService.delete(id);
        return "success";
    }

    //todo Resouce認證通過應該要能用Hello api
    @GetMapping("/hello")
    public String hello(Authentication auth) {
        return "Hello world Oauth Resource Service " + auth.getPrincipal();
    }

}
