package yea.deam;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdfparser.COSParser;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    private static String chapterRegex = ".*CAPÍTULO \\d.*";
    private static Path biblia = Path.of("./src/main/resources/Biblia.pdf");
    private static int firstPage = 1045;
    private static int otherPage = 1048;
    private static int lastPage = 1095;

    public static void main(String[] args) throws IOException {
        var yea = new PDFParser(RandomAccessReadBuffer.createBufferFromStream(Files.newInputStream(biblia)));
        PDDocument document = yea.parse();
        System.out.println(document.getNumberOfPages());
        PDFTextStripper pdfStripper = new PDFTextStripper();
        pdfStripper.setStartPage(firstPage);
        pdfStripper.setEndPage(lastPage);
        String text = pdfStripper.getText(document);

        var chapters = getChapters(text);
        chapters.forEach(System.out::println);
        Files.createDirectory(Path.of("./pure/"));
        chapters.forEach(chapter -> {
            Path path = Paths.get("./pure/", chapter.chapter() + ".txt");
            try (var br = Files.newBufferedWriter(path)) {
                br.write(chapter.chapter());
                br.newLine();
                br.write(chapter.description());
                br.newLine();
                br.write(String.join("\n", chapter.proverbs()));
            } catch (IOException e) {
                throw new RuntimeException("Fokiu", e);
            }
        });
    }

    public static List<Chapter> getChapters(String text) {
        List<String> list = text.lines().toList();
        List<Chapter> chapters = new ArrayList<>();
        List<Integer> chaptersIndexes = list.stream().filter(line -> line.matches(chapterRegex))
                .map(list::indexOf).toList();

        chaptersIndexes.stream().map(i -> list.subList(i, list.size()))
                .map(Main::createChapter)
                .forEach(chapters::add);

        return chapters;
    }

    private static List<String> getProverbs(List<String> list) {
        List<String> proverbs = new ArrayList<>();
        String proverb = "";
        for (String line : list.stream().dropWhile(s -> !s.matches("^\\d.+")).toList()) {
            if (Character.isWhitespace(line.charAt(0)) || line.contains("PROVERBIOS")) {
                continue;
            } else if (line.matches(chapterRegex)) {
                if (proverb.length() > 0) {
                    break;
                }
            } else {
                if (line.matches("^\\d.+") && !proverb.isBlank()) {
                    proverbs.add(proverb);
                    proverb = "";
                }
                proverb += line.matches("[y;?]") ? " " + line : line;
            }
        }
        return proverbs.stream().filter(s -> !s.matches("^\\d+\\s+\\d+.*"))
                .map(s -> s.replaceAll("\\..+", "."))
                .map(s -> s.replaceAll("\u00AD", ""))
                .map(s -> s.replaceAll("\\?¿", "? ¿"))
                .map(s -> s.replaceAll("(?>,)(\\S)", ", $1"))
                .map(s -> s.replaceAll("(?>\\?)(\\w)", "? $1"))
                .map(s -> s.replaceAll("(?>;)(\\S)", "; $1"))
                .map(s -> s.replaceAll("\\s+", " "))
                .toList();
    }

    private static Chapter createChapter(List<String> list) {
        return new Chapter(list.get(0)
                , list.stream().skip(1).takeWhile(s -> !s.matches("^\\d.+")).collect(Collectors.joining(" "))
                .replaceAll("[-\u00AD]\\s*", "")
                , getProverbs(list));
    }
}