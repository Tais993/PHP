package nl.tijsbeek.errors;

import nl.tijsbeek.exceptions.BoardNotFoundException;
import nl.tijsbeek.exceptions.BoardViewNotFoundException;
import nl.tijsbeek.exceptions.InvalidFilterRulesException;
import nl.tijsbeek.exceptions.InvalidTaskReferenceException;
import nl.tijsbeek.exceptions.TaskNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({
            TaskNotFoundException.class,
            BoardNotFoundException.class,
            BoardViewNotFoundException.class
    })
    public ProblemDetail handleNotFound(RuntimeException exception) {
        return createProblem(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({InvalidTaskReferenceException.class, InvalidFilterRulesException.class})
    public ProblemDetail handleBadRequest(RuntimeException exception) {
        return createProblem(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    private ProblemDetail createProblem(HttpStatus status, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("about:blank"));
        return problem;
    }
}
