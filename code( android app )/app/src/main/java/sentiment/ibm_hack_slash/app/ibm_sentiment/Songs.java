package sentiment.ibm_hack_slash.app.ibm_sentiment;

public class Songs {
    String songName;
    String songId;

    public Songs(){

    }

    public Songs(String songName,String songId) {
        this.songName = songName;
        this.songId = songId;
    }

    public String getSongName() {
        return songName;
    }

    public String getSongId() {
        return songId;
    }
}
