package org.example.controller;


import org.example.Entity.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


import java.net.URI;


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
public class UserController {


    @Autowired
    private RestTemplate restTemplate;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @GetMapping(value = "/hello")
    public String app(@RequestParam("code") String code) {
        String accessToken = accessToken(code);

        RequestEntity<Void> request =
                RequestEntity.get(URI.create("http://localhost:8081/hello"))
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .build();

        return "Client app print port:8082 ---> " + restTemplate.exchange(request, String.class).getBody();
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    private String accessToken(String code) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("code", code);
        map.add("grant_type", "authorization_code");
        map.add("redirect_uri", "http://localhost:8082/hello");
        map.add("client_id", "chen");
        map.add("client_secret", "fstop2022");

        RequestEntity<MultiValueMap<String, String>> request =
                RequestEntity.post(URI.create("http://localhost:8080/oauth/token"))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(map);

        return restTemplate.exchange(request, Token.class).getBody().getAccess_token();
    }
}
