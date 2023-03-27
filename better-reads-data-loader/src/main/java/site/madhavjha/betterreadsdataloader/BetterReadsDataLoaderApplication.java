package site.madhavjha.betterreadsdataloader;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import site.madhavjha.betterreadsdataloader.author.Author;
import site.madhavjha.betterreadsdataloader.author.AuthorRepository;
import site.madhavjha.betterreadsdataloader.book.Book;
import site.madhavjha.betterreadsdataloader.book.BookRepository;
import site.madhavjha.betterreadsdataloader.connection.DataStaxAstraProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableConfigurationProperties(DataStaxAstraProperties.class)
@Slf4j
public class BetterReadsDataLoaderApplication {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Value("${datadump.location.authors}")
    private String authorsDataDumpLocation;
    @Value("${datadump.location.works}")
    private String worksDataDumpLocation;


    private void initAuthors() {
        Path path = Paths.get(authorsDataDumpLocation);
        try (var lines = Files.lines(path)) {
            lines.forEach(line -> {
                var jsonString = line.substring(line.indexOf("{"));

                try {
                    var jsonObject = new JSONObject(jsonString);

                    var author = new Author();
                    author.setId(jsonObject.optString("key").replace("/authors/", ""));
                    author.setName(jsonObject.optString("name"));
                    author.setPersonalName(jsonObject.optString("personal_name"));
                    log.info("Saving Author {}", author.getName());

                    authorRepository.save(author);
                } catch (JSONException jsonException) {
                    log.error(jsonException.getMessage());
                }
            });

        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    private void initWorks() {
        Path path = Paths.get(worksDataDumpLocation);
        var dateFormat=DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        try (var lines = Files.lines(path)) {
            lines.forEach(line -> {
                var jsonString = line.substring(line.indexOf("{"));

                try {
                    var jsonObject = new JSONObject(jsonString);

                    var book = new Book();

                    book.setId(jsonObject.getString("key").replace("/works/",""));

                    book.setName(jsonObject.optString("title"));

                    var descriptionObject = jsonObject.optJSONObject("description");
                    if (Objects.nonNull(descriptionObject)) {
                        book.setDescription(descriptionObject.optString("value"));
                    }

                    var publishedObject = jsonObject.optJSONObject("created");
                    if (Objects.nonNull(publishedObject)) {
                        book.setPublishedDate(LocalDate.parse(publishedObject.optString("value"),dateFormat));
                    }

                    var coversArray = jsonObject.optJSONArray("covers");
                    if (Objects.nonNull(coversArray)) {
                        List<String> coverIds = new ArrayList<>(coversArray.length());
                        for (int i = 0; i < coversArray.length(); i++) {
                            coverIds.add(String.valueOf(coversArray.getInt(i)));
                        }
                        book.setCoverIds(coverIds);
                    }

                    var authorArray = jsonObject.optJSONArray("authors");
                    if (Objects.nonNull(authorArray)) {
                        List<String> authorIds = new ArrayList<>(authorArray.length());
                        for (int i = 0; i < authorArray.length(); i++) {
                            var authorId = authorArray.getJSONObject(i).getJSONObject("author").getString("key").replace("/authors/", "");
                            authorIds.add(authorId);
                        }
                        book.setAuthorIds(authorIds);

                        var authorNames = authorIds.stream().map(id -> authorRepository.findById(id)).map(optionalAuthor -> {
                            if (optionalAuthor.isPresent()){
                                return optionalAuthor.get().getName();
                            }
                            return "Unknown Author";
                        }).toList();

                        book.setAuthorNames(authorNames);
                    }

					log.info("Saving Book  {} ", book.getName());
                    bookRepository.save(book);

                } catch (JSONException jsonException) {
                    log.error(jsonException.getMessage());
                }
            });

        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    @PostConstruct
    public void start() {
        log.info("Authors datadump location {} ", authorsDataDumpLocation);
        log.info("Works datadump location {} ", worksDataDumpLocation);
        initAuthors();
        initWorks();
        log.info("Authors and work dumped in Cassandra");
    }

    public static void main(String[] args) {
        SpringApplication.run(BetterReadsDataLoaderApplication.class, args);
    }

}
