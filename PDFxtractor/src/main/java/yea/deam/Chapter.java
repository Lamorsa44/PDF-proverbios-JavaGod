package yea.deam;

import java.util.List;
import java.util.stream.Collectors;

public record Chapter(String chapter, String description, List<String> proverbs) {
    @Override
    public String toString() {
        String proverbsString = String.join("\n", proverbs);
        return String.format("""
                Capitulo: %s
                Description:
                %s
                Proverbs:
                %s
                """, chapter, description ,proverbsString);
    }
}
