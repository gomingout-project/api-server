package com.gloomy.server.application.reply;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gloomy.server.application.AbstractControllerTest;
import com.gloomy.server.application.comment.CommentService;
import com.gloomy.server.application.comment.TestCommentDTO;
import com.gloomy.server.application.feed.FeedService;
import com.gloomy.server.application.feed.TestFeedDTO;
import com.gloomy.server.application.feed.TestUserDTO;
import com.gloomy.server.application.image.ImageService;
import com.gloomy.server.application.notice.NoticeService;
import com.gloomy.server.domain.comment.Comment;
import com.gloomy.server.domain.feed.Feed;
import com.gloomy.server.domain.jwt.JWTSerializer;
import com.gloomy.server.domain.notice.Notice;
import com.gloomy.server.domain.reply.Reply;
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
public class ReplyRestControllerTest extends AbstractControllerTest {
    @Autowired
    private UserService userService;
    @Autowired
    private FeedService feedService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private ReplyService replyService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private JWTSerializer jwtSerializer;
    @Autowired
    ObjectMapper objectMapper;

    @Value("${cloud.aws.s3.feedTestDir}")
    private String feedTestDir;
    TestReplyDTO testReplyDTO;

    @BeforeEach
    void beforeEach() {
        User testUser = userService.createUser(TestUserDTO.makeTestUser());
        TestFeedDTO testFeedDTO = new TestFeedDTO(testUser, 1);
        Feed testFeed = feedService.createFeed(null, testFeedDTO.makeNonUserFeedDTO());
        TestCommentDTO testCommentDTO = new TestCommentDTO(testFeed.getId(), testUser.getId());
        Comment testComment = commentService.createComment(null, testCommentDTO.makeNonUserCommentDTO());
        testReplyDTO = new TestReplyDTO(testCommentDTO.getUserId(), testComment.getId());
        testReplyDTO.setToken(jwtSerializer.jwtFromUser(testUser));
    }

    @AfterEach
    void afterEach() {
        imageService.deleteAll(feedTestDir);
    }

    @DisplayName("?????????_??????_?????????")
    @Transactional
    @Test
    void createNonUserReply() throws Exception {
        ReplyDTO.Request request = testReplyDTO.makeNonUserReplyDTO();

        String body = objectMapper.writeValueAsString(request);

        this.mockMvc.perform(post("/reply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        requestFields(
                                fieldWithPath("content").description("????????? ??????"),
                                fieldWithPath("commentId").description("???????????? ?????? ID"),
                                fieldWithPath("password").description("????????????")),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("?????? ?????? ??????"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("result").type(JsonFieldType.OBJECT).description("?????? ?????????"),
                                fieldWithPath("result.id").type(JsonFieldType.NUMBER).description("????????? ID"),
                                fieldWithPath("result.content").type(JsonFieldType.STRING).description("????????? ??????"),
                                fieldWithPath("result.commentId").type(JsonFieldType.NUMBER).description("???????????? ?????? ID"),
                                fieldWithPath("result.userId").type(JsonFieldType.NULL).description("(????????? ??????) ?????? ID"),
                                fieldWithPath("result.nickname").type(JsonFieldType.STRING).description("(???????????? ??????) ?????????"),
                                fieldWithPath("result.password").type(JsonFieldType.STRING).description("(???????????? ??????) ????????????"),
                                fieldWithPath("result.status").type(JsonFieldType.STRING).description("????????? ?????? (ACTIVE, INACTIVE)"),
                                fieldWithPath("result.createdAt").type(JsonFieldType.STRING).description("????????? ????????????"),
                                fieldWithPath("result.updatedAt").type(JsonFieldType.STRING).description("????????? ????????????"),
                                fieldWithPath("result.deletedAt").type(JsonFieldType.STRING).description("????????? ????????????"),
                                fieldWithPath("responseTime").type(JsonFieldType.STRING).description("?????? ??????")
                        )
                ));
    }

    @DisplayName("?????????_??????_??????")
    @Transactional
    @Test
    void createUserReply() throws Exception {
        ReplyDTO.Request request = testReplyDTO.makeUserReplyDTO();

        String body = objectMapper.writeValueAsString(request);

        this.mockMvc.perform(post("/reply")
                        .header("Authorization", "Bearer " + testReplyDTO.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("????????? ??????")),
                        requestFields(
                                fieldWithPath("content").description("????????? ??????"),
                                fieldWithPath("commentId").description("?????? ID"),
                                fieldWithPath("password").description("???????????? (NULL)")),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("?????? ?????? ??????"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("result").type(JsonFieldType.OBJECT).description("?????? ?????????"),
                                fieldWithPath("result.id").type(JsonFieldType.NUMBER).description("????????? ID"),
                                fieldWithPath("result.content").type(JsonFieldType.STRING).description("????????? ??????"),
                                fieldWithPath("result.commentId").type(JsonFieldType.NUMBER).description("???????????? ?????? ID"),
                                fieldWithPath("result.userId").type(JsonFieldType.NUMBER).description("(????????? ??????) ?????? ID"),
                                fieldWithPath("result.nickname").type(JsonFieldType.NULL).description("(???????????? ??????) ?????????"),
                                fieldWithPath("result.password").type(JsonFieldType.NULL).description("(???????????? ??????) ????????????"),
                                fieldWithPath("result.status").type(JsonFieldType.STRING).description("????????? ?????? (ACTIVE, INACTIVE)"),
                                fieldWithPath("result.createdAt").type(JsonFieldType.STRING).description("????????? ????????????"),
                                fieldWithPath("result.updatedAt").type(JsonFieldType.STRING).description("????????? ????????????"),
                                fieldWithPath("result.deletedAt").type(JsonFieldType.STRING).description("????????? ????????????"),
                                fieldWithPath("responseTime").type(JsonFieldType.STRING).description("?????? ??????")
                        )
                ));
    }

    @DisplayName("??????_?????????_??????_??????")
    @Transactional
    @Test
    void getCommentAllReplies() throws Exception {
        ReplyDTO.Request request = testReplyDTO.makeNonUserReplyDTO();

        replyService.createReply(null, request);
        replyService.createReply(null, request);

        this.mockMvc.perform(get("/reply/comment/{commentId}", request.getCommentId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("commentId").description("????????? ???????????? ?????? ID")),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("?????? ?????? ??????"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("result").type(JsonFieldType.OBJECT).description("?????? ?????????"),
                                fieldWithPath("result.content[]").type(JsonFieldType.ARRAY).description("?????? ????????? ?????????"),
                                fieldWithPath("result.content[].id").type(JsonFieldType.NUMBER).description("????????? ID"),
                                fieldWithPath("result.content[].content").type(JsonFieldType.STRING).description("????????? ??????"),
                                fieldWithPath("result.content[].commentId").type(JsonFieldType.NUMBER).description("???????????? ?????? ID"),
                                fieldWithPath("result.content[].userId").description("(????????? ??????) ?????? ID"),
                                fieldWithPath("result.content[].nickname").type(JsonFieldType.STRING).description("(???????????? ??????) ?????????"),
                                fieldWithPath("result.content[].password").description("(???????????? ??????) ????????????"),
                                fieldWithPath("result.content[].status").type(JsonFieldType.STRING).description("????????? ?????? (ACTIVE, INACTIVE)"),
                                fieldWithPath("result.content[].createdAt").type(JsonFieldType.STRING).description("????????? ????????????"),
                                fieldWithPath("result.content[].updatedAt").type(JsonFieldType.STRING).description("????????? ????????????"),
                                fieldWithPath("result.content[].deletedAt").type(JsonFieldType.STRING).description("????????? ????????????"),

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

    @DisplayName("??????_??????_??????")
    @Transactional
    @Test
    void getReply() throws Exception {
        ReplyDTO.Request request = testReplyDTO.makeNonUserReplyDTO();

        Reply createdReply = replyService.createReply(null, request);

        this.mockMvc.perform(get("/reply/{replyId}", createdReply.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("replyId").description("????????? ????????? ID")),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("?????? ?????? ??????"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("result").type(JsonFieldType.OBJECT).description("?????? ?????????"),
                                fieldWithPath("result.id").type(JsonFieldType.NUMBER).description("????????? ID"),
                                fieldWithPath("result.content").type(JsonFieldType.STRING).description("????????? ??????"),
                                fieldWithPath("result.commentId").type(JsonFieldType.NUMBER).description("???????????? ?????? ID"),
                                fieldWithPath("result.userId").description("(????????? ??????) ?????? ID"),
                                fieldWithPath("result.nickname").description("(???????????? ??????) ?????????"),
                                fieldWithPath("result.password").description("(???????????? ??????) ????????????"),
                                fieldWithPath("result.status").type(JsonFieldType.STRING).description("????????? ?????? (ACTIVE, INACTIVE)"),
                                fieldWithPath("result.createdAt").type(JsonFieldType.STRING).description("????????? ????????????"),
                                fieldWithPath("result.updatedAt").type(JsonFieldType.STRING).description("????????? ????????????"),
                                fieldWithPath("result.deletedAt").type(JsonFieldType.STRING).description("????????? ????????????"),
                                fieldWithPath("responseTime").type(JsonFieldType.STRING).description("?????? ??????")
                        )
                ));
    }

    @DisplayName("?????????_??????")
    @Transactional
    @Test
    void updateReply() throws Exception {
        String updateContent = "??? ???";
        ReplyDTO.Request request = testReplyDTO.makeNonUserReplyDTO();
        UpdateReplyDTO.Request updateRequest = new UpdateReplyDTO.Request();
        updateRequest.setContent(updateContent);

        Reply createdReply = replyService.createReply(null, request);
        String body = objectMapper.writeValueAsString(updateRequest);

        this.mockMvc.perform(patch("/reply/{replyId}", createdReply.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("replyId").description("????????? ????????? ID")),
                        requestFields(
                                fieldWithPath("content").description("????????? ????????? ?????? (????????????)")),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("?????? ?????? ??????"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("result").type(JsonFieldType.OBJECT).description("?????? ?????????"),
                                fieldWithPath("result.id").type(JsonFieldType.NUMBER).description("????????? ID"),
                                fieldWithPath("result.content").type(JsonFieldType.STRING).description("????????? ??????"),
                                fieldWithPath("result.commentId").type(JsonFieldType.NUMBER).description("???????????? ?????? ID"),
                                fieldWithPath("result.userId").description("(????????? ??????) ?????? ID"),
                                fieldWithPath("result.nickname").description("(???????????? ??????) ?????????"),
                                fieldWithPath("result.password").description("(???????????? ??????) ????????????"),
                                fieldWithPath("result.status").type(JsonFieldType.STRING).description("????????? ?????? (ACTIVE, INACTIVE)"),
                                fieldWithPath("result.createdAt").type(JsonFieldType.STRING).description("????????? ????????????"),
                                fieldWithPath("result.updatedAt").type(JsonFieldType.STRING).description("????????? ????????????"),
                                fieldWithPath("result.deletedAt").type(JsonFieldType.STRING).description("????????? ????????????"),
                                fieldWithPath("responseTime").type(JsonFieldType.STRING).description("?????? ??????")
                        )
                ));
    }

    @DisplayName("????????? ??????")
    @Transactional
    @Test
    void deleteReply() throws Exception {
        ReplyDTO.Request request = testReplyDTO.makeNonUserReplyDTO();

        Reply createdReply = replyService.createReply(null, request);

        this.mockMvc.perform(delete("/reply/{replyId}", createdReply.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("replyId").description("????????? ????????? ID")),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("?????? ?????? ??????"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("?????? ?????????"),
                                fieldWithPath("result").type(JsonFieldType.NULL).description("??????"),
                                fieldWithPath("responseTime").type(JsonFieldType.STRING).description("?????? ??????")
                        )
                ));
    }
}
