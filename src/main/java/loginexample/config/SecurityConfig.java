package loginexample.config;

import loginexample.config.jwt.JwtLoginFilter;
import loginexample.config.jwt.JwtFilter;
import loginexample.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // IoC 빈(bean)을 등록
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CorsConfig corsConfig;

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
                                .requestMatchers("/api/v1/user/**").hasAnyRole(new String[]{"ADMIN", "MANAGER", "USER"})
                                .requestMatchers("/api/v1/manager/**").hasAnyRole(new String[]{"ADMIN", "MANAGER"})
                                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                                .anyRequest().permitAll()
                )

                // 세션을 사용하지 않기 때문에 STATELESS로 설정
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)

                .with(new MyCustomDsl(), customizer -> {
                })

                .build();
    }

    // custom filter 등록
    public class MyCustomDsl extends AbstractHttpConfigurer<MyCustomDsl, HttpSecurity> {
        @Override
        public void configure(HttpSecurity http) throws Exception {
            AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
            http
                    .addFilter(corsConfig.corsFilter())
                    .addFilter(new JwtLoginFilter(authenticationManager))
                    .addFilter(new JwtFilter(authenticationManager, userRepository));
        }
    }
}