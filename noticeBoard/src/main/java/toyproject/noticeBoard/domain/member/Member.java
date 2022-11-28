package toyproject.noticeBoard.domain.member;

import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import toyproject.noticeBoard.domain.common.BaseTimeEntity;

import javax.persistence.*;

@Table(name = "Member")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;  // primary key

    @Column(nullable = false, length = 30, unique = true)
    private String username;  // 아이디

    private String password;  // 비밀번호

    @Column(nullable = false, length = 30)
    private String email;  // 이메일

    @Column(nullable = false, length = 30, unique = true)
    private String nickname;  // 별명

    @Enumerated(EnumType.STRING)
    private RoleType role;  // 권한 -> USER, ADMIN

    @Column(length = 1000)
    private String refreshToken; //RefreshToken


    // == 정보 수정 ==//
    public void updatePassword(PasswordEncoder passwordEncoder, String password) {
        this.password = passwordEncoder.encode(password);
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void destroyRefreshToken() {
        this.refreshToken = null;
    }

    //== password 암호화 ==//
    public void encodePassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(password);
    }
}
