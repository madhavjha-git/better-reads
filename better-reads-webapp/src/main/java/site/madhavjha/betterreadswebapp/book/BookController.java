package site.madhavjha.betterreadswebapp.book;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import site.madhavjha.betterreadswebapp.userbooks.UserBooks;
import site.madhavjha.betterreadswebapp.userbooks.UserBooksPrimaryKey;
import site.madhavjha.betterreadswebapp.userbooks.UserBooksRepository;
import site.madhavjha.betterreadswebapp.utils.AppConstants;

import java.util.Objects;
import java.util.Optional;

@Controller
public class BookController {

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    UserBooksRepository userBooksRepository;

    @GetMapping(value = "/books/{bookId}")
    public String getBook(@PathVariable String bookId, Model model, @AuthenticationPrincipal OAuth2User principal) {
        var optionalBook = bookRepository.findById(bookId);
        if (optionalBook.isPresent()) {
            var book = optionalBook.get();
            var coverImageUrl = AppConstants.NO_IMAGE_PNG;
            if (Objects.nonNull(book.getCoverIds()) && !book.getCoverIds().isEmpty()) {
                coverImageUrl = AppConstants.COVER_IMAGE_ROOT + book.getCoverIds().get(0) + AppConstants.COVER_IMAGE_EXTENSION_L;
            }

            model.addAttribute("coverImage", coverImageUrl);
            model.addAttribute("book", book);

            if (principal != null && principal.getAttribute("login") != null) {
                String userId = principal.getAttribute("login");
                model.addAttribute("loginId", userId);

                var key = new UserBooksPrimaryKey();
                key.setBookId(bookId);
                key.setUserId(userId);

                var userBooks = userBooksRepository.findById(key);
                if (userBooks.isPresent()) {
                    model.addAttribute("userBooks", userBooks.get());
                } else {
                    model.addAttribute("userBooks", new UserBooks());
                }
            }

            return "book";
        }
        return "book-not-found";
    }
}
