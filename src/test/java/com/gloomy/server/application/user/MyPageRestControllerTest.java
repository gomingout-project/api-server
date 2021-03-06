package com.gloomy.server.application.user;

import com.gloomy.server.application.AbstractControllerTest;
import com.gloomy.server.application.comment.CommentDTO;
import com.gloomy.server.application.comment.CommentService;
import com.gloomy.server.application.feed.FeedDTO;
import com.gloomy.server.application.feed.FeedService;
import com.gloomy.server.application.feed.TestFeedDTO;
import com.gloomy.server.domain.feed.Feed;
import com.gloomy.server.domain.jwt.JWTSerializer;
import com.gloomy.server.domain.user.User;
import com.gloomy.server.domain.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "spring.config.location=classpath:test-application.yml,classpath:aws.yml"
})
@Transactional
public class MyPageRestControllerTest extends AbstractControllerTest {

    @Autowired
    UserService userService;
    @Autowired
    FeedService feedService;
    @Autowired
    CommentService commentService;
    @Autowired
    JWTSerializer jwtSerializer;
    User user;
    private User testUser;
    TestFeedDTO testFeedDTO1;
    TestFeedDTO testFeedDTO2;
    Authentication authentication;

    @BeforeEach
    public void setUp() {
        authentication= SecurityContextHolder.getContext().getAuthentication();
        this.user = TestUserDTO.TestUser.makeTestUser();
        testUser = userService.createUser(user);
        testFeedDTO1 = new TestFeedDTO(testUser, 1);
        testFeedDTO2 = new TestFeedDTO(testUser, 2);
    }

    @DisplayName("?????????_??????_??????")
    @Test
    void getUserFeeds() throws Exception {
        User saveUser=userService.createUser(user);
        feedService.createFeed(testFeedDTO1.getUserId(), testFeedDTO1.makeUserFeedDTO());
        feedService.createFeed(testFeedDTO2.getUserId(), testFeedDTO2.makeUserFeedDTO());

        String token=jwtSerializer.jwtFromUser(saveUser);
        mockMvc.perform(get("/myPage/feed")
                .header("Authorization", "Bearer " + token)
                .param("page", "0")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("????????? ??????")),
                        requestParameters(
                                parameterWithName("page").description("????????? ??????")),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("?????? ?????? ??????"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("result").description("?????? ?????????"),
                                fieldWithPath("result.content[]").type(JsonFieldType.ARRAY).description("?????? ????????? ?????????"),
                                fieldWithPath("result.content[].id").type(JsonFieldType.NUMBER).description("?????? ID"),
                                fieldWithPath("result.content[].ip").type(JsonFieldType.STRING).description("????????? IP"),
                                fieldWithPath("result.content[].userId").type(JsonFieldType.NUMBER).description("(????????? ??????) ?????? ID"),
                                fieldWithPath("result.content[].nickname").type(JsonFieldType.NULL).description("(???????????? ??????) ?????????"),
                                fieldWithPath("result.content[].password").type(JsonFieldType.NULL).description("(???????????? ??????) ????????????"),
                                fieldWithPath("result.content[].category").type(JsonFieldType.STRING).description("?????? ????????????"),
                                fieldWithPath("result.content[].title").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("result.content[].content").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("result.content[].likeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                fieldWithPath("result.content[].imageURLs").type(JsonFieldType.ARRAY).description("?????? ????????? ?????????"),
                                fieldWithPath("result.content[].commentCount").type(JsonFieldType.NUMBER).description("????????? ?????? ???"),
                                fieldWithPath("result.content[].status").type(JsonFieldType.STRING).description("?????? ?????? (ACTIVE, INACTIVE)"),
                                fieldWithPath("result.content[].createdAt").type(JsonFieldType.STRING).description("?????? ????????????"),
                                fieldWithPath("result.content[].updatedAt").type(JsonFieldType.STRING).description("?????? ????????????"),
                                fieldWithPath("result.content[].deletedAt").type(JsonFieldType.STRING).description("?????? ????????????"),

                                fieldWithPath("result.pageable").type(JsonFieldType.STRING).description("pageable ??????"),
                                fieldWithPath("result.totalPages").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                fieldWithPath("result.totalElements").type(JsonFieldType.NUMBER).description("?????? ????????? ??? ????????? ???"),
                                fieldWithPath("result.last").type(JsonFieldType.BOOLEAN).description("????????? ????????? ??????"),
                                fieldWithPath("result.numberOfElements").type(JsonFieldType.NUMBER).description("?????? ????????? ??? ????????? ???"),
                                fieldWithPath("result.first").type(JsonFieldType.BOOLEAN).description("??? ????????? ??????"),
                                fieldWithPath("result.size").type(JsonFieldType.NUMBER).description("????????? ??? ?????? ??????"),
                                fieldWithPath("result.sort.sorted").type(JsonFieldType.BOOLEAN).description("?????? ??????"),
                                fieldWithPath("result.sort.unsorted").type(JsonFieldType.BOOLEAN).description("????????? ??????"),
                                fieldWithPath("result.sort.empty").type(JsonFieldType.BOOLEAN).description("?????? ??????????????? ??????"),
                                fieldWithPath("result.number").type(JsonFieldType.NUMBER).description("?????? ????????? ?????????"),
                                fieldWithPath("result.empty").type(JsonFieldType.BOOLEAN).description("??????????????? ??????"),
                                fieldWithPath("responseTime").type(JsonFieldType.STRING).description("?????? ??????")
                        )
                ));
    }

    @DisplayName("find user Comment")
    @Test
    @WithMockUser
    public void userComment() throws Exception {
        User saveUser=userService.createUser(user);

        FeedDTO.Request feedDto1=new FeedDTO.Request("ALL", "test Title 1", "test content 1");

        Feed feed1=feedService.createFeed(saveUser.getId(), feedDto1);

        CommentDTO.Request comment1 = new CommentDTO.Request("test comment 1",feed1.getId());
        CommentDTO.Request comment2 = new CommentDTO.Request("test comment 2",feed1.getId());

        commentService.createComment(saveUser.getId(), comment1);
        commentService.createComment(saveUser.getId(), comment2);

        String token=jwtSerializer.jwtFromUser(saveUser);
        System.out.println(token);


        this.mockMvc.perform(get("/myPage/comment")
                .header("Authorization","Bearer "+token)
                .param("page", "0")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("????????? ?????? ( ?????? )")),
                        requestParameters(
                                parameterWithName("page").description("????????? ?????? (??????)")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("?????? ?????? ??????"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("result.content[].id").type(JsonFieldType.NUMBER).description("?????? ID"),
                                fieldWithPath("result.content[].content").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("result.content[].feedId").type(JsonFieldType.NUMBER).description("?????? ID"),
                                fieldWithPath("result.content[].userId").description("?????? ID"),
                                fieldWithPath("result.content[].nickname").description("(???????????? ??????) ?????????"),
                                fieldWithPath("result.content[].password").description("(???????????? ??????) ????????????"),
                                fieldWithPath("result.content[].status").description("?????? ??????"),
                                fieldWithPath("result.content[].createdAt").description("?????? ??????"),
                                fieldWithPath("result.content[].updatedAt").description("?????? ??????"),
                                fieldWithPath("result.content[].deletedAt").description("?????? ??????"),

                                fieldWithPath("result.pageable").type(JsonFieldType.STRING).description("pageable ??????"),
                                fieldWithPath("result.totalPages").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                fieldWithPath("result.totalElements").type(JsonFieldType.NUMBER).description("?????? ????????? ??? ????????? ???"),
                                fieldWithPath("result.last").type(JsonFieldType.BOOLEAN).description("????????? ????????? ??????"),
                                fieldWithPath("result.numberOfElements").type(JsonFieldType.NUMBER).description("?????? ????????? ??? ????????? ???"),
                                fieldWithPath("result.first").type(JsonFieldType.BOOLEAN).description("??? ????????? ??????"),
                                fieldWithPath("result.size").type(JsonFieldType.NUMBER).description("????????? ??? ?????? ??????"),
                                fieldWithPath("result.sort.sorted").type(JsonFieldType.BOOLEAN).description("?????? ??????"),
                                fieldWithPath("result.sort.unsorted").type(JsonFieldType.BOOLEAN).description("????????? ??????"),
                                fieldWithPath("result.sort.empty").type(JsonFieldType.BOOLEAN).description("?????? ??????????????? ??????"),
                                fieldWithPath("result.number").type(JsonFieldType.NUMBER).description("?????? ????????? ?????????"),
                                fieldWithPath("result.empty").type(JsonFieldType.BOOLEAN).description("??????????????? ??????"),
                                fieldWithPath("responseTime").type(JsonFieldType.STRING).description("?????? ??????")
                        )
                ));
    }

}