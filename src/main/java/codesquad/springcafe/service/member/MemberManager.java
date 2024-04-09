package codesquad.springcafe.service.member;

import codesquad.springcafe.domain.member.Member;
import codesquad.springcafe.repository.member.MemberRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberManager implements MemberService {
    private final MemberRepository repository;

    @Autowired
    public MemberManager(MemberRepository repository) {
        this.repository = repository;
    }

    @Override
    public Member join(Member member) {
        return repository.save(member);
    }

    @Override
    public Optional<Member> findMember(String loginId) {
        return repository.findById(loginId);
    }

    @Override
    public List<Member> findAllMember() {
        return repository.findAll();
    }
}
