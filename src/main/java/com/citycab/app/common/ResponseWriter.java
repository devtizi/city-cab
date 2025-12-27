package com.citycab.app.common;


import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class ResponseWriter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void write(HttpServletResponse response,
        int statusCode,
        String message,
        Object data
    ) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");

        Map<String, Object> body = new HashMap<>();
        body.put("message", message);
        body.put("statusCode", statusCode);
        body.put("data", data);
        body.put("time", Instant.now().toString());

        String json = objectMapper.writeValueAsString(body);
        response.getWriter().write(json);
        response.getWriter().flush();
    }
}