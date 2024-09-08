package com.palgona.palgona.image.domain;


import com.palgona.palgona.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Image extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @Column(nullable = false)
    private String imageUrl;

    public Image(String imageUrl){
        this.imageUrl = imageUrl;
    }

    public static Image from(String imageUrl) {
        return new Image(imageUrl);
    }
}