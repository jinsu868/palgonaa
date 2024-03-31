package com.palgona.palgona.common.fcm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.palgona.palgona.common.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

import static com.palgona.palgona.common.error.code.FCMErrorCode.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class FCMUtil {

    @Value("${fcm.project-name}")
    private String projectName;
    private static final String API_URL = "https://fcm.googleapis.com/v1/projects/%s/messages:send";
    private static final String GOOGLE_CLOUD_PLATFORM_SCOPE = "https://www.googleapis.com/auth/cloud-platform";

    private final String firebaseConfigPath = "FCMAccountKey.json";

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public void sendMessageTo(String targetToken, String title, String body) {
        try {
            String message = makeMessage(targetToken, title, body);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(getAccessToken());
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(message, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    String.format(API_URL, projectName),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("FCM Response: {}", response.getBody());
        } catch (Exception e) {
            throw new BusinessException(FAILED_TO_SEND_FCM);
        }
    }
    private String makeMessage(String targetToken, String title, String body) {
        try {
            FcmMessage fcmMessage = FcmMessage.builder()
                    .message(FcmMessage.Message.builder()
                            .token(targetToken)
                            .notification(FcmMessage.Notification.builder()
                                    .title(title)
                                    .body(body)
                                    .build())
                            .build())
                    .build();

            return objectMapper.writeValueAsString(fcmMessage);
        } catch (JsonProcessingException e) {
            throw new BusinessException(FAILED_TO_GENERATE_FCM_JSON);
        }
    }

    private String getAccessToken(){
        try {
            GoogleCredentials googleCredentials = GoogleCredentials
                    .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
                    .createScoped(List.of(GOOGLE_CLOUD_PLATFORM_SCOPE));

            googleCredentials.refreshIfExpired();
            return googleCredentials.getAccessToken().getTokenValue();
        } catch (IOException e) {
            throw new BusinessException(FAILED_TO_GET_ACCESS_TOKEN);
        }
    }


}
