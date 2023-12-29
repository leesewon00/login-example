package loginexample;

import jakarta.annotation.PostConstruct;
import loginexample.member.Member;
import loginexample.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestDataInit {
    private final MemberRepository memberRepository;

    /**
     * 테스트용 데이터 추가
     */
    @PostConstruct
    public void init() {
        Member member = new Member("string","string");
        memberRepository.save(member);
    }

}