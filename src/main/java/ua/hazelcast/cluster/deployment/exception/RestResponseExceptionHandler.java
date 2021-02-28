package ua.hazelcast.cluster.deployment.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.ProblemHandling;

import javax.naming.AuthenticationException;


/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 * The error response follows RFC7807 - Problem Details for HTTP APIs (https://tools.ietf.org/html/rfc7807)
 */
@ControllerAdvice
public class RestResponseExceptionHandler implements ProblemHandling {

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<Problem> handleAuthentication(final InsufficientAuthenticationException e,
                                                        final NativeWebRequest request) {

        Problem problem = Problem.builder()
                .withType(Problem.DEFAULT_TYPE)
                .withTitle("Authentication error")
                .withStatus(Status.UNAUTHORIZED)
                .withDetail(e.getMessage())
                .build();

        return create(e, problem, request);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleGeneralExceptions(final Exception e, final NativeWebRequest request) {

        Problem problem = Problem.builder()
                .withType(Problem.DEFAULT_TYPE)
                .withTitle(Status.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .withStatus(Status.INTERNAL_SERVER_ERROR)
                .withDetail("Something went wrong")
                .build();

        return create(e, problem, request);
    }
}
