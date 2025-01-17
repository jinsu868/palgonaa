package com.palgona.palgona.controller;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.common.jwt.util.JwtUtils;
import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.member.domain.Role;
import com.palgona.palgona.member.domain.Status;
import com.palgona.palgona.product.domain.SortType;
import com.palgona.palgona.product.dto.response.ProductPageResponse;
import com.palgona.palgona.member.domain.MemberRepository;
import com.palgona.palgona.product.application.ProductService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    MemberRepository memberRepository;

    @MockBean
    ProductService productService;

    @BeforeEach
    void setUp() {
        memberRepository.save(Member.of(0, Status.ACTIVE, "100", Role.USER));
    }

    @Test
    void 상품_조회_테스트() throws Exception {
        SliceResponse<ProductPageResponse> response = SliceResponse.of(
                List.of(
                        new ProductPageResponse(
                                1L,
                                "상품",
                                1000,
                                1,
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                "qwer.png"
                        ),
                        new ProductPageResponse(
                                2L,
                                "상품",
                                1000,
                                1,
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                "qwer.png"
                        )), null, null);

        given(productService.readProducts(SortType.LATEST, null, null, null, 20)).willReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products")
                .header(AUTHORIZATION, "BEARER " + jwtUtils.createAccessToken("100")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.values[0].id").value(1))
                .andExpect(jsonPath("$.values[1].id").value(2));
    }
}