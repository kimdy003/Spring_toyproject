package toyproject.noticeBoard.global.jwt.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface JwtService {
    String createAccessToken(String username);
    String createRefreshToken();

    void updateRefreshToken(String username, String refreshToken);

    void destroyRefreshToken(String username);

    void sendToken(HttpServletResponse response, String accessToken, String refreshToken) throws IOException;

    String extractAccessToken(HttpServletRequest request) throws IOException;

    String extractRefreshToken(HttpServletRequest request) throws  IOException;

    String extractUsername(String accessToken);

    void setAccessTokenHeader(HttpServletResponse response, String accessToken);
    void setRefreshTokenHeader(HttpServletResponse response, String refreshToken);
}
