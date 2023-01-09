package org.example.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;


@Configuration
@RequiredArgsConstructor
@EnableResourceServer
public class ResourceConfig extends ResourceServerConfigurerAdapter {

    public static final String RESOURCE_ID = "test1"; //若不寫ID則表示不需要驗這個ResourceService


    @Bean
    public static NoOpPasswordEncoder passwordEncoder() {
        return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
    }

    //ResourceService與Token解析
    @Bean
    public ResourceServerTokenServices tokenService() {
        // 使用遠端Service請求ResourceServer驗證Token(其實就是建立一個遠端的Security驗證器) ,必須指定Token的Url, client_id, client_secret
        RemoteTokenServices service = new RemoteTokenServices();
        service.setCheckTokenEndpointUrl("http://localhost:8080/oauth/check_token");//要注意這個連結必須要設置認證後才可以到，沒有認證的話會是拒絕被請求的
        // 宣告只有那個client的接口才可以對ResourceServer做請求
        service.setClientId("chen"); //這邊其實必須加密
        service.setClientSecret("fstop2022");
        return service;
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        // 宣告該Resource的ID ,與認證的TokenService目標
        resources
                .resourceId(RESOURCE_ID)
                .tokenServices(tokenService());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS); //控制Session的關係

        //STATELESS 表示Spring Security不會建立或使用任何Session
        //NEVER 表示框架永遠不會建立Session,但若已經有了,則使用那一個
        //IF_REQUIRED 僅在需要時建立
        //ALWAYS 如果不存在Session，則建立一個

    }
}


//    @Override
//    public void configure(HttpSecurity http) throws Exception {
//        http
//                .csrf().disable()
//                .anonymous().disable()
//                .authorizeRequests()
//                .antMatchers(HttpMethod.OPTIONS).permitAll()
//                // when restricting access to 'Roles' you must remove the "ROLE_" part role
//                // for "ROLE_USER" use only "USER"
//                .antMatchers("/**").access("#oauth2.hasScope('all')")
//                .antMatchers("/hello").access("hasAnyRole('ADMIN')")
//                .antMatchers("/hello").hasRole("ADMIN")
//                // use the full name when specifying authority access
//                .antMatchers("/hello").hasAuthority("ROLE_ADMIN")
//                .and()
//                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//        // restricting all access to /api/** to authenticated users
//    }