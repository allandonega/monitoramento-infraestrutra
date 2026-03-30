package com.monitora.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.PrintWriter;
import java.io.StringWriter;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        
        String html = "<html><body style='font-family: monospace; background: #000; color: #fff; padding: 20px;'>"
                + "<h2>Erro Crítico - Stacktrace</h2>"
                + "<pre style='white-space: pre-wrap; word-wrap: break-word;'>"
                + sw.toString()
                + "</pre></body></html>";
                
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(html);
    }
}
