package hexlet.code.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import hexlet.code.model.Url;

import java.util.List;

@Getter
@AllArgsConstructor
public class UrlsPage extends BasePage {
    private List<Url> urls;
}