package com.gloomy.server.application.image;

import com.gloomy.server.application.image.s3.S3Uploader;
import com.gloomy.server.domain.common.Status;
import com.gloomy.server.domain.feed.Feed;
import com.gloomy.server.domain.image.Image;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ImageService {
    private final S3Uploader s3Uploader;
    private final ImageRepository imageRepository;

    @Value("${cloud.aws.s3.feedDir}")
    private String feedDir;

    public ImageService(S3Uploader s3Uploader, ImageRepository imageRepository) {
        this.s3Uploader = s3Uploader;
        this.imageRepository = imageRepository;
    }

    @Transactional
    public Images uploadMany(Feed feed, List<MultipartFile> multipartFiles) throws IllegalArgumentException {
        validateImages(feed, multipartFiles);
        Images images = new Images();
        if (!ObjectUtils.isEmpty(multipartFiles)) {
            for (MultipartFile multipartFile : multipartFiles) {
                Image createdImage = uploadOne(feed, multipartFile);
                images.addImage(createdImage);
            }
        }
        return images;
    }

    private void validateImages(Feed feed, List<MultipartFile> multipartFiles) throws IllegalArgumentException {
        if (feed == null) {
            throw new IllegalArgumentException("[ImageService] 피드가 존재하지 않습니다.");
        }
        if (multipartFiles == null) {
            throw new IllegalArgumentException("[ImageService] 이미지 파일이 존재하지 않습니다.");
        }
    }

    private Image uploadOne(Feed feed, MultipartFile multipartFile) {
        String dirName = feedDir + feed.getId();
        String uploadImageUrl = s3Uploader.upload(dirName, multipartFile);
        Image image = Image.of(feed, uploadImageUrl);
        return imageRepository.save(image);
    }

    public Images findImages(Feed feedId) throws IllegalArgumentException {
        if (feedId == null) {
            throw new IllegalArgumentException("[ImageService] 피드가 유효하지 않습니다.");
        }
        return new Images(imageRepository.findAllByFeedId(feedId));
    }

    public Images findActiveImages(Feed feedId) throws IllegalArgumentException {
        if (feedId == null) {
            throw new IllegalArgumentException("[ImageService] 피드가 유효하지 않습니다.");
        }
        return new Images(imageRepository.findAllByFeedIdAndStatus(feedId, Status.ACTIVE));
    }

    public Images updateImages(Feed feedId, List<MultipartFile> images) {
        deleteImages(feedId);
        return uploadMany(feedId, images);
    }

    @Transactional
    public Images deleteImages(Feed feedId) throws IllegalArgumentException {
        if (feedId == null) {
            throw new IllegalArgumentException("[ImageService] 피드가 유효하지 않습니다.");
        }
        List<Image> foundImages = imageRepository.findAllByFeedId(feedId);
        for (Image foundImage : foundImages) {
            foundImage.delete();
        }
        return new Images(imageRepository.saveAll(foundImages));
    }

    public void deleteAll() {
        imageRepository.deleteAll();
        s3Uploader.deleteAll();
    }
}