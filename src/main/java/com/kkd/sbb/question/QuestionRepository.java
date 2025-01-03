package com.kkd.sbb.question;

import com.kkd.sbb.category.Category;
import com.kkd.sbb.user.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
    Question findBySubject(String subject);
    Question findBySubjectAndContent(String subject, String content);
    List<Question> findBySubjectLike(String subject);
    Page<Question> findAll(Pageable pageable);
    Page<Question> findAll(Specification<Question> spec, Pageable pageable);

    Page<Question> findByAuthor(SiteUser siteUser, Pageable pageable);
    Page<Question> findByCategory(Category category, Pageable pageable);

//    @Query("select "
//            + "distinct q "
//            + "from Question q "
//            + "left outer join SiteUser u1 on q.author=u1 "
//            + "left outer join Answer a on a.question=q "
//            + "left outer join SiteUser u2 on a.author=u2 "
//            + "where "
//            + "   q.subject like %:kw% "
//            + "   or q.content like %:kw% "
//            + "   or u1.username like %:kw% "
//            + "   or a.content like %:kw% "
//            + "   or u2.username like %:kw% ")
//    Page<Question> findAllByKeyword(@Param("kw") String kw, Pageable pageable);
}