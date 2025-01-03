package com.kkd.sbb.question;

import com.kkd.sbb.answer.Answer;
import com.kkd.sbb.answer.AnswerForm;
import com.kkd.sbb.answer.AnswerService;
import com.kkd.sbb.category.Category;
import com.kkd.sbb.category.CategoryService;
import com.kkd.sbb.comment.Comment;
import com.kkd.sbb.comment.CommentForm;
import com.kkd.sbb.comment.CommentService;
import com.kkd.sbb.user.SiteUser;
import com.kkd.sbb.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RequestMapping("/question")
@Controller
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final UserService userService;
    private final AnswerService answerService;
    private final CommentService commentService;
    private final CategoryService categoryService;

    @GetMapping("/list")
    public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "kw", defaultValue = "") String kw) {
        Page<Question> paging = this.questionService.getList(page, kw);
        List<Category> categoryList = this.categoryService.getAll();
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("category_list", categoryList);
        return "question_list";
    }

    @GetMapping(value = "/detail/{id}")
    public String detail(Model model, @PathVariable("id") Integer id, AnswerForm answerForm,
                         CommentForm commentForm,
                         @RequestParam(value="ans-page", defaultValue="0") int answerPage,
                         @RequestParam(value="ans-ordering", defaultValue="time") String answerOrderMethod) {
        this.questionService.viewUp(id);
        Question question = this.questionService.getQuestion(id);
        Page<Answer> answerPaging = this.answerService.getAnswerList(question,
                answerPage, answerOrderMethod);
        List<Comment> commentList = this.commentService.getCommentList(question);
        List<Category> categoryList = this.categoryService.getAll();

        model.addAttribute("question", question);
        model.addAttribute("ans_paging", answerPaging);
        model.addAttribute("comment_list", commentList);
        model.addAttribute("category_list", categoryList);
        return "question_detail";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String questionCreate(QuestionForm questionForm, Model model) {
        List<Category> categoryList = this.categoryService.getAll();
        model.addAttribute("category_list", categoryList);
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String questionCreate(@Valid QuestionForm questionForm, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "question_form";
        }
        SiteUser siteUser = this.userService.getUser(principal.getName());
        Category category = this.categoryService.getCategoryByName(questionForm.getCategory());
        System.out.println("category.getName() = " + category.getName());
        this.questionService.create(questionForm.getSubject(), questionForm.getContent(),
                category, siteUser);
        return "redirect:/question/list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String questionModify(QuestionForm questionForm, @PathVariable("id") Integer id, Principal principal, Model model) {
        Question question = this.questionService.getQuestion(id);
        if(!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        List<Category> categoryList = this.categoryService.getAll();
        model.addAttribute("category_list", categoryList);

        questionForm.setId(question.getId());
        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());
        return "question_modify_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String questionModify(@Valid QuestionForm questionForm, BindingResult bindingResult,
                                 Principal principal, @PathVariable("id") Integer id) {
        if (bindingResult.hasErrors()) {
            return "question_form";
        }
        Question question = this.questionService.getQuestion(id);
        if(!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        Category category = this.categoryService.getCategoryByName(questionForm.getCategory());
        question.setCategory(category);

        this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent());
        return String.format("redirect:/question/detail/%s", id);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String questionDelete(Principal principal, @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        if(!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }
        this.questionService.delete(question);
        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String questionVote(Principal principal, @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.questionService.vote(question, siteUser);
        return String.format("redirect:/question/detail/%s", id);
    }
}
