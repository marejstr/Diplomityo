package fi.marejstr.movementtraining.gson;

import com.google.gson.annotations.SerializedName;

public class TimeResponse {

    @SerializedName("Content")
    private final TimeResponse.Content content;

    public TimeResponse(TimeResponse.Content content) {
        this.content = content;
    }

    public TimeResponse.Content getContent() {
        return content;
    }

    public class Content {

        @SerializedName("utcTime")
        private long utctime;

        @SerializedName("relativeTime")
        private int relativetime;

        @SerializedName("tickRate")
        private int tickrate;

        @SerializedName("accuracy")
        private int accuracy;

        public long getUtctime() {
            return utctime;
        }

        public int getRelativetime() {
            return relativetime;
        }

        public int getTickrate() {
            return tickrate;
        }

        public int getAccuracy() {
            return accuracy;
        }

    }
}


/*
{
"Content":
    {
            "utcTime": 1420070484611000,
            "relativeTime": 84611,
            "tickRate": 1024,
            "accuracy": 20
     }
}
 */
