package com.gloomy.server.application.user;

import com.gloomy.server.application.core.response.ErrorResponse;
import com.gloomy.server.application.core.response.RestResponse;
import com.gloomy.server.application.feed.UpdateFeedDTO;
import com.gloomy.server.application.image.UserProfileImageService;
import com.gloomy.server.domain.feed.Feed;
import com.gloomy.server.domain.jwt.JWTSerializer;
import com.gloomy.server.domain.user.User;
import com.gloomy.server.domain.user.UserService;
import com.gloomy.server.infrastructure.jwt.UserJWTPayload;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.gloomy.server.application.user.UserDTO.*;
import static org.springframework.http.ResponseEntity.*;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class UserRestController {

    private final UserService userService;
    private final JWTSerializer jwtSerializer;
    private final UserProfileImageService userProfileImageService;

    UserRestController(UserService userService, JWTSerializer jwtSerializer, UserProfileImageService userProfileImageService) {
        this.userService = userService;
        this.jwtSerializer = jwtSerializer;
        this.userProfileImageService = userProfileImageService;
    }

    /**
     * 일반 회원가입
     * @param request
     * @return
     */
    @PostMapping(value = "/user" ,produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addUser(@Validated @RequestBody PostRequest request) {
        final User userSaved = userService.signUp(request);
        return ok(new RestResponse<>(200,"user add success"
                ,Response.fromUserAndToken(userSaved, jwtSerializer.jwtFromUser(userSaved),userProfileImageService.findImageByUserId(userSaved).getImageUrl().getImageUrl())));
    }

    @PostMapping(value = "/user/login", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@Validated @RequestBody LoginRequest request) {
        return ok(new RestResponse<>(200,"user login success",
                userService.login(request)
                        .map(user -> Response.fromUserAndToken(user, jwtSerializer.jwtFromUser(user), userProfileImageService.findImageByUserId(user).getImageUrl().getImageUrl()))));
    }

    @PostMapping(value = "/kakao/signUp", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> kakaoLogin(@Validated @RequestBody KakaoCodeRequest request) {
        System.out.println(request.code);
        return ok(new RestResponse<>(200,"user kakao login success",userService.kakaoLogin(request)
                .map(user -> Response.fromUserAndToken(user, jwtSerializer.jwtFromUser(user), userProfileImageService.findImageByUserId(user).getImageUrl().getImageUrl()))));
    }

    @GetMapping(value = "/user")
    public ResponseEntity<Response> getUser(@AuthenticationPrincipal UserJWTPayload jwtPayload) {
        return of(userService.findById(jwtPayload.getUserId())
                .map(user -> Response.fromUserAndToken(user, getCurrentCredential(),userProfileImageService.findImageByUserId(user).getImageUrl().getImageUrl())));
    }

    private static String getCurrentCredential() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getCredentials()
                .toString();
    }

    @PostMapping(value = "/user/update/{userId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateUser(@PathVariable("userId") Long userId
            ,@ModelAttribute UpdateUserDTO.Request updateUserDTO){
        try {
            User updateUser = userService.updateUser(userId,updateUserDTO);
            return ok(new RestResponse<>(200,"user update 성공",makeUpdateUserDTO(updateUser)));
        } catch (IllegalArgumentException e) {
            return badRequest().body(new ErrorResponse(400,"user update 실패",e.getMessage(),updateUserDTO));
        }
    }


    private UpdateUserDTO.Response makeUpdateUserDTO(User user){
        return UpdateUserDTO.Response.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .sex(user.getSex())
                .imageUrl(userProfileImageService.findImageByUserId(user).getImageUrl().getImageUrl())
                .dateOfBirth(user.getDateOfBirth().toString())
                .build();

    }

    @GetMapping(value ="/user/detail/{userId}")
    public ResponseEntity<?> userDetail(@PathVariable("userId")Long userId,Model model){
        try {
            User findUser = userService.findUser(userId);
            return ok(new RestResponse<>(200,"user detail 조회 성공",makeUpdateUserDTO(findUser)));
        } catch (IllegalArgumentException e) {
            return badRequest().body(new ErrorResponse(400,"user detail 조회 실패",e.getMessage(),userId));
        }
    }

    private List<String> makeErrorMessage(String errorMessage, Object errorObject) {
        List<String> errorMessages = new ArrayList<>();
        errorMessages.add(errorMessage);
        errorMessages.add(errorObject.toString());
        return errorMessages;
    }

}
