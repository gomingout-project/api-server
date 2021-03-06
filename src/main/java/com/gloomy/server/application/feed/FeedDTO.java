package com.gloomy.server.application.feed;

import com.gloomy.server.application.image.Images;
import com.gloomy.server.domain.feed.Feed;
import com.gloomy.server.domain.image.Image;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

public class FeedDTO {
    @Getter
    @Setter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Request {
        private String password;
        @NotBlank
        private String category;
        @NotBlank
        private String title;
        @NotBlank
        private String content;

        public Request(String category, String title, String content) {
            this.category = category;
            this.title = title;
            this.content = content;
        }

        public Request(String password, String category, String title, String content) {
            this.password = password;
            this.category = category;
            this.title = title;
            this.content = content;
        }
    }

    @Getter
    public static class Response {
        private final Long id;
        private final String ip;
        private final Long userId;
        private final String nickname;
        private final String password;
        private final String category;
        private final String title;
        private final String content;
        private final List<String> imageURLs;
        private final Integer likeCount;
        private final Integer commentCount;
        private final String status;
        private final String createdAt;
        private final String updatedAt;
        private final String deletedAt;

        @Builder
        public Response(Long id, String ip, Long userId, String nickName, String password, String category, String title, String content, Integer likeCount, List<String> imageURLs, Integer commentCount, String status, String createdAt, String updatedAt, String deletedAt) {
            this.id = id;
            this.ip = ip;
            this.userId = userId;
            this.nickname = nickName;
            this.password = password;
            this.category = category;
            this.title = title;
            this.content = content;
            this.imageURLs = imageURLs;
            this.likeCount = likeCount;
            this.commentCount = commentCount;
            this.status = status;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.deletedAt = deletedAt;
        }

        public static Response of(Feed feed, Images images, Integer likeCount, Integer commentCount) {
            List<String> imageURLs = new ArrayList<>();
            for (Image image : images.getImages()) {
                imageURLs.add(image.getImageUrl().getImageUrl());
            }
            if (feed.getUserId() != null) {
                return builder()
                        .id(feed.getId())
                        .ip(feed.getIp().getIp())
                        .userId(feed.getUserId().getId())
                        .nickName(null)
                        .password(null)
                        .category(feed.getCategory().toString())
                        .title(feed.getTitle().getTitle())
                        .content(feed.getContent().getContent())
                        .imageURLs(imageURLs)
                        .likeCount(likeCount)
                        .commentCount(commentCount)
                        .status(feed.getStatus().toString())
                        .createdAt(feed.getCreatedAt().getCreatedAt().toString())
                        .updatedAt(feed.getUpdatedAt().getUpdatedAt().toString())
                        .deletedAt(feed.getDeletedAt().getDeletedAt().toString())
                        .build();
            }
            return builder()
                    .id(feed.getId())
                    .ip(feed.getIp().getIp())
                    .userId(null)
                    .nickName(feed.getNonUser().getNickname().getNickname())
                    .password(feed.getNonUser().getPassword().getPassword())
                    .category(feed.getCategory().toString())
                    .title(feed.getTitle().getTitle())
                    .content(feed.getContent().getContent())
                    .likeCount(likeCount)
                    .imageURLs(imageURLs)
                    .commentCount(commentCount)
                    .status(feed.getStatus().toString())
                    .createdAt(feed.getCreatedAt().getCreatedAt().toString())
                    .updatedAt(feed.getUpdatedAt().getUpdatedAt().toString())
                    .deletedAt(feed.getDeletedAt().getDeletedAt().toString())
                    .build();
        }
    }
}
