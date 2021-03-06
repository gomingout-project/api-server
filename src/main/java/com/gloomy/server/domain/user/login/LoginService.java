package com.gloomy.server.domain.user.login;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gloomy.server.application.jwt.JwtService;
import com.gloomy.server.application.user.UserDTO;
import com.gloomy.server.domain.common.entity.Status;
import com.gloomy.server.domain.jwt.JWTDeserializer;
import com.gloomy.server.domain.jwt.JWTSerializer;
import com.gloomy.server.domain.logout.Logout;
import com.gloomy.server.domain.logout.LogoutRepository;
import com.gloomy.server.domain.user.Type;
import com.gloomy.server.domain.user.User;
import com.gloomy.server.domain.user.UserRepository;
import com.gloomy.server.domain.user.UserService;
import com.gloomy.server.domain.user.kakao.KakaoApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static java.time.Instant.now;

@RequiredArgsConstructor
@Service
@Transactional
public class LoginService {

    private final LoginApiService<UserDTO.KakaoToken, UserDTO.KakaoUser> kakaoApiService;
    private final LoginApiService<UserDTO.KakaoToken, UserDTO.KakaoUser> appleApiService;
    private final UserRepository userRepository;
    private final LogoutRepository logoutRepository;
    private final UserService userService;
    private final JWTSerializer jwtSerializer;
    private final JwtService jwtService;

    public void test(){
        System.out.println(kakaoApiService.getClass());
        System.out.println(appleApiService.getClass());
        appleApiService.logout(1L,"token");
    }

    public User login(UserDTO.CodeRequest request) {
        UserDTO.KakaoToken kakaoToken = kakaoApiService.getToken(request).block();
        UserDTO.KakaoUser kakaoUser =  kakaoApiService.getUserInfo(kakaoToken.getAccess_token()).block();

        Optional<User> userOp =
                userRepository.findFirstByEmailAndJoinStatus(kakaoUser.getKakao_account().getEmail(), Status.ACTIVE);
        User user;
        if(userOp.isEmpty()) { // ????????????
            user=User.of(kakaoUser.getKakao_account().getEmail(), userService.createNickName(),
                    Type.KAKAO, kakaoToken.getAccess_token(),jwtSerializer.createRefreshToken());
        }
        else{ // ?????????
            user=userOp.get();
            user.changeKakaoToken(kakaoToken.getAccess_token());
        }
        userRepository.save(user);

        return user;
    }

    public void logout() throws JsonProcessingException {

        Long userId=jwtService.getMyInfo();

        Optional<User> user =userRepository.findByIdAndJoinStatus(userId, Status.ACTIVE);
        if(user.isEmpty()){
            throw new IllegalArgumentException("[ userService ] ???????????? ?????? user");
        }

        kakaoApiService.logout(userId,user.get().getKakaoToken()); // ????????? ???????????? ??????
        this.jwtLogout();
    }

    protected void jwtLogout() throws JsonProcessingException {
        String token= userService.getToken();
//        long expiredTime=jwtDeserializer.jwtPayloadFromJWT(token).getExpiredTime()-now().getEpochSecond();
        logoutRepository.save(new Logout(token));
    }

}
