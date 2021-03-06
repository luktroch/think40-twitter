package org.think40.twitter;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = EmbeddedElasticInit.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
public class MockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void createObject() throws Exception {
        this.mockMvc.perform(post("/api/v1/tweets").content("{}").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.creationDate", notNullValue()));

        this.mockMvc.perform(post("/api/v1/tweets").content("{\"id\":\"123\",\"creationDate\":\"2019-02-24T15:56:49.094Z\",\"userName\":\"Johnny\",\"message\":\"message sent\"}")
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(is("123"))))
                .andExpect(jsonPath("$.creationDate", is("2019-02-24T15:56:49.094Z")))
                .andExpect(jsonPath("$.userName", is("Johnny")))
                .andExpect(jsonPath("$.message", is("message sent")))
                .andDo(document("create-tweet", requestFields(
                        fieldWithPath("id").type(JsonFieldType.STRING).description("Autogenerated identifier.").ignored(),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("Message with tags").optional(),
                        fieldWithPath("userName").type(JsonFieldType.STRING).description("User who created Tweet").optional(),
                        fieldWithPath("creationDate").type(JsonFieldType.STRING).description("Date when Tweet was created in format: yyyy-MM-dd'T'HH:mm:ss.SSS'Z'. When not provided will be generated").optional()
                        ),
                        responseFields(
                                fieldWithPath("id").type(JsonFieldType.STRING).description("Autogenerated identifier.").optional(),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("Message with tags").optional(),
                                fieldWithPath("userName").type(JsonFieldType.STRING).description("User who created Tweet").optional(),
                                fieldWithPath("creationDate").type(JsonFieldType.STRING).description("Date when Tweet was created in format: yyyy-MM-dd'T'HH:mm:ss.SSS'Z'. When not provided will be generated").optional()
                        )
                ));
    }

    @Test
    public void searchByTag() throws Exception {
        this.mockMvc.perform(post("/api/v1/tweets").content("{\"message\":\"message with #tag\"}").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        Thread.sleep(2000);

        this.mockMvc.perform(get("/api/v1/tweets?tag=tag"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tweets", hasSize(1)))
                .andExpect(jsonPath("$.tweets[0].message", is("message with #tag")))
                .andDo(document("search-tweet", requestParameters(
                        parameterWithName("tag").description("Tag to search for. Without #. If ommited all messages will be returned.").optional()),
                        responseFields(
                                subsectionWithPath("tweets").description("array of tweets found"),
                                fieldWithPath("tweets[].id").type(JsonFieldType.STRING).description("Autogenerated identifier.").optional(),
                                fieldWithPath("tweets[].message").type(JsonFieldType.STRING).description("Message with tags").optional(),
                                fieldWithPath("tweets[].userName").type(JsonFieldType.STRING).description("User who created Tweet").optional(),
                                fieldWithPath("tweets[].creationDate").type(JsonFieldType.STRING).description("Date when Tweet was created in format: yyyy-MM-dd'T'HH:mm:ss.SSS'Z'. When not provided will be generated").optional(),
                                subsectionWithPath("paging").description("paging details section"),
                                fieldWithPath("paging.limit").description("Number of tweets returned"),
                                fieldWithPath("paging.offset").description("Results starting index"),
                                fieldWithPath("paging.totalCount").description("Number of all Tweets found")
                        )
                ));

        this.mockMvc.perform(get("/api/v1/tweets?tag=tag"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tweets", hasSize(1)))
                .andExpect(jsonPath("$.tweets[0].message", is("message with #tag")));

        this.mockMvc.perform(post("/api/v1/tweets").content("{\"message\":\"more #tag #with\"}").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        Thread.sleep(2000);
        this.mockMvc.perform(get("/api/v1/tweets?tag=with"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tweets", hasSize(1)))
                .andExpect(jsonPath("$.tweets[0].message", is("more #tag #with")));

        this.mockMvc.perform(post("/api/v1/tweets").content("{\"message\":\"more #with #tag #with\"}").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        Thread.sleep(2000);
        this.mockMvc.perform(get("/api/v1/tweets?tag=with"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tweets", hasSize(2)));

        this.mockMvc.perform(get("/api/v1/tweets?tag=more"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tweets", hasSize(0)));

        this.mockMvc.perform(get("/api/v1/tweets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tweets", hasSize(3)));
    }

    @Test
    public void testPaging() throws Exception {
        for (int i = 0; i < 20; i++) {
            this.mockMvc.perform(post("/api/v1/tweets").content("{\"message\":\"#sample message\"}").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());
        }
        Thread.sleep(3000);
        this.mockMvc.perform(get("/api/v1/tweets?tag=sample"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paging.limit", is(10)))
                .andExpect(jsonPath("$.tweets", hasSize(10)))
                .andExpect(jsonPath("$.paging.totalCount", is(20)));

        this.mockMvc.perform(get("/api/v1/tweets?tag=sample&limit=2&offset=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tweets", hasSize(2)))
                .andExpect(jsonPath("$.paging.limit", is(2)))
                .andExpect(jsonPath("$.paging.offset", is(10)))
                .andExpect(jsonPath("$.paging.totalCount", is(20)))
                .andDo(document("search-tweet-paging", requestParameters(
                        parameterWithName("tag").description("Tag to search for. Without #. If ommited all messages will be returned.").optional(),
                        parameterWithName("offset").description("Results starting index. Default 0").optional(),
                        parameterWithName("limit").description("Number of tweets to be returned. Default 10").optional()),
                        responseFields(
                                subsectionWithPath("paging").description("paging details section"),
                                fieldWithPath("paging.limit").description("Number of tweets returned"),
                                fieldWithPath("paging.offset").description("Results starting index"),
                                fieldWithPath("paging.totalCount").description("Number of all Tweets found"),
                                subsectionWithPath("tweets").description("array of tweets found"),
                                fieldWithPath("tweets[].id").type(JsonFieldType.STRING).description("Autogenerated identifier.").optional(),
                                fieldWithPath("tweets[].message").type(JsonFieldType.STRING).description("Message with tags").optional(),
                                fieldWithPath("tweets[].userName").type(JsonFieldType.STRING).description("User who created Tweet").optional(),
                                fieldWithPath("tweets[].creationDate").type(JsonFieldType.STRING).description("Date when Tweet was created in format: yyyy-MM-dd'T'HH:mm:ss.SSS'Z'. When not provided will be generated").optional()
                                )
                ));

        this.mockMvc.perform(get("/api/v1/tweets?tag=sample&offset=30"))
                .andExpect(jsonPath("$.tweets", hasSize(0)));
    }
}
