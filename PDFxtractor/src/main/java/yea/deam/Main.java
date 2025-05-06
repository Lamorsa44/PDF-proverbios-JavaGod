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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static String chapterRegex = ".*CAPÍTULO \\d.*";
    private static Path biblia = Path.of("C:\\Users\\lamorsa\\Downloads\\Biblia.pdf");
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
    }

    public static List<Chapter> getChapters(String text) {
        List<String> list = text.lines().toList();
        List<Chapter> chapters = new ArrayList<>();
        List<Integer> chaptersIndexes = list.stream().filter(line -> line.matches(chapterRegex))
                .map(list::indexOf).peek(System.out::println).toList();
        chaptersIndexes.stream().map(i -> list.subList(i, list.size()))
                .map(list1 -> new Chapter(list1.get(0), getProverbs(list1)))
                .peek(System.out::println).forEach(chapters::add);

        return chapters;
    }

    private static List<String> getProverbs(List<String> list) {
        List<String> proverbs = new ArrayList<>();
        String proverb = "";
        for (String line : list) {
            if (Character.isWhitespace(line.charAt(0)) || line.contains("PROVERBIOS")) {
                continue;
            } else if (line.matches(chapterRegex)) {
                if (proverb.length() > 0) {
                    break;
                }
            } else {
                if (line.matches("^\\d.+")) {
                    proverbs.add(proverb);
                    proverb = "";
                }
                proverb += line;
            }
        }
        return proverbs;
    }
}