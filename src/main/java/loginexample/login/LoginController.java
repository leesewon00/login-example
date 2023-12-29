package loginexample.login;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import loginexample.member.Member;
import loginexample.SessionConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LoginController {
    @Autowired
    LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity login(@Valid @RequestBody LoginForm form, BindingResult bindingResult,
                                HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            log.info("바인딩 에러");
            return ResponseEntity.badRequest().build();
        }

        Member loginMember = loginService.login(form.getEmail(), form.getPassword());

        if (loginMember == null) {
            log.info("아이디 비밀번호 오류");
            return ResponseEntity.badRequest().build();
        }

        HttpSession session = request.getSession(); // 세션, 쿠키 포괄 관리
        session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);
        log.info("로그인 성공");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok().build();
    }
}
