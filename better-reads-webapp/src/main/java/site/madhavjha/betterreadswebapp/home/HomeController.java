package site.madhavjha.betterreadswebapp.home;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import site.madhavjha.betterreadswebapp.user.BooksByUser;
import site.madhavjha.betterreadswebapp.user.BooksByUserRepository;
import site.madhavjha.betterreadswebapp.utils.AppConstants;

import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    BooksByUserRepository booksByUserRepository;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal == null || principal.getAttribute("login") == null) {
            return "index";
        }

        String userId = principal.getAttribute("login");
        var booksSlice = booksByUserRepository.findAllById(userId, CassandraPageRequest.of(0, 100));
        var booksByUser = booksSlice.getContent();
        booksByUser = booksByUser.stream().distinct().map(HomeController::mapBooksByUser).collect(Collectors.toList());
        model.addAttribute("books", booksByUser);
        return "home";
    }

    private static BooksByUser mapBooksByUser(BooksByUser book) {
        var coverImageUrl = AppConstants.NO_IMAGE_PNG;
        if (Objects.nonNull(book.getCoverIds()) && !book.getCoverIds().isEmpty()) {
            coverImageUrl = AppConstants.COVER_IMAGE_ROOT + book.getCoverIds().get(0) + AppConstants.COVER_IMAGE_EXTENSION_M;
        }
        book.setCoverUrl(coverImageUrl);
        return book;
    }
}
