package com.gloomy.server.application.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gloomy.server.application.AbstractControllerTest;
import com.gloomy.server.application.feed.FeedService;
import com.gloomy.server.application.feed.TestFeedDTO;
import com.gloomy.server.application.feed.TestUserDTO;
import com.gloomy.server.application.image.ImageService;
import com.gloomy.server.application.notice.NoticeService;
import com.gloomy.server.domain.comment.Comment;
import com.gloomy.server.domain.feed.Feed;
import com.gloomy.server.domain.jwt.JWTSerializer;
import com.gloomy.server.domain.user.User;
import com.gloomy.server.domain.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "spring.config.location=classpath:test-application.yml,classpath:aws.yml"
})
class CommentRestControllerTest extends AbstractControllerTest {
    @Autowired
    UserService userService;
    @Autowired
    FeedService feedService;
    @Autowired
    ImageService imageService;
    @Autowired
    CommentService commentService;
    @Autowired
    NoticeService noticeService;
    @Autowired
    private JWTSerializer jwtSerializer;
    @Autowired
    ObjectMapper objectMapper;

    @Value("${cloud.aws.s3.feedTestDir}")
    private String feedTestDir;
    private TestCommentDTO testCommentDTO;

    @BeforeEach
    void beforeEach() {
        User testUser = userService.createUser(TestUserDTO.makeTestUser());
        TestFeedDTO testFeedDTO = new TestFeedDTO(testUser, 1);
        Feed testFeed = feedService.createFeed(null, testFeedDTO.makeNonUserFeedDTO());
        testCommentDTO = new TestCommentDTO(testFeed.getId(), testUser.getId());
        testCommentDTO.setToken(jwtSerializer.jwtFromUser(testUser));
    }

    @AfterEach
    void afterEach() {
        imageService.deleteAll(feedTestDir);
    }

    @DisplayName("?????????_??????_??????")
    @Transactional
    @Test
    void createNonUserComment() throws Exception {
        CommentDTO.Request request = testCommentDTO.makeNonUserCommentDTO();

        String body = objectMapper.writeValueAsString(request);

        this.mockMvc.perform(post("/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        requestFields(
                                fieldWithPath("content").description("?????? ??????"),
                                fieldWithPath("feedId").description("????????? ?????? ID"),
                                fieldWithPath("password").description("????????????")),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("?????? ?????? ??????"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("result").type(JsonFieldType.OBJECT).description("?????? ?????????"),
                                fieldWithPath("result.id").type(JsonFieldType.NUMBER).description("?????? ID"),
                                fieldWithPath("result.content").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("result.feedId").type(JsonFieldType.NUMBER).description("????????? ?????? ID"),
                                fieldWithPath("result.userId").type(JsonFieldType.NULL).description("(????????? ??????) ?????? ID"),
                                fieldWithPath("result.nickname").type(JsonFieldType.STRING).description("(???????????? ??????) ?????????"),
                                fieldWithPath("result.password").type(JsonFieldType.STRING).description("(???????????? ??????) ????????????"),
                                fieldWithPath("result.status").type(JsonFieldType.STRING).description("?????? ?????? (ACTIVE, INACTIVE)"),
                                fieldWithPath("result.createdAt").type(JsonFieldType.STRING).description("?????? ????????????"),
                                fieldWithPath("result.updatedAt").type(JsonFieldType.STRING).description("?????? ????????????"),
                                fieldWithPath("result.deletedAt").type(JsonFieldType.STRING).description("?????? ????????????"),
                                fieldWithPath("responseTime").type(JsonFieldType.STRING).description("?????? ??????")
                        )
                ));
    }

    @DisplayName("??????_??????_??????")
    @Transactional
    @Test
    void createUserComment() throws Exception {
        CommentDTO.Request request = testCommentDTO.makeUserCommentDTO();

        String body = objectMapper.writeValueAsString(request);

        this.mockMvc.perform(post("/comment")
                        .header("Authorization", "Bearer " + testCommentDTO.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("????????? ??????")),
                        requestFields(
                                fieldWithPath("content").description("?????? ??????"),
                                fieldWithPath("feedId").description("????????? ?????? ID"),
                                fieldWithPath("password").description("???????????? (NULL)")),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("?????? ?????? ??????"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("result").type(JsonFieldType.OBJECT).description("?????? ?????????"),
                                fieldWithPath("result.id").type(JsonFieldType.NUMBER).description("?????? ID"),
                                fieldWithPath("result.content").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("result.feedId").type(JsonFieldType.NUMBER).description("????????? ?????? ID"),
                                fieldWithPath("result.userId").type(JsonFieldType.NUMBER).description("(????????? ??????) ?????? ID"),
                                fieldWithPath("result.nickname").type(JsonFieldType.NULL).description("(???????????? ??????) ?????????"),
                                fieldWithPath("result.password").type(JsonFieldType.NULL).description("(???????????? ??????) ????????????"),
                                fieldWithPath("result.status").type(JsonFieldType.STRING).description("?????? ?????? (ACTIVE, INACTIVE)"),
                                fieldWithPath("result.createdAt").type(JsonFieldType.STRING).description("?????? ????????????"),
                                fieldWithPath("result.updatedAt").type(JsonFieldType.STRING).description("?????? ????????????"),
                                fieldWithPath("result.deletedAt").type(JsonFieldType.STRING).description("?????? ????????????"),
                                fieldWithPath("responseTime").type(JsonFieldType.STRING).description("?????? ??????")
                        )
                ));
    }

    @DisplayName("??????_??????_??????_??????")
    @Transactional
    @Test
    void getFeedAllComments() throws Exception {
        CommentDTO.Request request = testCommentDTO.makeNonUserCommentDTO();

        commentService.createComment(null, request);
        commentService.createComment(null, request);

        this.mockMvc.perform(get("/comment/feed/{feedId}", request.getFeedId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("feedId").description("????????? ????????? ?????? ID")),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("?????? ?????? ??????"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("result").type(JsonFieldType.OBJECT).description("?????? ?????????"),
                                fieldWithPath("result.content[]").type(JsonFieldType.ARRAY).description("?????? ????????? ?????????"),
                                fieldWithPath("result.content[].id").type(JsonFieldType.NUMBER).description("?????? ID"),
                                fieldWithPath("result.content[].content").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("result.content[].feedId").type(JsonFieldType.NUMBER).description("????????? ?????? ID"),
                                fieldWithPath("result.content[].userId").description("(????????? ??????) ?????? ID"),
                                fieldWithPath("result.content[].nickname").type(JsonFieldType.STRING).description("(???????????? ??????) ?????????"),
                                fieldWithPath("result.content[].password").description("(???????????? ??????) ????????????"),
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

    @DisplayName("??????_??????")
    @Transactional
    @Test
    void getComment() throws Exception {
        CommentDTO.Request request = testCommentDTO.makeNonUserCommentDTO();

        Comment createdComment = commentService.createComment(null, request);

        this.mockMvc.perform(get("/comment/{commentId}", createdComment.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("commentId").description("????????? ?????? ID")),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("?????? ?????? ??????"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("result").type(JsonFieldType.OBJECT).description("?????? ?????????"),
                                fieldWithPath("result.id").type(JsonFieldType.NUMBER).description("?????? ID"),
                                fieldWithPath("result.content").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("result.feedId").type(JsonFieldType.NUMBER).description("????????? ?????? ID"),
                                fieldWithPath("result.userId").description("(????????? ??????) ?????? ID"),
                                fieldWithPath("result.nickname").description("(???????????? ??????) ?????????"),
                                fieldWithPath("result.password").description("(???????????? ??????) ????????????"),
                                fieldWithPath("result.status").type(JsonFieldType.STRING).description("?????? ?????? (ACTIVE, INACTIVE)"),
                                fieldWithPath("result.createdAt").type(JsonFieldType.STRING).description("?????? ????????????"),
                                fieldWithPath("result.updatedAt").type(JsonFieldType.STRING).description("?????? ????????????"),
                                fieldWithPath("result.deletedAt").type(JsonFieldType.STRING).description("?????? ????????????"),
                                fieldWithPath("responseTime").type(JsonFieldType.STRING).description("?????? ??????")
                        )
                ));
    }

    @DisplayName("??????_??????")
    @Transactional
    @Test
    void updateComment() throws Exception {
        String updateContent = "??? ???";
        CommentDTO.Request request = testCommentDTO.makeNonUserCommentDTO();
        UpdateCommentDTO.Request updateRequest = new UpdateCommentDTO.Request();
        updateRequest.setContent(updateContent);

        Comment createdComment = commentService.createComment(null, request);
        String body = objectMapper.writeValueAsString(updateRequest);

        this.mockMvc.perform(patch("/comment/{commentId}", createdComment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("commentId").description("????????? ?????? ID")),
                        requestFields(
                                fieldWithPath("content").description("????????? ?????? ?????? (????????????)")),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("?????? ?????? ??????"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("result").type(JsonFieldType.OBJECT).description("?????? ?????????"),
                                fieldWithPath("result.id").type(JsonFieldType.NUMBER).description("?????? ID"),
                                fieldWithPath("result.content").type(JsonFieldType.STRING).description("?????? ??????"),
                                fieldWithPath("result.feedId").type(JsonFieldType.NUMBER).description("????????? ?????? ID"),
                                fieldWithPath("result.userId").description("(????????? ??????) ?????? ID"),
                                fieldWithPath("result.nickname").description("(???????????? ??????) ?????????"),
                                fieldWithPath("result.password").description("(???????????? ??????) ????????????"),
                                fieldWithPath("result.status").type(JsonFieldType.STRING).description("?????? ?????? (ACTIVE, INACTIVE)"),
                                fieldWithPath("result.createdAt").type(JsonFieldType.STRING).description("?????? ????????????"),
                                fieldWithPath("result.updatedAt").type(JsonFieldType.STRING).description("?????? ????????????"),
                                fieldWithPath("result.deletedAt").type(JsonFieldType.STRING).description("?????? ????????????"),
                                fieldWithPath("responseTime").type(JsonFieldType.STRING).description("?????? ??????")
                        )
                ));
    }

    @DisplayName("?????? ??????")
    @Transactional
    @Test
    void deleteComment() throws Exception {
        CommentDTO.Request request = testCommentDTO.makeNonUserCommentDTO();

        Comment createdComment = commentService.createComment(null, request);

        this.mockMvc.perform(delete("/comment/{commentId}", createdComment.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("commentId").description("????????? ?????? ID")),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("?????? ?????? ??????"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("result").type(JsonFieldType.NULL).description("??????"),
                                fieldWithPath("responseTime").type(JsonFieldType.STRING).description("?????? ??????")
                        )
                ));
    }
}
