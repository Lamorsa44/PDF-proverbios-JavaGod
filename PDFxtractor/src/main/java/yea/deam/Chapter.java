package yea.deam;

import java.util.List;
import java.util.stream.Collectors;

public record Chapter(String chapter, List<String> proverbs) {
    @Override
    public String toString() {
        String proverbsString = String.join("\n", proverbs);
        return String.format("Capitulo: %s\nProverbios:\n%s", chapter, proverbsString);
    }
}
