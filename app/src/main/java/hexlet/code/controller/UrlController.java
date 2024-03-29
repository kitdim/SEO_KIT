package hexlet.code.controller;

import hexlet.code.dto.UrlPage;
import hexlet.code.dto.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlsCheckRepository;
import hexlet.code.repository.UrlsRepository;
import hexlet.code.util.NamedRoutes;
import hexlet.code.util.Normalizer;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.Collections;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Slf4j
public class UrlController {
    private static final String INCORRECT_URL = "Incorrect URL";
    private static final String ALREADY_EXITS = "This page already exist";
    private static final String SUCCESSFULLY = "Page added successfully";

    public static void index(Context ctx) throws SQLException {
        List<Url> urls = UrlsRepository.getEntities();
        Map<Long, UrlCheck> urlChecks = UrlsCheckRepository.findLatestChecks();
        var page = new UrlsPage(urls, urlChecks);
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flash-type"));
        ctx.render("urls/index.jte", Collections.singletonMap("page", page));
    }

    public static void show(Context ctx) throws SQLException {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        Url url = UrlsRepository.find(id)
                .orElseThrow(() -> new NotFoundResponse("Entity with id = " + id + " not found"));
        url.setId(id);
        UrlPage page = new UrlPage(url);
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flash-type"));
        ctx.render("urls/show.jte", Collections.singletonMap("page", page));
    }

    public static void create(Context ctx) throws SQLException {
        String input = ctx.formParamAsClass("url", String.class)
                .get()
                .trim()
                .toLowerCase();
        String normalizedUrl;
        try {
            URL someUrl = new URL(input);
            normalizedUrl = Normalizer.getNormalizedURL(someUrl);
        } catch (Exception exception) {
            log.info(exception.getMessage());
            ctx.sessionAttribute("flash", INCORRECT_URL);
            ctx.sessionAttribute("flash-type", "warning");
            ctx.redirect(NamedRoutes.rootPath());
            return;
        }

        boolean isFind = UrlsRepository.findByName(normalizedUrl).isPresent();
        if (isFind) {
            log.info("info");
            ctx.sessionAttribute("flash", ALREADY_EXITS);
            ctx.sessionAttribute("flash-type", "info");
            ctx.redirect(NamedRoutes.urlsPath());
        } else {
            log.info("success");
            Url url = Url.builder().name(normalizedUrl).build();
            UrlsRepository.save(url);
            ctx.sessionAttribute("flash", SUCCESSFULLY);
            ctx.sessionAttribute("flash-type", "success");
            ctx.redirect(NamedRoutes.urlsPath());
        }
    }
}
