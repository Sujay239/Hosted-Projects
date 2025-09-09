package com.chatapp.exception;

//import org.springframework.web.bind.annotation.ControllerAdvice
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleAllException(Exception e){
        log.error(e.getMessage());
        return e.getMessage();
    }
}
