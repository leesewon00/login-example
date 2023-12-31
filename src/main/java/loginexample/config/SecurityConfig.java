package loginexample.config;

import loginexample.config.oauth.CustomOauth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // IoC 빈(bean)을 등록
//@EnableMethodSecurity
//@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomOauth2UserService customOauth2UserService;

    @Bean
    public BCryptPasswordEncoder encodePwd() {
        return new BCryptPasswordEncoder();
    }

    // 시큐리티 login은 x-www-form-url-encoded 타입만 인식
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests((authz) -> authz
//                        .requestMatchers("/user/**", "/admin/**", "/manager/**").authenticated()
                        // 직접 설정해줘야한다. annotation 불능
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/manager/**").hasAnyRole(new String[]{"ADMIN", "MANAGER"})
                        .requestMatchers("/user/**").hasAnyRole(new String[]{"ADMIN", "MANAGER", "USER"})
                        .anyRequest().permitAll()
                )
                .formLogin((formLogin) ->
                        // 시큐리티 login은 x-www-form-url-encoded 타입만 인식
                        formLogin
                                .loginProcessingUrl("/loginProc")
                                .defaultSuccessUrl("/")
                )
                .oauth2Login((oauth2) ->
                        oauth2
                                .userInfoEndpoint(userInfoEndpoint ->
                                        userInfoEndpoint
                                                .userService(customOauth2UserService)
                                )
                )

                .csrf(AbstractHttpConfigurer::disable)
                .build();

    }
}