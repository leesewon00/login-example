package loginexample.controller;

import loginexample.model.User;
import loginexample.config.auth.CustomUserDetails;
import loginexample.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Iterator;

@Slf4j
@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping()
    public String index() {
        return "인덱스 페이지입니다.";
    }

//    @Secured({"ROLE_USER", "ROLE_ADMIN", "ROLE_MANAGER"})
    @GetMapping("/user")
    public String user(@AuthenticationPrincipal CustomUserDetails principal) {
        log.info("Principal : " + principal);
        // iterator 순차 출력 해보기
        Iterator<? extends GrantedAuthority> iter = principal.getAuthorities().iterator();
        while (iter.hasNext()) {
            GrantedAuthority auth = iter.next();
            log.info(auth.getAuthority());
        }
        return "유저 페이지입니다.";
    }

//    @Secured("ROLE_ADMIN")
    @GetMapping("/admin")
    public String admin() {
        return "어드민 페이지입니다.";
    }

//    @PostAuthorize("hasRole('ROLE_MANAGER')")
//    @PreAuthorize("hasRole('ROLE_MANAGER')")
//    @Secured({"ROLE_MANAGER", "ROLE_ADMIN"})
    @GetMapping("/manager")
    public String manager() {
        return "매니저 페이지입니다.";
    }

    @PostMapping("/join")
    public ResponseEntity join(@RequestBody User user) {
        log.info("enter /join " + user);

        String rawPassword = user.getPassword();
        String encPassword = bCryptPasswordEncoder.encode(rawPassword);
        user.setPassword(encPassword);
//        user.setRole("ROLE_USER");
        userRepository.save(user);

        log.info("out /join " + user);
        return ResponseEntity.ok().build();
    }

//    @PostMapping(value = "/loginProc")
//    public void login( @RequestParam("username") String username,
//                       @RequestParam("password") String password){
//        // 시큐리티 login은 x-www-form-url-encoded 타입만 인식
//        // 해당 url로 로그인 접근 가능
//    }

}