package loginexample.config.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import loginexample.config.auth.CustomUserDetails;
import loginexample.dto.LoginRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

// [POST] /login case
@Slf4j
@RequiredArgsConstructor
public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {

    // Authentication 객체 만들어서 리턴 => 의존 : AuthenticationManager
    private final AuthenticationManager authenticationManager;

    // 인증 요청시에 실행되는 함수 /login
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        log.info("JwtLoginFilter attemptAuthentication : 진입");

        //클라이언트 요청에서 username, password 추출
        ObjectMapper om = new ObjectMapper();
        LoginRequestDto loginRequestDto = null;
        try {
            // loginRequestDto 객체 생성
            loginRequestDto = om.readValue(request.getInputStream(), LoginRequestDto.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("loginRequestDto : " + loginRequestDto);

        //스프링 시큐리티에서 username과 password를 검증하기 위해서는 token에 담아야 함
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getUsername(),
                        loginRequestDto.getPassword());
        System.out.println("authenticationToken 생성 완료 : " + authenticationToken);

        // Tip: 인증 프로바이더의 디폴트 서비스는 UserDetailsService 타입
        // Tip: 인증 프로바이더의 디폴트 암호화 방식은 BCryptPasswordEncoder
        // 결론은 인증 프로바이더에게 알려줄 필요가 없음.

        //token에 담은 검증을 위한 AuthenticationManager로 전달
        Authentication authentication =
                // authenticate() 함수가 호출 되면 인증 프로바이더가 유저 디테일 서비스의
                // loadUserByUsername(토큰의 첫번째 파라메터) 를 호출하고
                // UserDetails를 리턴받아서 토큰의 두번째 파라메터(credential)과
                // UserDetails(DB값)의 getPassword()함수로 비교해서 동일하면
                // Authentication 객체를 만들어서 필터체인으로 리턴해준다.
                authenticationManager.authenticate(authenticationToken);
        System.out.println("authentication 생성 완료 : " + authentication);

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        System.out.println("Authentication : " + customUserDetails.getUser().getUsername());

        log.info("JwtLoginFilter attemptAuthentication : 퇴장");

        return authentication;
    }

    //로그인 성공시 실행하는 메소드 (여기서 JWT를 발급하면 됨)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {

        log.info("JwtLoginFilter successfulAuthentication : 진입");

        CustomUserDetails customUserDetails = (CustomUserDetails) authResult.getPrincipal();

        String jwtToken = JWT.create()
                .withSubject(customUserDetails.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.EXPIRATION_TIME))
                .withClaim("id", customUserDetails.getUser().getId())
                .withClaim("username", customUserDetails.getUser().getUsername())
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));
        System.out.println("JWT 생성 완료 : " + jwtToken);

        response.addHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + jwtToken);

        log.info("JwtLoginFilter successfulAuthentication : 퇴장");
    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {

    }

}