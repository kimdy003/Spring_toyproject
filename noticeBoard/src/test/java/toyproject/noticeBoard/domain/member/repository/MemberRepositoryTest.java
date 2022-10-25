package toyproject.noticeBoard.domain.member.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import toyproject.noticeBoard.domain.member.Member;
import toyproject.noticeBoard.domain.member.RoleType;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
class MemberRepositoryTest {
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    EntityManager em;

    // flush 로 DB에 저장하고, clear 로 1차 캐시를 지우기 때문에
    // find 함수를 사용했을때 DB에 제대로 저장됐는지 확인할 수 있음
    private void clear() {
        em.flush();
        em.clear();
    }

    @AfterEach
    private void after() {
        em.clear();
    }

    @Test
    public void 회원가입_성공() throws Exception {
        //given
        Member member = Member.builder().username("user1").password("123456").email("kdzeor0317@gmail.com").nickname("Nick1").role(RoleType.USER).build();

        //when
        Member save = memberRepository.save(member);

        //then
        Member findMember = memberRepository.findById(save.getId()).orElseThrow(() -> new RuntimeException("저장된 회원이 없습니다."));

        assertThat(findMember).isSameAs(save);
        assertThat(findMember).isSameAs(member);
    }

    @Test
    public void 오류_회원가입시_아이디가_없음() throws Exception {
        //given
        Member member = Member.builder().password("123456").email("kdzeor0317@gmail.com").nickname("Nick1").role(RoleType.USER).build();

        //when, then
        assertThrows(Exception.class, () -> memberRepository.save(member));
    }

    @Test
    public void 오류_회원가입시_닉네임이_없음() throws Exception {
        //given
        Member member = Member.builder().username("user1").password("123456").email("kdzeor0317@gmail.com").role(RoleType.USER).build();

        //when, then
        assertThrows(Exception.class, () -> memberRepository.save(member));
    }

    @Test
    public void 오류_회원가입시_중복된_아이디가_있음() throws Exception {
        //given
        Member member1 = Member.builder().username("user1").password("123456").email("kdzeor0317@gmail.com").nickname("Nick1").role(RoleType.USER).build();
        Member member2 = Member.builder().username("user1").password("11111111").email("kkk7@gmail.com").nickname("Nick2").role(RoleType.USER).build();

        memberRepository.save(member1);
        clear();

        //when, then
        assertThrows(Exception.class, () -> memberRepository.save(member2));
    }

    @Test
    public void 오류_회원가입시_중복된_닉네임이_있음() throws Exception {
        //given
        Member member1 = Member.builder().username("user1").password("123456").email("kdzeor0317@gmail.com").nickname("Nick1").role(RoleType.USER).build();
        Member member2 = Member.builder().username("user2").password("11111111").email("kkk7@gmail.com").nickname("Nick1").role(RoleType.USER).build();

        memberRepository.save(member1);
        clear();

        //when, then
        assertThrows(Exception.class, () -> memberRepository.save(member2));
    }
    
    @Test
    public void 회원_수정() throws Exception {
        //given
        Member member1 = Member.builder().username("user1").password("123456").email("kdzeor0317@gmail.com").nickname("Nick1").role(RoleType.USER).build();
        memberRepository.save(member1);
        clear();

        String updatePassword = "updatePassword";
        String updateNickname = "updateNickname";

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        //when
        Member findMember = memberRepository.findById(member1.getId()).orElseThrow(() -> new Exception());
        findMember.updatePassword(passwordEncoder, updatePassword);
        findMember.updateNickname(updateNickname);
        em.flush();

        //then
        Member findUpdateMember = memberRepository.findById(findMember.getId()).orElseThrow(() -> new Exception());

        assertThat(findMember).isSameAs(findUpdateMember);
        assertThat(passwordEncoder.matches(updatePassword, findUpdateMember.getPassword())).isTrue();
        assertThat(findUpdateMember.getNickname()).isEqualTo(updateNickname);
     }
     
     @Test
     public void 회원_삭제() throws Exception {
         //given
         Member member1 = Member.builder().username("user1").password("123456").email("kdzeor0317@gmail.com").nickname("Nick1").role(RoleType.USER).build();
         memberRepository.save(member1);
         clear();
         
         //when
         memberRepository.delete(member1);
         clear();
         
         //then
         assertThrows(Exception.class, () -> memberRepository.findById(member1.getId()).orElseThrow(() -> new Exception()));
      }
      
      @Test
      public void existsByUsername_정상작동() throws Exception {
          //given
          String username = "user1";
          Member member1 = Member.builder().username(username).password("123456").email("kdzeor0317@gmail.com").nickname("Nick1").role(RoleType.USER).build();
          memberRepository.save(member1);
          clear();
          
          //when, then
          assertThat(memberRepository.existsByUsername(username)).isTrue();
          assertThat(memberRepository.existsByUsername(username+"123")).isFalse();
       }

       @Test
       public void existsByNickname_정상작동() throws Exception {
           //given
           String nickName = "Nick1";
           Member member1 = Member.builder().username("user1").password("123456").email("kdzeor0317@gmail.com").nickname(nickName).role(RoleType.USER).build();
           memberRepository.save(member1);
           clear();

           //when, then
           assertThat(memberRepository.existsByNickname(nickName)).isTrue();
           assertThat(memberRepository.existsByNickname(nickName+"123")).isFalse();
        }

        @Test
        public void findByUsername_정상작동() throws Exception {
            //given
            String username = "user1";
            Member member1 = Member.builder().username(username).password("123456").email("kdzeor0317@gmail.com").nickname("Nick1").role(RoleType.USER).build();
            memberRepository.save(member1);
            clear();

            //when, then
            assertThat(memberRepository.findByUsername(username).get().getUsername()).isEqualTo(member1.getUsername());
            assertThat(memberRepository.findByUsername(username).get().getNickname()).isEqualTo(member1.getNickname());
            assertThat(memberRepository.findByUsername(username).get().getId()).isEqualTo(member1.getId());
            assertThrows(Exception.class, () -> memberRepository.findByUsername(username+"123").orElseThrow(() -> new Exception()));
         }

         @Test
         public void 회원가입시_생성시간_등록확인() throws Exception {
             //given
             Member member1 = Member.builder().username("user1").password("123456").email("kdzeor0317@gmail.com").nickname("Nick1").role(RoleType.USER).build();
             memberRepository.save(member1);
             clear();

             //when
             Member findMember = memberRepository.findById(member1.getId()).orElseThrow(() -> new Exception());

             //then
             assertThat(findMember.getCreateData()).isNotNull();
             assertThat(findMember.getLastModifiedDate()).isNotNull();
          }
}