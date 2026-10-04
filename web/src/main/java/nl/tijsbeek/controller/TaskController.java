package nl.tijsbeek.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/test")
public class TaskController {


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(String resource) {
        Objects.requireNonNull(resource);

        return 0L;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public String receive() {
        return "5";
    }
}
