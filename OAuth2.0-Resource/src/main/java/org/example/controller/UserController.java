package org.example.controller;


import lombok.RequiredArgsConstructor;

import org.example.Entity.User;
import org.example.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
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

    @CrossOrigin
    @GetMapping("/hello")
    public Object hello(Authentication auth) {
        return "Hello world Oauth2.0 , Resources Server port:8081";
    }
}

