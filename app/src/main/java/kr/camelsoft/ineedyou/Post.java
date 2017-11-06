package kr.camelsoft.ineedyou;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pjookim on 05/11/2017.
 */

public class Post implements Serializable {
    String writerId;
    String text;
    String bgUrl;
    long writeTime;
    Map<String, Comment> commentMap = new HashMap<>();


    public String getWriterId() {
        return writerId;
    }

    public void setWriterId(String writerId) {
        this.writerId = writerId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getBgUrl() {
        return bgUrl;
    }

    public void setBgUrl(String bgUrl) {
        this.bgUrl = bgUrl;
    }

    public long getWriteTime() {
        return writeTime;
    }

    public void setWriteTime(long writeTime) {
        this.writeTime = writeTime;
    }

    public Map<String, Comment> getCommentMap() {
        return commentMap;
    }

    public void setCommentMap(Map<String, Comment> commentMap) {
        this.commentMap = commentMap;
    }
}