package com.baechu.member.service;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baechu.common.dto.BaseResponse;
import com.baechu.common.exception.CustomException;
import com.baechu.common.exception.ErrorCode;
import com.baechu.common.exception.SuccessCode;
import com.baechu.member.dto.SigninDto;
import com.baechu.member.entity.Member;
import com.baechu.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

	private final MemberRepository memberRepository;

	@Transactional
	public ResponseEntity<BaseResponse> signin(SigninDto signinDto) {
		// 아이디 중복 검사
		Optional<Member> findEmail = memberRepository.findByEmail(signinDto.getEmail());
		if (findEmail.isPresent()) {
			throw new CustomException(ErrorCode.DUPLICATE_MEMBER);
		} else {
			memberRepository.save(new Member(signinDto));
			return BaseResponse.toResponseEntity(SuccessCode.SIGNUP_SUCCESS);
		}
	}
}
