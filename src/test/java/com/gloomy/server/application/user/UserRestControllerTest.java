package com.gloomy.server.application.user;

import com.gloomy.server.application.AbstractControllerTest;
import com.gloomy.server.application.image.TestImage;
import com.gloomy.server.application.security.JWTAuthenticationProvider;
import com.gloomy.server.domain.jwt.JWTSerializer;
import com.gloomy.server.domain.user.User;
import com.gloomy.server.domain.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static com.gloomy.server.application.user.UserDTO.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.fileUpload;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "spring.config.location=classpath:test-application.yml,classpath:aws.yml"
})
@Transactional
class UserRestControllerTest extends AbstractControllerTest {

    @Autowired
    UserService userService;
    @Autowired
    JWTSerializer jwtSerializer;

    @Autowired JWTAuthenticationProvider jwtAuthenticationProvider;

    TestImage testImage;
    User user;
    UpdateUserDTO.Request updateUserDTO;
    Authentication authentication;
    MultipartFile profileImage;

    @BeforeEach
    public void setUp(){
        authentication= SecurityContextHolder.getContext().getAuthentication();
        this.user= TestUserDTO.TestUser.makeTestUser();
        user.changeId(100L);
        testImage=new TestImage();
        profileImage=testImage.makeImages(1).get(0);
        this.updateUserDTO= TestUserDTO.UpdateUserTestDTO.makeUpdateUserDtoRequest();
    }

    @DisplayName("User Detail")
    @Test
    public void userDetail() throws Exception {
        User saveUser=userService.createUser(user);
        String token=jwtSerializer.jwtFromUser(saveUser);
        this.mockMvc.perform(get("/user/detail")
                .header("Authorization","Bearer "+token)
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("?????????"),
                                fieldWithPath("result.id").type(JsonFieldType.NUMBER).description("?????? id"),
                                fieldWithPath("result.email").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("result.nickname").type(JsonFieldType.STRING).description("?????? nickname"),
                                fieldWithPath("responseTime").type(JsonFieldType.STRING).description("?????? ??????")
                        )
                        )
                ).andReturn();
    }


    @DisplayName("?????? ??????")
    @Test
    public void inactiveUser() throws Exception {
        User saveUser=userService.createUser(user);
        String token=jwtSerializer.jwtFromUser(saveUser);

        this.mockMvc.perform(put("/user/inactive")
                .header("Authorization","Bearer "+token)
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("????????? ?????? ( ?????? )")),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("Http ?????? ??????"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("?????? ?????? ?????????"),
                                fieldWithPath("responseTime").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("result").type(JsonFieldType.STRING).description("??????").optional()
                        )
                        )
                ).andReturn();

    }

/*
    @DisplayName("????????????")
    @Test
    public void logout() throws Exception {
        String token="eyJhbGciOiJIUzI1NiIsInR5cGUiOiJKV1QifQ.eyJzdWIiOjE1MjgsIm5hbWUiOiJvanc5NzA3MjVAbmF2ZXIuY29tIiwiaWF0IjoxNjQ0OTMwNzIwfQ.IVrqbiCi_2gI0EXaCHbDDcwqn-LiMtlaw4kWLUcab5k";
        this.mockMvc.perform(get("/kakao/logout")
                .header("Authorization","Bearer "+token)
                .with(authentication(authentication))
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }
 */


}