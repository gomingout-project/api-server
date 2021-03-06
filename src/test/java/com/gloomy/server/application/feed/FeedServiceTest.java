package com.gloomy.server.application.feed;

import com.gloomy.server.application.feedlike.FeedLikeDTO;
import com.gloomy.server.application.feedlike.FeedLikeService;
import com.gloomy.server.application.image.ImageService;
import com.gloomy.server.application.image.Images;
import com.gloomy.server.application.image.TestImage;
import com.gloomy.server.application.report.ReportDTO;
import com.gloomy.server.domain.common.entity.Status;
import com.gloomy.server.domain.feed.Category;
import com.gloomy.server.domain.feed.Feed;
import com.gloomy.server.domain.feedlike.FeedLike;
import com.gloomy.server.domain.report.ReportCategory;
import com.gloomy.server.domain.report.ReportService;
import com.gloomy.server.domain.user.User;
import com.gloomy.server.domain.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "spring.config.location=classpath:test-application.yml,classpath:aws.yml"
})
class FeedServiceTest {
    @Autowired
    private FeedService feedService;
    @Autowired
    private FeedLikeService feedLikeService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private UserService userService;

    @Value("${cloud.aws.s3.feedTestDir}")
    private String feedTestDir;
    private TestFeedDTO testFeedDTO;
    private UpdateFeedDTO.Request updateFeedDTO;

    @BeforeEach
    void beforeEach() {
        User testUser = userService.createUser(TestUserDTO.makeTestUser());
        testFeedDTO = new TestFeedDTO(testUser, 1);
        updateFeedDTO = new UpdateFeedDTO.Request();
    }

    @AfterEach
    void afterEach() {
        reportService.deleteAll();
        imageService.deleteAll(feedTestDir);
        feedService.deleteAll();
        userService.deleteAll();
    }

    @Test
    void ??????_??????_??????_??????() {
        FeedDTO.Request userFeedDTO = testFeedDTO.makeUserFeedDTO();

        Feed createdFeed = feedService.createFeed(testFeedDTO.getUserId(), userFeedDTO);

        checkCreatedFeedSuccess(testFeedDTO.getUserId(), userFeedDTO, createdFeed);
    }

    @Test
    void ??????_??????_??????_??????() {
        userService.deleteAll();

        FeedDTO.Request userFeedDTO = testFeedDTO.makeUserFeedDTO();

        checkCreatedFeedFail(testFeedDTO.getUserId(), userFeedDTO, null);
    }

    @Test
    void ??????_??????_?????????_??????() {
        FeedDTO.Request nonUserFeedDTO = testFeedDTO.makeNonUserFeedDTO();

        Feed createdFeed = feedService.createFeed(null, nonUserFeedDTO);

        checkCreatedFeedSuccess(null, nonUserFeedDTO, createdFeed);
    }

    @Test
    void ??????_??????_?????????_??????() {
        FeedDTO.Request nonUserFeedDTOWithZeroOrLessPassword = new FeedDTO.Request(
                "", testFeedDTO.getCategory(), testFeedDTO.getTitle(), testFeedDTO.getContent());

        checkCreatedFeedFail(null, nonUserFeedDTOWithZeroOrLessPassword, "[FeedService] ????????? ?????? ?????? ?????? ???????????? ?????????????????????.");
    }

    @Test
    void ??????_??????_??????_??????() {
        FeedDTO.Request feedDTOWithNoUserIdAndNoPassword = new FeedDTO.Request(
                null, testFeedDTO.getCategory(), testFeedDTO.getTitle(), testFeedDTO.getContent());
        FeedDTO.Request nonUserFeedDTOWithNoCategory = new FeedDTO.Request(
                testFeedDTO.getPassword(), null, testFeedDTO.getTitle(), testFeedDTO.getContent());
        FeedDTO.Request nonUserFeedDTOWithInvalidCategory = new FeedDTO.Request(
                testFeedDTO.getPassword(), "INVALID_CATEGORY", testFeedDTO.getTitle(), testFeedDTO.getContent());
        FeedDTO.Request nonUserFeedDTOWithNoTitle = new FeedDTO.Request(
                testFeedDTO.getPassword(), testFeedDTO.getCategory(), null, testFeedDTO.getContent());
        FeedDTO.Request nonUserFeedDTOWithZeroOrLessTitle = new FeedDTO.Request(
                testFeedDTO.getPassword(), testFeedDTO.getCategory(), "", testFeedDTO.getContent());
        FeedDTO.Request nonUserFeedDTOWithNoContent = new FeedDTO.Request(
                testFeedDTO.getPassword(), testFeedDTO.getCategory(), testFeedDTO.getTitle(), null);
        FeedDTO.Request nonUserFeedDTOWithZeroOrLessContent = new FeedDTO.Request(
                testFeedDTO.getPassword(), testFeedDTO.getCategory(), testFeedDTO.getTitle(), "");

        checkCreatedFeedFail(null, feedDTOWithNoUserIdAndNoPassword, "[FeedService] ?????? ?????? ?????? ???????????? ?????????????????????.");
        checkCreatedFeedFail(null, nonUserFeedDTOWithNoCategory, "[FeedService] ?????? ?????? ?????? ???????????? ?????????????????????.");
        checkCreatedFeedFail(null, nonUserFeedDTOWithInvalidCategory, "[FeedService] ?????? ?????? ?????? ???????????? ?????????????????????.");
        checkCreatedFeedFail(null, nonUserFeedDTOWithNoTitle, "[FeedService] ?????? ?????? ?????? ???????????? ?????????????????????.");
        checkCreatedFeedFail(null, nonUserFeedDTOWithZeroOrLessTitle, "[FeedService] ?????? ?????? ?????? ???????????? ?????????????????????.");
        checkCreatedFeedFail(null, nonUserFeedDTOWithNoContent, "[FeedService] ?????? ?????? ?????? ???????????? ?????????????????????.");
        checkCreatedFeedFail(null, nonUserFeedDTOWithZeroOrLessContent, "[FeedService] ?????? ?????? ?????? ???????????? ?????????????????????.");
    }

    @Transactional
    @Test
    void ??????_??????_?????????_?????????_??????_??????_??????_??????() {
        ArrayList<MultipartFile> images = TestImage.makeImages(2);
        Feed userFeed = feedService.createFeed(testFeedDTO.getUser());

        Images createdImages = feedService.uploadImages(userFeed.getId(), testFeedDTO.getUserId(), images);

        checkUploadedImageSuccess(userFeed, createdImages, images);
    }

    @Transactional
    @Test
    void ??????_??????_?????????_?????????_??????_??????_??????_??????() {
        ArrayList<MultipartFile> images = TestImage.makeImages(2);

        Images createdImages = feedService.uploadImages(null, testFeedDTO.getUserId(), images);

        checkUploadedImageSuccess(createdImages.getImages().get(0).getFeedId(), createdImages, images);
    }

    @Transactional
    @Test
    void ??????_??????_?????????_?????????_??????() {
        User invalidUser = userService.createUser(TestUserDTO.makeTestUser());
        Feed userFeed = feedService.createFeed(testFeedDTO.getUser());

        checkUploadedUserFeedImageFail(userFeed.getId(), null, null, "[FeedService] ?????? ????????? ?????? ???????????? ?????????????????????.");
        checkUploadedUserFeedImageFail(userFeed.getId(), invalidUser.getId(), null, "[FeedService] ?????? ID??? ?????? ID??? ???????????? ????????????.");
    }

    @Transactional
    @Test
    void ?????????_??????_?????????_?????????_??????_??????_??????_??????() {
        ArrayList<MultipartFile> images = TestImage.makeImages(2);
        Feed nonUserFeed = feedService.createFeed(null);

        Images createdImages = feedService.uploadImages(nonUserFeed.getId(), null, images);

        checkUploadedImageSuccess(nonUserFeed, createdImages, images);
    }

    @Transactional
    @Test
    void ?????????_??????_?????????_?????????_??????_??????_??????_??????() {
        ArrayList<MultipartFile> images = TestImage.makeImages(2);

        Images createdImages = feedService.uploadImages(null, null, images);

        checkUploadedImageSuccess(createdImages.getImages().get(0).getFeedId(), createdImages, images);
    }

    @Test
    void ?????????_??????_?????????_?????????_??????() {
        Feed nonUserFeed = feedService.createFeed(null);

        checkUploadedUserFeedImageFail(nonUserFeed.getId(), testFeedDTO.getUserId(), null, "[FeedService] ????????? ????????? ?????? ???????????? ?????????????????????.");
    }

    @Transactional
    @Test
    void ??????_??????_??????_??????() {
        final int allNonUserFeedsNum = 3;
        final int allUserFeedsNum = 3;

        List<Feed> createdAllFeeds = addFeeds(allNonUserFeedsNum, allUserFeedsNum);
        Page<Feed> foundAllFeeds = feedService.findAllFeeds(PageRequest.of(0, 10));
        checkFoundAllFeedsSuccess(createdAllFeeds, foundAllFeeds, allNonUserFeedsNum, allUserFeedsNum);
    }

    @Test
    void ??????_??????_??????_??????() {
        checkFoundAllFeedsFail(null, "[FeedService] Pageable??? ???????????? ????????????.");
    }

    @Transactional
    @Test
    void ??????_??????_??????_??????() {
        FeedDTO.Request userFeedDTO = testFeedDTO.makeUserFeedDTO();

        Feed createdUserFeedFirst = feedService.createFeed(testFeedDTO.getUserId(), userFeedDTO);
        Page<Feed> foundUserFeedsFirst = feedService.findUserFeeds(
                PageRequest.of(0, 10), createdUserFeedFirst.getUserId().getId());
        Feed createdUserFeedSecond = feedService.createFeed(testFeedDTO.getUserId(), userFeedDTO);
        Page<Feed> foundUserFeedsSecond = feedService.findUserFeeds(
                PageRequest.of(0, 10), createdUserFeedFirst.getUserId().getId());

        assertEquals(foundUserFeedsFirst.getContent().size(), 1);
        assertEquals(foundUserFeedsFirst.getContent().get(0), createdUserFeedFirst);
        assertEquals(foundUserFeedsSecond.getContent().size(), 2);
        assertEquals(foundUserFeedsSecond.getContent().get(0), createdUserFeedFirst);
        assertEquals(foundUserFeedsSecond.getContent().get(1), createdUserFeedSecond);
    }

    @Test
    void ??????_??????_??????_??????() {
        User createdUser = userService.createUser(TestUserDTO.makeTestUser());
        User deletedUser = userService.createUser(TestUserDTO.makeTestUser());
        PageRequest pageable = PageRequest.of(0, 10);

        userService.deleteUser(deletedUser.getId());

        checkFoundUserFeedFail(pageable, null, "[FeedService] ?????? ID??? ???????????? ????????????.");
        checkFoundUserFeedFail(pageable, 0L, "[FeedService] ?????? ID??? ???????????? ????????????.");
        checkFoundUserFeedFail(pageable, deletedUser.getId(), "[FeedService] ???????????? ????????? ????????????.");
        checkFoundUserFeedFail(null, createdUser.getId(), "[FeedService] pageable??? ???????????? ????????????.");
    }

    @Transactional
    @Test
    void ??????_??????_?????????_??????() {
        FeedDTO.Request nonUserFeedDTO = testFeedDTO.makeNonUserFeedDTO();

        Feed createdUserFeed = feedService.createFeed(null, nonUserFeedDTO);
        Feed foundNonUserFeed = feedService.findOneFeed(createdUserFeed.getId());

        assertEquals(foundNonUserFeed, createdUserFeed);
    }

    @Test
    void ??????_??????_?????????_??????() {
        Feed createNonUserFeed = feedService.createFeed(null, testFeedDTO.makeNonUserFeedDTO());

        imageService.deleteAll(feedTestDir);
        feedService.deleteAll();

        checkFoundNonUserFeedFail(0L, "[FeedService] ????????? ?????? ID??? ???????????? ????????????.");
        checkFoundNonUserFeedFail(null, "[FeedService] ????????? ?????? ID??? ???????????? ????????????.");
        checkFoundNonUserFeedFail(createNonUserFeed.getId(), "[FeedService] ?????? ?????? ID??? ???????????? ????????????.");
    }

    @Transactional
    @Test
    void ??????_??????_??????_??????_????????????_??????() {
        FeedDTO.Request nonUserFeedDTO = testFeedDTO.makeNonUserFeedDTO();
        Feed activeFeed = feedService.createFeed(null, nonUserFeedDTO);
        Feed inactiveFeed = feedService.createFeed(null, nonUserFeedDTO);

        feedService.deleteFeed(inactiveFeed.getId());
        Page<Feed> foundActiveFeeds = feedService.findAllActiveFeeds(PageRequest.of(0, 10), null, null);

        assertEquals(foundActiveFeeds.getContent().size(), 1);
        assertEquals(foundActiveFeeds.getContent().get(0), activeFeed);
    }

    @Transactional
    @Test
    void ??????_??????_??????_??????_?????????_??????() {
        FeedDTO.Request nonUserFeedDTO = testFeedDTO.makeNonUserFeedDTO();
        Feed activeFeedFirst = feedService.createFeed(null, nonUserFeedDTO);
        Feed activeFeedSecond = feedService.createFeed(null, nonUserFeedDTO);
        PageRequest pageableWithSortNull = PageRequest.of(0, 10);
        PageRequest pageableWithSortDate = PageRequest.of(0, 10, Sort.by("date"));

        Page<Feed> foundActiveFeedsWithNull = feedService.findAllActiveFeeds(pageableWithSortNull, null, null);
        Page<Feed> foundActiveFeedsWithSortDate = feedService.findAllActiveFeeds(pageableWithSortDate, null, null);

        assertEquals(foundActiveFeedsWithNull.getContent().size(), 2);
        assertEquals(foundActiveFeedsWithNull.getContent().get(0), activeFeedSecond);
        assertEquals(foundActiveFeedsWithNull.getContent().get(1), activeFeedFirst);

        assertEquals(foundActiveFeedsWithSortDate.getContent().size(), 2);
        assertEquals(foundActiveFeedsWithSortDate.getContent().get(0), activeFeedSecond);
        assertEquals(foundActiveFeedsWithSortDate.getContent().get(1), activeFeedFirst);
    }

    @Transactional
    @Test
    void ??????_??????_??????_??????_??????_?????????_??????() {
        FeedDTO.Request nonUserFeedDTO = testFeedDTO.makeNonUserFeedDTO();
        Feed activeFeedFirst = feedService.createFeed(null, nonUserFeedDTO);
        Feed activeFeedSecond = feedService.createFeed(null, nonUserFeedDTO);
        PageRequest pageableWithSortLike = PageRequest.of(0, 10, Sort.by("like"));

        FeedLike feedLike = feedLikeService.createFeedLike(null, new FeedLikeDTO.Request(activeFeedFirst.getId()));
        Page<Feed> foundActiveFeeds = feedService.findAllActiveFeeds(pageableWithSortLike, null, null);

        assertEquals(foundActiveFeeds.getContent().size(), 2);
        assertEquals(foundActiveFeeds.getContent().get(0), activeFeedFirst);
        assertEquals(foundActiveFeeds.getContent().get(1), activeFeedSecond);
    }

    @Transactional
    @Test
    void ??????_??????_??????_??????_??????_??????_??????_??????() {
        PageRequest pageable = PageRequest.of(0, 10);
        Feed feedWithReport = feedService.createFeed(null, testFeedDTO.makeNonUserFeedDTO());
        ReportDTO.Request reportRequestDTO = new ReportDTO.Request(feedWithReport.getId(), ReportCategory.ABUSE.toString());

        Page<Feed> allActiveFeedsBeforeReport = feedService.findAllActiveFeeds(pageable, testFeedDTO.getUserId(), null);
        reportService.saveReport(reportRequestDTO, testFeedDTO.getUserId());
        Page<Feed> allActiveFeedsAfterReport = feedService.findAllActiveFeeds(pageable, testFeedDTO.getUserId(), null);

        assertEquals(allActiveFeedsBeforeReport.getContent().size(), 1);
        assertEquals(allActiveFeedsAfterReport.getContent().size(), 0);
    }

    @Transactional
    @Test
    void ??????_??????_??????_??????_??????_????????????_??????_??????_??????() {
        PageRequest pageable = PageRequest.of(0, 10);
        createFeed("FRIEND");
        createFeed("FAMILY");

        Page<Feed> foundAllFeeds = feedService.findAllActiveFeeds(pageable, null, null);

        assertEquals(foundAllFeeds.getContent().size(), 2);
        assertEquals(foundAllFeeds.getContent().get(0).getCategory(), Category.FAMILY);
        assertEquals(foundAllFeeds.getContent().get(1).getCategory(), Category.FRIEND);
    }

    @Transactional
    @Test
    void ??????_??????_??????_??????_??????_???????????????_??????_??????() {
        PageRequest pageable = PageRequest.of(0, 10);
        createFeed("FRIEND");
        createFeed("FAMILY");

        Page<Feed> foundAllFeeds = feedService.findAllActiveFeeds(pageable, null, "FAMILY");

        assertEquals(foundAllFeeds.getContent().size(), 1);
        assertEquals(foundAllFeeds.getContent().get(0).getCategory(), Category.FAMILY);
    }

    @Test
    void ??????_??????_??????_??????_??????_??????() {
        Pageable pageableWithSortInvalid = PageRequest.of(0, 10, Sort.by("invalid"));

        userService.deleteAll();

        checkFoundAllActiveFeedsFail(null, testFeedDTO.getUserId(), null, "[FeedService] ?????? ID??? ???????????? ????????????.");
        checkFoundAllActiveFeedsFail(null, null, null, "[FeedService] pageable??? ???????????? ????????????.");
        checkFoundAllActiveFeedsFail(pageableWithSortInvalid, null, null, "[FeedService] sort??? ???????????? ????????????.");
        checkFoundAllActiveFeedsFail(null, null, "INVALID", "[FeedService] category??? ???????????? ????????????.");
    }

    @Transactional
    @Test
    void ??????_??????_??????_??????() {
        Feed createdNonUserFeed = feedService.createFeed(null, testFeedDTO.makeNonUserFeedDTO());
        Images images = imageService.uploadImages(createdNonUserFeed, testFeedDTO.getImages());
        String updateContent = "??? ???";
        ArrayList<MultipartFile> updateImages = testFeedDTO.getImages();
        updateFeedDTO.setContent(updateContent);

        Feed updatedNonUserFeed = feedService.updateOneFeed(createdNonUserFeed.getId(), updateFeedDTO);
        Feed foundUpdatedNonUserFeed = feedService.findOneFeed(createdNonUserFeed.getId());
        Images foundActiveImages = imageService.findAllActiveImages(createdNonUserFeed);

        assertEquals(foundUpdatedNonUserFeed, updatedNonUserFeed);
        assertEquals(foundActiveImages.getSize(), images.getSize());
    }

    @Transactional
    @Test
    void ??????_??????_?????????_??????() {
        String updatePassword = "34567";
        Feed createdNonUserFeed = feedService.createFeed(null, testFeedDTO.makeNonUserFeedDTO());
        updateFeedDTO.setPassword(updatePassword);

        Feed updatedNonUserFeed = feedService.updateOneFeed(createdNonUserFeed.getId(), updateFeedDTO);
        Feed foundUpdatedNonUserFeed = feedService.findOneFeed(createdNonUserFeed.getId());

        assertEquals(foundUpdatedNonUserFeed, updatedNonUserFeed);
    }

    @Test
    void ??????_??????_??????_??????() {
        String updatePassword = "34567";
        Feed createdUserFeed = feedService.createFeed(testFeedDTO.getUserId(), testFeedDTO.makeUserFeedDTO());
        updateFeedDTO.setPassword(updatePassword);

        checkUpdatedFeedFail(createdUserFeed.getId(), updateFeedDTO, "[FeedService] ?????? ?????? ?????? ?????? ???????????? ?????????????????????.");
    }

    @Transactional
    @Test
    void ??????_??????_??????() {
        FeedDTO.Request userFeedDTO = testFeedDTO.makeUserFeedDTO();

        Feed createdUserFeed = feedService.createFeed(testFeedDTO.getUserId(), userFeedDTO);
        Feed deletedUserFeed = feedService.deleteFeed(createdUserFeed.getId());

        assertEquals(deletedUserFeed.getStatus(), Status.inactive());
    }

    @Transactional
    @Test
    void ??????_??????_?????????_??????() {
        FeedDTO.Request nonUserFeedDTO = testFeedDTO.makeNonUserFeedDTO();

        Feed createdNonUserFeed = feedService.createFeed(null, nonUserFeedDTO);
        Feed deletedNonUserFeed = feedService.deleteFeed(createdNonUserFeed.getId());

        assertEquals(deletedNonUserFeed.getStatus(), Status.inactive());
    }

    @Test
    void ??????_??????_??????_??????() {
        Feed createUserFeed = feedService.createFeed(testFeedDTO.getUserId(), testFeedDTO.makeUserFeedDTO());
        Feed createNonUserFeed = feedService.createFeed(null, testFeedDTO.makeNonUserFeedDTO());

        imageService.deleteAll(feedTestDir);
        feedService.deleteAll();

        checkDeletedFeedFail(0L, "[FeedService] ????????? ?????? ID??? ???????????? ????????????.");
        checkDeletedFeedFail(null, "[FeedService] ????????? ?????? ID??? ???????????? ????????????.");
        checkDeletedFeedFail(createUserFeed.getId(), "[FeedService] ?????? ?????? ID??? ???????????? ????????????.");
        checkDeletedFeedFail(createNonUserFeed.getId(), "[FeedService] ?????? ?????? ID??? ???????????? ????????????.");
    }

    private void checkCreatedFeedSuccess(Long userId, FeedDTO.Request feedDTO, Feed createdFeed) {
        assertEquals(createdFeed.getContent().getContent(), feedDTO.getContent());

        if (userId != null) {
            assertEquals(createdFeed.getUserId().getId(), userId);
            assertNull(createdFeed.getNonUser());
            return;
        }
        assertEquals(createdFeed.getNonUser().getPassword().getPassword(), feedDTO.getPassword());
        assertNull(createdFeed.getUserId());
    }

    private void checkCreatedFeedFail(Long userId, FeedDTO.Request feedDTO, String errorMessage) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            feedService.createFeed(userId, feedDTO);
        });
        if (errorMessage != null) {
            assertEquals(exception.getMessage(), errorMessage);
        }
    }

    private void checkFoundUserFeedFail(Pageable pageable, Long userId, String errorMessage) {
        assertEquals(
                assertThrows(IllegalArgumentException.class, () -> {
                    feedService.findUserFeeds(pageable, userId);
                }).getMessage(),
                errorMessage);
    }

    private void checkFoundNonUserFeedFail(Long feedId, String errorMessage) {
        assertEquals(
                assertThrows(IllegalArgumentException.class, () -> {
                    feedService.findOneFeed(feedId);
                }).getMessage(),
                errorMessage);
    }

    private List<Feed> addFeeds(int nonUserFeedNum, int userFeedNum) {
        List<Feed> allFeeds = new ArrayList<>();
        FeedDTO.Request nonUserFeedDTO = testFeedDTO.makeNonUserFeedDTO();
        for (int num = 0; num < nonUserFeedNum; num++) {
            allFeeds.add(feedService.createFeed(null, nonUserFeedDTO));
        }
        FeedDTO.Request userFeedDTO = testFeedDTO.makeUserFeedDTO();
        for (int num = 0; num < userFeedNum; num++) {
            allFeeds.add(feedService.createFeed(testFeedDTO.getUserId(), userFeedDTO));
        }
        return allFeeds;
    }

    private void checkFoundAllFeedsSuccess(List<Feed> cratedAllFeeds, Page<Feed> foundAllFeeds, int allNonUserFeedsNum, int allUserFeedsNum) {
        assertEquals(foundAllFeeds.getContent().size(), allNonUserFeedsNum + allUserFeedsNum);
        for (int num = 0; num < allNonUserFeedsNum + allUserFeedsNum; num++) {
            assertEquals(foundAllFeeds.getContent().get(num), cratedAllFeeds.get(num));
        }
    }

    private void checkFoundAllFeedsFail(Pageable pageable, String errorMessage) {
        assertEquals(
                assertThrows(IllegalArgumentException.class, () -> {
                    feedService.findAllFeeds(pageable);
                }).getMessage(),
                errorMessage);
    }

    private void checkFoundAllActiveFeedsFail(Pageable pageable, Long userId, String category, String errorMessage) {
        assertThrows(IllegalArgumentException.class, () -> {
            feedService.findAllActiveFeeds(pageable, userId, category);
        }, errorMessage);
    }

    private void checkUpdatedFeedFail(Long feedId, UpdateFeedDTO.Request feedDTO, String errorMessage) {
        assertEquals(
                assertThrows(IllegalArgumentException.class, () -> {
                    feedService.updateOneFeed(feedId, feedDTO);
                }).getMessage(),
                errorMessage);
    }

    private void checkDeletedFeedFail(Long feedId, String errorMessage) {
        assertEquals(
                assertThrows(IllegalArgumentException.class, () -> {
                    feedService.deleteFeed(feedId);
                }).getMessage(),
                errorMessage);
    }

    private void checkUploadedImageSuccess(Feed feed, Images createdImages, ArrayList<MultipartFile> images) {
        assertEquals(createdImages.getSize(), images.size());
        for (int i = 0; i < images.size(); i++) {
            assertEquals(createdImages.getImages().get(i).getFeedId().getId(), feed.getId());
        }
    }

    private void checkUploadedUserFeedImageFail(Long feedId, Long userId, ArrayList<MultipartFile> images, String errorMessage) {
        assertEquals(
                assertThrows(IllegalArgumentException.class, () -> {
                    feedService.uploadImages(feedId, userId, images);
                }).getMessage(),
                errorMessage);
    }

    private void createFeed(String category) {
        testFeedDTO.setCategory(category);
        FeedDTO.Request nonUserFeed = testFeedDTO.makeNonUserFeedDTO();
        feedService.createFeed(null, nonUserFeed);
    }
}
