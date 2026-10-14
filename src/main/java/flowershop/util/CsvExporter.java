package flowershop.util;

import flowershop.exception.BusinessException;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CsvExporter {

    private static final char SEPARATOR = ';';   // так Excel с русской локалью разбивает на столбцы
    private static final String NEW_LINE = "\r\n";
    private static final char BOM = '\uFEFF';    // чтобы Excel правильно прочитал кириллицу в UTF-8

    private CsvExporter() {
        // утилитный класс
    }

    public static void export(Path file, List<String> headers, List<List<String>> rows) {
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write(BOM);
            writeLine(writer, headers);
            for (List<String> row : rows) {
                writeLine(writer, row);
            }
        } catch (IOException e) {
            throw new BusinessException("Не удалось сохранить файл: " + e.getMessage(), e);
        }
    }

    private static void writeLine(Writer writer, List<String> cells) throws IOException {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                line.append(SEPARATOR);
            }
            line.append(escape(cells.get(i)));
        }
        line.append(NEW_LINE);
        writer.write(line.toString());
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        // Защита от выполнения формул при открытии в Excel
        if (!value.isEmpty() && "=+@".indexOf(value.charAt(0)) >= 0) {
            value = "'" + value;
        }
        boolean needsQuotes = value.indexOf(SEPARATOR) >= 0
                || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0
                || value.indexOf('\r') >= 0;
        if (needsQuotes) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }
}