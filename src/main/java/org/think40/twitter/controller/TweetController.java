package org.think40.twitter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.think40.twitter.api.Tweet;
import org.think40.twitter.api.TweetApi;
import org.think40.twitter.api.TweetListResult;

import java.io.IOException;
import java.util.Date;

@RestController
public class TweetController {

    private TweetApi service;

    @Autowired
    public TweetController(TweetApi service) {
        this.service = service;
    }

    @ExceptionHandler({ IOException.class })
    public ResponseEntity<IOException> handleException(IOException e) {
        return new ResponseEntity<>(e,HttpStatus.SERVICE_UNAVAILABLE);
    }


    @PostMapping(value = "/api/v1/tweets", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Tweet> createTweet(@RequestBody Tweet tweet) throws IOException {
        tweet.setId(null);
        if(tweet.getCreationDate() == null) tweet.setCreationDate(new Date());
        return new ResponseEntity<>(service.createDocument(tweet), HttpStatus.CREATED);
     }

    @GetMapping(value = "/api/v1/tweets", produces = "application/json")
    public ResponseEntity<TweetListResult> findByTag(@RequestParam(required = false) String tag, @RequestParam(required = false) Integer offset, @RequestParam(required = false) Integer limit) throws IOException {
        if (tag != null && !tag.startsWith("#")) tag = "#" + tag;
        if (limit == null) limit = 10;
        if (offset == null) offset = 0;
        return new ResponseEntity<>(service.searchInMessage(tag, offset, limit), HttpStatus.OK);
    }
}
