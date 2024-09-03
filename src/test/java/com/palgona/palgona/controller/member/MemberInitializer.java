package com.palgona.palgona.controller.member;

import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.member.domain.Role;
import com.palgona.palgona.member.domain.Status;
import com.palgona.palgona.member.domain.MemberRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MemberInitializer {

    @Autowired
    private MemberRepository memberRepository;

    @PostConstruct
    public void init() {
        memberRepository.save(Member.of(0, Status.ACTIVE, "123", Role.GUEST));
        memberRepository.save(Member.of(0, Status.ACTIVE, "1234", Role.USER));
        memberRepository.save(Member.of(0, Status.ACTIVE, "12345", Role.ADMIN));
    }
}
