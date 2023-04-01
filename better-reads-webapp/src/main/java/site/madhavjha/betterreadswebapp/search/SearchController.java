package site.madhavjha.betterreadswebapp.search;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import site.madhavjha.betterreadswebapp.utils.AppConstants;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class SearchController {

    private final WebClient webClient;

    public SearchController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024))
                .build()).baseUrl("http://openlibrary.org/search.json").build();
    }

    @GetMapping(value = "/search")
    public String getSearchResults(@RequestParam String query, Model model) {
        Mono<SearchResult> resultsMono = this.webClient.get()
                .uri("?q={query}", query)
                .retrieve().bodyToMono(SearchResult.class);
        SearchResult result = resultsMono.block();

        List<SearchResultBook> books = result.getDocs()
                .stream()
                .limit(10)
                .map(this::mapSearchResult)
                .collect(Collectors.toList());

        model.addAttribute("searchResults", books);

        return "search";
    }

    private SearchResultBook mapSearchResult(SearchResultBook bookResult) {
        bookResult.setKey(bookResult.getKey().replace("/works/", ""));
        String coverId = bookResult.getCover_i();
        if (StringUtils.hasText(coverId)) {
            coverId = AppConstants.COVER_IMAGE_ROOT + coverId + AppConstants.COVER_IMAGE_EXTENSION_M;
        } else {
            coverId = AppConstants.NO_IMAGE_PNG;
        }
        bookResult.setCover_i(coverId);
        return bookResult;
    }
}
