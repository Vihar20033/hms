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
    private static final String ACCESS_TOKEN_COOKIE = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final String LOGGED_IN_CLIENT_COOKIE = "hms_logged_in";

    public void setAccessTokenCookie(HttpServletResponse response, String token) {
        setCookie(response, ACCESS_TOKEN_COOKIE, token, (int) (jwtProperties.getExpirationMs() / 1000), true);
        setCookie(response, LOGGED_IN_CLIENT_COOKIE, "true", (int) (jwtProperties.getExpirationMs() / 1000), false);
    }

    public void setRefreshTokenCookie(HttpServletResponse response, String token) {
        setCookie(response, REFRESH_TOKEN_COOKIE, token, (int) (jwtProperties.getRefreshExpirationMs() / 1000), true);
    }

    public void clearAuthCookies(HttpServletResponse response) {
        setCookie(response, ACCESS_TOKEN_COOKIE, "", 0, true);
        setCookie(response, REFRESH_TOKEN_COOKIE, "", 0, true);
        setCookie(response, LOGGED_IN_CLIENT_COOKIE, "", 0, false);
    }

    public Optional<String> getAccessToken(HttpServletRequest request) {
        return getCookieValue(request, ACCESS_TOKEN_COOKIE);
    }

    public Optional<String> getRefreshToken(HttpServletRequest request) {
        return getCookieValue(request, REFRESH_TOKEN_COOKIE);
    }

    private void setCookie(HttpServletResponse response, String name, String value, int maxAge, boolean httpOnly) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(jwtProperties.isCookieSecure());
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", jwtProperties.getCookieSameSite());
        response.addCookie(cookie);
    }

    private Optional<String> getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return Optional.empty();
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(name)) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }
}
