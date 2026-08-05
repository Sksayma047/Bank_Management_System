package com.bankmanagement.controller;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class BaseController extends HttpServlet {
    
    // Configured Gson instance for handling LocalDate and LocalDateTime
    protected static final Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();

    protected void writeJson(HttpServletResponse resp, int status, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(status);
        resp.getWriter().write(gson.toJson(data));
    }

    protected void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(status);
        resp.getWriter().write(gson.toJson(new ErrorResponse(message)));
    }

    protected <T> T readJson(HttpServletRequest req, Class<T> clazz) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return gson.fromJson(sb.toString(), clazz);
    }

    /**
     * Extracts an ID from the end of the URL path (e.g. /api/customers/123 -> 123)
     */
    protected int getIdFromPath(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            return -1;
        }
        try {
            // Remove leading slash
            return Integer.parseInt(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static class ErrorResponse {
        private final boolean success = false;
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSuccess() {
            return success;
        }
    }

    // GSON adapters for Java 8 Date APIs
    private static class LocalDateAdapter extends TypeAdapter<LocalDate> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.format(formatter));
            }
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
            String val = in.nextString();
            return (val == null || val.trim().isEmpty()) ? null : LocalDate.parse(val, formatter);
        }
    }

    private static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.format(formatter));
            }
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            String val = in.nextString();
            return (val == null || val.trim().isEmpty()) ? null : LocalDateTime.parse(val, formatter);
        }
    }
}
