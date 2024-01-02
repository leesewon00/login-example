package loginexample.config.jwt;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import loginexample.config.auth.CustomUserDetails;
import loginexample.model.Users;
import loginexample.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

// 인가
// 권한이나 인증 필요한 주소 요청시 해당 필터 적용된다.BasicAuthenticationFilter
@Slf4j
public class JwtFilter extends BasicAuthenticationFilter {

    private UserRepository userRepository;

    public JwtFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
        super(authenticationManager);
        this.userRepository = userRepository;
    }

    // 헤더의 토큰 검증 로직
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        log.info("JwtFilter doFilterInternal : 진입");

        String header = request.getHeader(JwtProperties.HEADER_STRING);
        if (header == null || !header.startsWith(JwtProperties.TOKEN_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }
        System.out.println("header : " + header);
        String token = request.getHeader(JwtProperties.HEADER_STRING)
                .replace(JwtProperties.TOKEN_PREFIX, "");

        // 토큰 검증 (이게 인증이기 때문에 AuthenticationManager도 필요 없음)
        // 내가 SecurityContext에 직접접근해서 세션을 만들때 자동으로 UserDetailsService에 있는
        // loadByUsername이 호출됨.
        String username = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(token)
                .getClaim("username").asString();
        System.out.println("토큰 검증 끝");

        if (username != null) {
            Users user = userRepository.findByUsername(username);

            // 인증은 토큰 검증시 끝. 인증을 하기 위해서가 아닌 스프링 시큐리티가 수행해주는 권한 처리를 위해
            // 아래와 같이 토큰을 만들어서 Authentication 객체를 강제로 만들고 그걸 세션에 저장!
            CustomUserDetails customUserDetails = new CustomUserDetails(user);

            // 스프링 시큐리티 인증 토큰 생성
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    customUserDetails, // 나중에 컨트롤러에서 DI해서 쓸 때 사용하기 편함.
                    null, // 패스워드는 모르니까 null 처리, 어차피 지금 인증하는게 아니니까!!
                    customUserDetails.getAuthorities());
            System.out.println("authentication : " + authentication);

            // 강제로 시큐리티의 세션에 접근하여 값 저장
            // 세션에 사용자 등록
            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("시큐리티 세션에 저장 끝");
        }

        log.info("JwtFilter doFilterInternal : 퇴장");

        chain.doFilter(request, response);
    }

}