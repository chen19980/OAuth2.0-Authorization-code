package org.example.service.Impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.Entity.User;
import org.example.repository.UserDao;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service(value = "userService")
public class UserServiceImpl implements UserDetailsService, UserService {

    @Resource
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserDao userDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.debug("權限框架-加載用戶");
        List<GrantedAuthority> auths = new ArrayList<>(); //

        User user = userDao.findByUsername(username);
        System.out.println(user);
        if (user == null) {
            log.debug("找不到該用戶 用戶名:{}", username);
            throw new UsernameNotFoundException("can not find user！");
        }

//        List<BaseRole> roles = baseRoleService.selectRolesByUserId(baseUser.getId());
//        if (roles != null) {
//            //設置腳色名稱
//            for (BaseRole role : roles) {
//                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role.getRoleCode());
//                auths.add(authority);
//            }
//        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                true,
                true,
                true,
                true,
                auths
        );
    }

//    用ID認證也可以找到user
//    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
//        log.info("用戶認證，userID：{}", userId);
//        User user = userDao.findByUsername(userId);
//        if (user == null) {
//            throw new UsernameNotFoundException("Invalid username or password.");
//        }
//        return new org.springframework.security.core.userdetails.User(
//                String.valueOf(
//                        user.getId()),
//                user.getPassword(),
//                getAuthority());
//    }

    private List<SimpleGrantedAuthority> getAuthority() {
        return Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        userDao.findAll().iterator().forEachRemaining(list::add);
        return list;
    }

    @Override
    public void delete(long id) {
        userDao.deleteById(id);
    }

    @Override
    public User save(User user) {
        return userDao.save(user);
    }
}
