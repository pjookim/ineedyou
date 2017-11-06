package kr.camelsoft.ineedyou;

import java.io.Serializable;

/**
 * Created by pjookim on 05/11/2017.
 */

// 포스트의 댓글
public class Comment implements Serializable {
    String writerId;
    String text;
    String bgUrl;
    long writeTime;

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
}