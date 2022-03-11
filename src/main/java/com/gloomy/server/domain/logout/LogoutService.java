package com.gloomy.server.domain.logout;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LogoutService {

    private final LogoutRepository logoutRepository;

    public Optional<Logout> getToken(String logoutToken){
        return logoutRepository.findByLogoutToken(logoutToken);
    }
}