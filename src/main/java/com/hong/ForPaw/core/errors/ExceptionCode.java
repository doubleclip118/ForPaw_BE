package com.hong.ForPaw.core.errors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ExceptionCode {
    // 사용자 관련 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    USER_EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 이메일을 찾을 수 없습니다."),
    USER_EMAIL_EXIST(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다."),
    USER_ACCOUNT_WRONG(HttpStatus.BAD_REQUEST, "이메일 또는 비밀번호를 다시 확인해 주세요"),
    USER_PASSWORD_WRONG(HttpStatus.BAD_REQUEST, "현재 비밀번호가 올바르지 않습니다."),
    USER_PASSWORD_MATCH_WRONG(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."), // 두 비밀번호 일치 여부 확인
    USER_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "사용자 정보를 수정할 권한이 없습니다."),
    USER_FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    USER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않았습니다"),

    // 이메일 코드 관련 에러
    CODE_EXPIRED(HttpStatus.BAD_REQUEST, "유효기간이 만료되었습니다."),
    CODE_WRONG(HttpStatus.BAD_REQUEST, "잘못된 인증코드입니다."),
    CODE_NOT_SEND(HttpStatus.INTERNAL_SERVER_ERROR, "인증코드를 전송하지 못했습니다."),

    // 토큰 관련 에러
    TOKEN_WRONG(HttpStatus.BAD_REQUEST, "잘못된 토큰 형식입니다."),
    TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "토큰이 만료됐습니다."),

    // 동물 관련 에러
    ANIMAL_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 동물을 찾을 수 없습니다."),
    ANIMAL_NOT_EXIST(HttpStatus.NOT_FOUND, "목록에 보호 동물이 존재하지 않습니다."),
    ANIMAL_ALREADY_APPLY(HttpStatus.BAD_REQUEST, "이미 지원하였습니다."),
    APPLY_NOT_FOUND(HttpStatus.BAD_REQUEST, "지원서가 존재하지 않습니다."),
    SHELTER_NOT_FOUND(HttpStatus.NOT_FOUND, "보호소가 존재하지 않습니다"),

    // 그룹 관련 에러
    GROUP_NAME_EXIST(HttpStatus.BAD_REQUEST, "이미 존재하는 이름입니다."),
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "그룹이 존재하지 않습니다."),
    GROUP_ALREADY_JOIN(HttpStatus.BAD_REQUEST, "이미 가입하였거나, 신청이 완료되었습니다."),
    GROUP_NOT_JOIN(HttpStatus.BAD_REQUEST, "가입 신청을 하지 않았습니다"),

    // 잘못된 접근
    BAD_APPROACH(HttpStatus.BAD_REQUEST, "잘못된 접근입니다."),
    EXCEED_REQUEST_NUM(HttpStatus.BAD_REQUEST, "가능한 요청 횟수를 초과하였습니다."),

    // 검색
    SEARCH_NOT_FOUND(HttpStatus.NOT_FOUND, "검색 결과값이 존재하지 않습니다");

    private final HttpStatus httpStatus;
    private final String message;
}
