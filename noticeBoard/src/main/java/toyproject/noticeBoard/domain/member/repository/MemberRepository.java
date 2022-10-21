package toyproject.noticeBoard.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import toyproject.noticeBoard.domain.member.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);  // 아이디 찾기

    boolean existsByUsername(String username);  // 아이디 중복 검사

    boolean existsByNickname(String nickname);  // 닉네임 중복 검사
}
