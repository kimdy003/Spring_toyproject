package toyproject.noticeBoard.global.jwt.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import toyproject.noticeBoard.domain.member.Member;
import toyproject.noticeBoard.domain.member.RoleType;
import toyproject.noticeBoard.domain.member.repository.MemberRepository;
import toyproject.noticeBoard.global.jwt.service.JwtService;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class JwtAuthenticationProcessingFilterTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager em;

    @Autowired
    JwtService jwtService;

    PasswordEncoder delegatingPasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.access.header}")
    private String accessHeader;
    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    private static String KEY_USERNAME = "username";
    private static String KEY_PASSWORD = "password";
    private static String USERNAME = "kdzero";
    private static String PASSWORD = "123456789";

    private static String LOGIN_RUL = "/login";


    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String BEARER = "Bearer ";


    private ObjectMapper objectMapper = new ObjectMapper();

    private void clear() {
        em.flush();
        em.clear();
    }


    @BeforeEach
    private void init() {
        memberRepository.save(Member.builder()
                .username(USERNAME)
                .password(delegatingPasswordEncoder.encode(PASSWORD))
                .email("kdzero0317@gmail.com")
                .nickname("NickName1")
                .role(RoleType.USER)
                .build());
        clear();
    }

    private Map getUsernamePasswordMap(String username, String password) {
        Map<String, String> map = new HashMap<>();
        map.put(KEY_USERNAME, username);
        map.put(KEY_PASSWORD, password);
        return map;
    }

    private Map getAccessAndRefreshToken() throws Exception {
        Map<String, String> map = getUsernamePasswordMap(USERNAME, PASSWORD);

        MvcResult result = mockMvc.perform(post(LOGIN_RUL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(map)))
                .andReturn();

        String accessToken = result.getResponse().getHeader(accessHeader);
        String refreshToken = result.getResponse().getHeader(refreshHeader);

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put(accessHeader, accessToken);
        tokenMap.put(refreshHeader, refreshToken);

        return tokenMap;
    }

    /**
     * AccessToken : 존재하지 않음
     * RefreshToken : 존재하지 않음
     */
    @Test
    public void Access_Refresh_모두_존재하지_않음() throws Exception {
        //when, then
        mockMvc.perform(get(LOGIN_RUL + "123"))
                .andExpect(status().isForbidden());
    }

    /**
     * AccessToken : 유효,
     * RefreshToken : 존재하지 않음
     */
    @Test
    public void AccessToken만_보내서_인증() throws Exception {
        //given
        Map accessAndRefreshToken = getAccessAndRefreshToken();
        String accessToken= (String) accessAndRefreshToken.get(accessHeader);

        //when, then
        mockMvc.perform(get(LOGIN_RUL+"123").header(accessHeader,BEARER+ accessToken))//login이 아닌 다른 임의의 주소
                .andExpectAll(status().isNotFound());//없는 주소로 보냈으므로 NotFound

    }


    /**
     * AccessToken : 유효하지 않음
     * RefreshToken : 존재하지 않음
     */
    @Test
    public void 안유효한AccessToken만_보내서_인증X_상태코드는_403_NotFound() throws Exception {
        //given
        Map accessAndRefreshToken = getAccessAndRefreshToken();
        String accessToken = (String) accessAndRefreshToken.get(accessHeader);

        //when, then
        mockMvc.perform(get(LOGIN_RUL + "123").header(accessHeader, accessToken + "1"))
                .andExpectAll(status().isForbidden());  // 없는 주소로 보냈으므로 NotFound

    }

    /**
     * AccessToken : 존재하지 않음
     * RefreshToken : 유효
     */
    @Test
    public void 유효한RefreshToken만_보내서_AccessToken_재발급_200() throws Exception {
        //given
        Map accessAndRefreshToken = getAccessAndRefreshToken();
        String refreshToken = (String) accessAndRefreshToken.get(refreshHeader);

        //when
        MvcResult result = mockMvc.perform(get(LOGIN_RUL + "123").header(refreshHeader, BEARER + refreshToken))
                .andExpect(status().isOk()).andReturn();

        String accessToken = result.getResponse().getHeader(accessHeader);

        //then
        String subject = JWT.require(Algorithm.HMAC512(secret)).build().verify(accessToken).getSubject();
        assertThat(subject).isEqualTo(ACCESS_TOKEN_SUBJECT);
    }

    /**
     * AccessToken : 존재하지 않음
     * RefreshToken : 유효하지 않음
     */
    @Test
    public void 안유효한_RefreshToken만_보내면_403() throws Exception {
        //given
        Map accessAndRefreshToken = getAccessAndRefreshToken();
        String refreshToken = (String) accessAndRefreshToken.get(refreshHeader);

        //when, then
        mockMvc.perform(get(LOGIN_RUL + "123").header(refreshHeader, refreshToken))  // Bearer 을 붙이지 않음
                .andExpect(status().isForbidden());
        mockMvc.perform(get(LOGIN_RUL + "123").header(refreshHeader, BEARER + refreshToken + "1"))  // 유효하지 않은 토큰
                .andExpect(status().isForbidden());
    }

    /**
     * AccessToken : 유효
     * RefreshToken : 유효
     */
    @Test
    public void 유효한RefreshToken이랑_유요한AccessToken_같이_보냈을때_AccessToken_재발급_200() throws Exception {
        //given
        Map accessAndRefreshToken = getAccessAndRefreshToken();
        String accessToken = (String) accessAndRefreshToken.get(accessHeader);
        String refreshToken = (String) accessAndRefreshToken.get(refreshHeader);

        //when, then
        MvcResult result = mockMvc.perform(get(LOGIN_RUL + "123")
                        .header(refreshHeader, BEARER + refreshToken)
                        .header(accessHeader, BEARER + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        String responseAccessToken = result.getResponse().getHeader(accessHeader);
        String responseRefreshToken = result.getResponse().getHeader(refreshHeader);

        String subject = JWT.require(Algorithm.HMAC512(secret)).build().verify(responseAccessToken).getSubject();

        assertThat(subject).isEqualTo(ACCESS_TOKEN_SUBJECT);
        assertThat(responseRefreshToken).isNull();  // refreshToken은 재발급되지 않음
    }


    /**
     * AccessToken : 유효
     * RefreshToken : 유효하지 않음
     */
    @Test
    public void 안유효한RefreshToken이랑_유효한AccessToken_같이보냈을때_상태코드200_혹은404_RefreshToken_AccessToken_모두_재발급되지않음() throws Exception {
        //given
        Map accessAndRefreshToken = getAccessAndRefreshToken();
        String accessToken = (String) accessAndRefreshToken.get(accessHeader);
        String refreshToken = (String) accessAndRefreshToken.get(refreshHeader);

        //when, then
        MvcResult result = mockMvc.perform(get(LOGIN_RUL + "123")
                        .header(refreshHeader, BEARER + refreshToken + "1")
                        .header(accessHeader, BEARER + accessToken))
                .andExpect(status().isNotFound())
                .andReturn();

        String responseAccessToken = result.getResponse().getHeader(accessHeader);
        String responseRefreshToken = result.getResponse().getHeader(refreshHeader);

        assertThat(responseAccessToken).isNull();  // accessToken 은 재발급되지 않음
        assertThat(responseRefreshToken).isNull();  // refreshToken 은 재발급되지 않음
    }

    /**
     * AccessToken : 유효하지 않음
     * RefreshToken : 유효하지 않음
     */
    @Test
    public void 안유효한RefreshToken이랑_안유효한AccessToken_같이_보냈을때_403() throws Exception {
        //given
        Map accessAndRefreshToken = getAccessAndRefreshToken();
        String accessToken = (String) accessAndRefreshToken.get(accessHeader);
        String refreshToken = (String) accessAndRefreshToken.get(refreshHeader);

        //when, then
        MvcResult result = mockMvc.perform(get(LOGIN_RUL + "123")
                        .header(refreshHeader, BEARER + refreshToken + "1")
                        .header(accessHeader, BEARER + accessToken + "1"))
                .andExpect(status().isForbidden())
                .andReturn();

        String responseAccessToken = result.getResponse().getHeader(accessHeader);
        String responseRefreshToken = result.getResponse().getHeader(refreshHeader);

        assertThat(responseAccessToken).isNull();  // accessToken 은 재발급되지 않음
        assertThat(responseRefreshToken).isNull();  // refreshToken 은 재발급되지 않음
    }

    @Test
    public void 로그인_주소로_보내면_필터작동_X() throws Exception {
        //given
        Map accessAndRefreshToken = getAccessAndRefreshToken();
        String accessToken = (String) accessAndRefreshToken.get(accessHeader);
        String refreshToken = (String) accessAndRefreshToken.get(refreshHeader);

        //when, then
        MvcResult result = mockMvc.perform(post(LOGIN_RUL)  // get인 경우 config에서 permitAll을 했기에 notfound
                        .header(refreshHeader, BEARER + refreshToken)
                        .header(accessHeader, BEARER + accessToken))
                .andExpect(status().isOk())
                .andReturn();
    }

}