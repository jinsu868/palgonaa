package com.palgona.palgona.product.application;

import com.palgona.palgona.bookmark.domain.BookmarkRepository;
import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.fcm.domain.SilentNotifications;
import com.palgona.palgona.image.application.S3Service;
import com.palgona.palgona.image.domain.Image;
import com.palgona.palgona.image.dto.ImageUploadRequest;
import com.palgona.palgona.image.util.FileUtils;
import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.notification.domain.SilentNotificationsRepository;
import com.palgona.palgona.product.domain.Category;
import com.palgona.palgona.product.domain.Product;
import com.palgona.palgona.product.domain.ProductImage;
import com.palgona.palgona.product.domain.ProductState;
import com.palgona.palgona.product.domain.SortType;
import com.palgona.palgona.product.dto.request.ProductCreateRequest;
import com.palgona.palgona.product.dto.response.ProductDetailResponse;
import com.palgona.palgona.product.dto.response.ProductPageResponse;
import com.palgona.palgona.bidding.domain.BiddingRepository;
import com.palgona.palgona.image.domain.ImageRepository;
import com.palgona.palgona.product.domain.ProductRepository;
import com.palgona.palgona.product.event.ImageUploadEvent;
import com.palgona.palgona.product.infrastructure.querydto.ProductDetailQueryResponse;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.palgona.palgona.common.error.code.ProductErrorCode.*;
import static com.palgona.palgona.common.error.code.SlientNotificationsErrorCode.NOTIFICATION_ALREADY_SILENCED;
import static com.palgona.palgona.common.error.code.SlientNotificationsErrorCode.NOTIFICATION_NOT_FOUND;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ImageRepository imageRepository;
    private final BookmarkRepository bookmarkRepository;
    private final BiddingRepository biddingRepository;
    private final S3Service s3Service;
    private final SilentNotificationsRepository silentNotificationsRepository;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public void createProduct(ProductCreateRequest request, Member member) {

        Product product = Product.of(
                request.name(),
                request.initialPrice(),
                request.content(),
                request.category(),
                request.deadline(),
                ProductState.ON_SALE,
                member
        );

        List<ImageUploadRequest> uploadRequests = new ArrayList<>();
        List<ProductImage> productImages = new ArrayList<>();

        for (MultipartFile imageFile : request.files()) {
            String uploadFileName = FileUtils.createFileName(imageFile.getOriginalFilename());
            String imageUrl = s3Service.generateS3FileUrl(uploadFileName);
            uploadRequests.add(new ImageUploadRequest(imageFile, uploadFileName));
            Image image = Image.from(imageUrl);
            productImages.add(ProductImage.of(product, image));
        }

        product.addProductImages(productImages);

        productRepository.save(product);
        publisher.publishEvent(new ImageUploadEvent(uploadRequests));
    }

    public ProductDetailResponse readProduct(Long productId, CustomMemberDetails memberDetail){
        Member member = memberDetail.getMember();

        //1. 상품 정보 가져오기(상품, 멤버 정보, 최고 입찰가, 북마크 개수, 채팅 개수)
        ProductDetailQueryResponse queryResponse = productRepository.findProductDetailsById(productId, member)
                .orElseThrow(() -> new BusinessException(NOT_FOUND));
        log.info(queryResponse.toString());


        //상품이 삭제되었는지 확인
        if(queryResponse.productState() == ProductState.DELETED){
            throw new BusinessException(DELETED_PRODUCT);
        }

        //2. 컬렉션 정보 가져오기(상품 이미지)
        List<String> imageUrls= imageRepository.findAllByProductId(productId).stream()
                .map(Image::getImageUrl)
                .toList();
        log.info(imageUrls.toString());


        return ProductDetailResponse.from(queryResponse, imageUrls);
    }

    @Transactional(readOnly = true)
    public SliceResponse<ProductPageResponse> readProducts(SortType sortType,
                                                           Category category,
                                                           String searchWord,
                                                           String cursor,
                                                           int pageSize
    ) {
        return productRepository.findAllByCategoryAndSearchWord(category, searchWord, cursor, sortType, pageSize);
    }

    @Transactional
    public void deleteProduct(Long productId, Member member) {

        Product product = findProduct(productId);

        validateProductPermission(member, product);
        validateProductDelete(product);

        bookmarkRepository.deleteByProduct(product);

        // TODO: history 테이블로 이전시키기
        product.updateProductState(ProductState.DELETED);
    }

    public void turnOffProductNotification(Long productId, CustomMemberDetails memberDetails){
        Member member = memberDetails.getMember();

        //1. 해당 상품이 존재하는지 확인
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND));

        //2. 해당 상품에 알림 무시가 이미 설정되어있는지 확인
        silentNotificationsRepository.findByMemberAndProduct(member, product)
                .ifPresent(b -> {
                    throw new BusinessException(NOTIFICATION_ALREADY_SILENCED);
                });

        //3. 알림 무시 추가
        SilentNotifications silentNotifications = SilentNotifications.builder()
                .member(member)
                .product(product)
                .build();

        silentNotificationsRepository.save(silentNotifications);
    }

    public void turnOnProductNotification(Long productId, CustomMemberDetails memberDetails){
        Member member = memberDetails.getMember();

        //1. 해당 상품이 존재하는지 확인
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND));

        //2. 해당 상품에 알림 무시가 설정되어있는지 확인
        SilentNotifications silentNotifications = silentNotificationsRepository.findByMemberAndProduct(member, product)
                .orElseThrow(() -> new BusinessException(NOTIFICATION_NOT_FOUND));

        //3. 알림 무시 삭제
        silentNotificationsRepository.delete(silentNotifications);
    }

    @Transactional
    public void addImage(Member member, Long productId, MultipartFile file) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND));

        if (!product.isOwner(member)) {
            throw new BusinessException(INSUFFICIENT_PERMISSION);
        }

        String uploadFileName = FileUtils.createFileName(file.getOriginalFilename());
        String imageUrl = s3Service.generateS3FileUrl(uploadFileName);

        Image image = Image.from(imageUrl);
        ProductImage productImage = ProductImage.of(product, image);
        product.addProductImage(productImage);

        publisher.publishEvent(ImageUploadEvent.from(List.of(ImageUploadRequest.of(file, uploadFileName))));
    }

    private void validateProductDelete(Product product){
        if (biddingRepository.existsByProduct(product)) {
            throw new BusinessException(RELATED_BIDDING_EXISTS);
        }
    }

    private void validateProductPermission(Member member, Product product) {
        if (!(product.isOwner(member) || member.isAdmin())) {
            throw new BusinessException(INSUFFICIENT_PERMISSION);
        }
    }

    private List<Long> toImageIds(List<Image> images) {
        return images.stream()
                .map(image -> image.getImageId())
                .toList();
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(NOT_FOUND));
    }
}