package com.baechu.member.service;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baechu.common.dto.BaseResponse;
import com.baechu.common.exception.CustomException;
import com.baechu.common.exception.ErrorCode;
import com.baechu.common.exception.SuccessCode;
import com.baechu.member.dto.LoginDto;
import com.baechu.member.dto.SigninDto;
import com.baechu.member.entity.Member;
import com.baechu.member.repository.MemberRepository;
import com.baechu.session.SessionConst;

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

	@Transactional
	public ResponseEntity<BaseResponse> login(LoginDto loginDto, HttpServletRequest request) {

		Member findMember = memberRepository.findByEmail(loginDto.getEmail()).orElseThrow(
			()-> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		if(findMember.getPassword().equals(loginDto.getPassword())){

			HttpSession session = request.getSession();
			session.setMaxInactiveInterval(1800);
			session.setAttribute(SessionConst.LOGIN_MEMBER,findMember);

			return BaseResponse.toResponseEntity(SuccessCode.LOGIN_SUCCESS);

		}else {
			throw new CustomException(ErrorCode.INVALIDATION_PASSWORD);
		}

	}


	public ResponseEntity<BaseResponse> logout(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}

		return BaseResponse.toResponseEntity(SuccessCode.LOGOUT_SUCCESS);
	}
}
