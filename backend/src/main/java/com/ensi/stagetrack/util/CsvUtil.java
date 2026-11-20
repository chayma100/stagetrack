package com.ensi.stagetrack.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.List;

/** Génération de CSV lisible par Excel (séparateur point-virgule, UTF-8 avec BOM). */
public final class CsvUtil {

    private CsvUtil() {
    }

    public static byte[] toCsv(String[] headers, List<String[]> rows) {
        StringBuilder sb = new StringBuilder("﻿");
        appendLine(sb, headers);
        for (String[] row : rows) {
            appendLine(sb, row);
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static ResponseEntity<byte[]> response(String filename, byte[] body) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    private static void appendLine(StringBuilder sb, String[] cells) {
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) {
                sb.append(';');
            }
            sb.append(escape(cells[i]));
        }
        sb.append("\r\n");
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        // Neutralise les formules Excel (injection CSV) puis protège les séparateurs
        String v = value;
        if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) {
            v = "'" + v;
        }
        if (v.contains(";") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}
