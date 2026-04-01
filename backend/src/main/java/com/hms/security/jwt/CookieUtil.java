package com.hms.security.jwt;

import com.hms.security.config.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    private final JwtProperties jwtProperties;
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    public void setRefreshTokenCookie(HttpServletResponse response, String token) {
        setCookie(response, token, (int) (jwtProperties.getRefreshExpirationMs() / 1000));
    }

    public void clearAuthCookies(HttpServletResponse response) {
        setCookie(response, "", 0);
    }

    public Optional<String> getRefreshToken(HttpServletRequest request) {
        return getCookieValue(request);
    }

    private void setCookie(HttpServletResponse response, String value, int maxAge) {
        Cookie cookie = new Cookie(CookieUtil.REFRESH_TOKEN_COOKIE, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(jwtProperties.isCookieSecure());
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", jwtProperties.getCookieSameSite());
        response.addCookie(cookie);
    }

    private Optional<String> getCookieValue(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(CookieUtil.REFRESH_TOKEN_COOKIE)) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }
}
