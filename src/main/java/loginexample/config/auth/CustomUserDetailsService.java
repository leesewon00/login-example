package loginexample.config.auth;

import loginexample.model.Users;
import loginexample.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("CustomUserDetailsService loadUserByUsername : 진입");
        Users user = userRepository.findByUsername(username);

        // session.setAttribute("loginUser", user);
        log.info("CustomUserDetailsService loadUserByUsername : 퇴장");

        //UserDetails에 담아서 return하면 AutneticationManager가 검증 함
        return new CustomUserDetails(user);
    }
}