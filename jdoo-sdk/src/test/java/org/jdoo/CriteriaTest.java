package org.jdoo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

public class CriteriaTest {
    @Test
    void serialize() throws JsonProcessingException {
        Criteria c = Criteria.equal("field", "value").or(Criteria.like("f1", "v1"), Criteria.greater("f2", "v2"));
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(c);

        assertEquals("[\"|\",\"|\",{\"field\":\"field\",\"op\":\"=\",\"value\":\"value\"},{\"field\":\"f1\",\"op\":\"like\",\"value\":\"v1\"},{\"field\":\"f2\",\"op\":\">\",\"value\":\"v2\"}]",
         json);

        c = mapper.readValue(json, Criteria.class);

        c = mapper.readValue(
                "[\"&\", [\"field\", \"=\", \"value\"], \"|\", [\"f1\",\"!=\",\"v1\"],{\"field\":\"f2\",\"op\":\"=\",\"value\":\"v2\"}]",
                Criteria.class);

        c = mapper.readValue("{\"field\":\"f2\",\"op\":\"=\",\"value\":\"v2\"}",
                Criteria.class);
    }
}
