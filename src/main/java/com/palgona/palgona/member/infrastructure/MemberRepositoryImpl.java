package com.palgona.palgona.member.infrastructure;

import static com.palgona.palgona.member.domain.QMember.member;

import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.member.dto.response.MemberResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public SliceResponse<MemberResponse> findAllOrderByIdDesc(Long cursor, int pageSize) {
        List<MemberResponse> members = queryFactory.select(
                Projections.constructor(MemberResponse.class,
                        member.id,
                        member.nickName,
                        member.profileImage))
                .from(member)
                .where(ltMemberId(cursor))
                .orderBy(member.id.desc())
                .limit(pageSize + 1)
                .fetch();

        return convertToSlice(members, pageSize);
    }

    private BooleanExpression ltMemberId(Long cursor) {
        if (cursor != null) {
            return member.id.lt(cursor);
        }

        return null;
    }

    private SliceResponse<MemberResponse> convertToSlice(
            List<MemberResponse> members,
            int pageSize
    ) {
        if (members.isEmpty()) {
            return SliceResponse.of(members, false, null);
        }

        boolean hasNext = existNextPage(members, pageSize);
        String nextCursor = generateCursor(members);
        return SliceResponse.of(members, hasNext, nextCursor);
    }

    private String generateCursor(List<MemberResponse> members) {
        MemberResponse lastMember = members.get(members.size() - 1);
        return String.valueOf(lastMember.id());
    }

    private boolean existNextPage(List<MemberResponse> members, int pageSize) {
        if (members.size() > pageSize) {
            members.remove(pageSize);
            return true;
        }

        return false;
    }
}
