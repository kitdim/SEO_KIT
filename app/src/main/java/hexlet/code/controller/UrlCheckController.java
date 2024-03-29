package hexlet.code.controller;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlsCheckRepository;
import hexlet.code.repository.UrlsRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.sql.SQLException;

public class UrlCheckController {
    private static final String INCORRECT_URL = "Incorrect URL";
    private static final String SUCCESSFULLY = "Page checked successfully";
    public static void createCheck(Context ctx) throws SQLException {
        Long urlId = ctx.pathParamAsClass("id", Long.class).get();
        Url url = UrlsRepository.find(urlId)
                .orElseThrow(() -> new NotFoundResponse("Entity with id = " + urlId + " not found"));
        try {
            HttpResponse<String> response = Unirest.get(url.getName()).asString();
            Document doc = Jsoup.parse(response.getBody());

            var statusCode = response.getStatus();
            var title = doc.title();

            var h1Temp = doc.selectFirst("h1");
            var h1 = h1Temp == null ? "" : h1Temp.text();

            var descriptionTemp = doc.selectFirst("meta[name=description]");
            var description = descriptionTemp == null ? "" : descriptionTemp.attr("content");

            var urlCheck = UrlCheck.builder()
                    .statusCode(statusCode)
                    .title(title)
                    .h1(h1)
                    .urlId(urlId)
                    .description(description)
                    .build();

            UrlsCheckRepository.save(urlCheck);
            ctx.sessionAttribute("flash", SUCCESSFULLY);
            ctx.sessionAttribute("flash-type", "success");
        } catch (Exception e) {
            ctx.sessionAttribute("flash", INCORRECT_URL);
            ctx.sessionAttribute("flash-type", "danger");
        }
        ctx.redirect(NamedRoutes.urlPath(urlId));
    }
}
