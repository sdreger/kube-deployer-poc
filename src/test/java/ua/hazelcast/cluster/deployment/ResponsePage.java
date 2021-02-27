package ua.hazelcast.cluster.deployment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponsePage<T> extends PageImpl<T> {

    private static final long serialVersionUID = 1L;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public ResponsePage(final @JsonProperty("content") List<T> content,
                        final @JsonProperty("number") int number,
                        final @JsonProperty("size") int size,
                        final @JsonProperty("totalElements") Long totalElements,
                        final @JsonProperty("pageable") JsonNode pageable,
                        final @JsonProperty("last") boolean last,
                        final @JsonProperty("totalPages") int totalPages,
                        final @JsonProperty("sort") JsonNode sort,
                        final @JsonProperty("first") boolean first,
                        final @JsonProperty("numberOfElements") int numberOfElements) {
        super(content, PageRequest.of(number, size), totalElements);
    }

    public ResponsePage(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public ResponsePage(List<T> content) {
        super(content);
    }

    public ResponsePage() {
        super(new ArrayList<T>());
    }

}