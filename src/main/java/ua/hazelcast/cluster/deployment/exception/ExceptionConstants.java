package ua.hazelcast.cluster.deployment.exception;

import java.net.URI;

public final class ExceptionConstants {

    private static final String PROBLEM_BASE_URL = "https://hazelcast.com/problem";
    
    public static final URI DEFAULT_TYPE = URI.create(PROBLEM_BASE_URL + "/generic-problem");

    private ExceptionConstants() { }
}
