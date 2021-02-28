package ua.hazelcast.cluster.deployment.exception;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class ApplicationException extends AbstractThrowableProblem {

    public ApplicationException(String message) {
        super(ExceptionConstants.DEFAULT_TYPE, "Application Exception", Status.INTERNAL_SERVER_ERROR, message);
    }
}
