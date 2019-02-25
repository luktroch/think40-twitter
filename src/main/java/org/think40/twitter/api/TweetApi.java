package org.think40.twitter.api;

import java.io.IOException;

//TODO: exceptions
public interface TweetApi {

    public Tweet createDocument(Tweet doc) throws IOException;

    //TODO: sorting
    public TweetListResult searchInMessage(String tag, Integer offset, Integer limit) throws IOException;
}
