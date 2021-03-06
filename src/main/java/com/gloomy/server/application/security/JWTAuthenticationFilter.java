package com.gloomy.server.application.security;

import com.gloomy.server.domain.logout.Logout;
import com.gloomy.server.domain.logout.LogoutRepository;
import com.gloomy.server.domain.logout.LogoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static com.gloomy.server.application.core.ErrorMessage.isLogoutToken;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@RequiredArgsConstructor
class JWTAuthenticationFilter extends OncePerRequestFilter {

//    private final RedisService redisService;
    private final LogoutService logoutService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String s = request.getHeader(AUTHORIZATION);
            JWT jwt=null;

            if (s != null) {
                String token = s.substring("Bearer ".length());

                logoutService.getToken(token);
//                checkLogout(token); // 로그아웃 체크
                jwt=new JWT(token);
            }

            SecurityContextHolder.getContext().setAuthentication(jwt);
            filterChain.doFilter(request, response);

    }
    
    private void checkLogout(String token){
//        String isLogout=redisService.getValue(token);
//        if (!ObjectUtils.isEmpty(isLogout)) { // 블랙리스트에 없을 경우
//            throw new IllegalArgumentException(isLogoutToken);
//        }

    }
    

    @SuppressWarnings("java:S2160")
    static class JWT extends AbstractAuthenticationToken {

        private final String token;

        private JWT(String token) {
            super(null);
            this.token = token;
        }

        @Override
        public Object getPrincipal() {
            return token;
        }

        @Override
        public Object getCredentials() {
            return null;
        }
    }
}