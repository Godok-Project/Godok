package com.baechu.jumoon.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


import com.baechu.jumoon.entity.Jumoon;
import com.baechu.member.entity.Member;

public interface JumoonRepository extends JpaRepository<Jumoon, Long> {


	Optional<Jumoon> findById(Long id);

	Optional<Jumoon> findByIdAndMember(Long id, Member member);

	List<Jumoon> findAllByMember(Member member);

}
