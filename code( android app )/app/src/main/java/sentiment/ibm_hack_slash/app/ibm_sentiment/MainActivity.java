package sentiment.ibm_hack_slash.app.ibm_sentiment;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.List;
import java.util.Vector;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;


public class  MainActivity extends AppCompatActivity {
    TextView tv;
    public String tweets="";
    DatabaseReference songDatabase;
    EditText handle;
    Button submit;
    TextView name;
    String username;
    TextView dealing;
    String personname;
    ProgressBar pb;
    int songcount=0;
    ListView playlist;
    Vector<String> v;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_main);
        //tv = (TextView)findViewById(R.id.twitterview);
        playlist = (ListView)findViewById(R.id.listplaylist);
        handle = (EditText)findViewById(R.id.inputhandle);
        submit = (Button)findViewById(R.id.clickme);
        name = (TextView)findViewById(R.id.name);
        dealing = (TextView)findViewById(R.id.textView2);
        pb = (ProgressBar)findViewById(R.id.progressBar);
        findViewById(R.id.progressBar).setVisibility(View.GONE);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(handle.getText().length()!=0) {
                    //tv.setText("Loading");
                    name.setText("");
                    dealing.setText("");
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    username = handle.getText().toString();
                    Retrivetweets rt = new Retrivetweets();
                    rt.execute("");
                    GetSentiment gs = new GetSentiment();
                    gs.execute(tweets);
                }
            }
        });

    }
    public class Retrivetweets extends AsyncTask<String, Void, List<Status>> {

        @Override
        protected List<twitter4j.Status> doInBackground(String... strings) {
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthConsumerKey("1S7vQgNdGf45V9fMsxSec9VIt")
                    .setOAuthConsumerSecret("TUq7LaNCiDKyxT3SG8XwlK8yVUsqRgUcsdZyXKu3UH7YFegw5i")
                    .setOAuthAccessToken("832861425127264256-kNarHNi1CbEGOl0erBnRMyD6JCo9hAS")
                    .setOAuthAccessTokenSecret("IgR1kWZ7DAip5Ng15E6uccxjdqWzpXSSoEHLhGkJ7Rm6v")
                    .setTweetModeExtended(true);
            TwitterFactory tf = new TwitterFactory(cb.build());
            Twitter twitter = tf.getInstance();
            List<twitter4j.Status> statuses= null;
            Log.d("hey","hello");
            try {
                // gets Twitter instance with default credentials
                //User user = twitter.verifyCredentials();
                statuses = twitter.getUserTimeline(username);
                User user = twitter.showUser(username);
                personname = user.getName();
                //Log.d("Twittername",personname);
                //Toast.makeText(MainActivity.this,personname,Toast.LENGTH_LONG).show();
                //System.out.println("Showing @" + user.getScreenName() + "'s home timeline.");
                for (twitter4j.Status status : statuses) {
                    //Log.d("heya","@" + status.getUser().getScreenName() + " - " + status.getText());
                //    System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
                }
            } catch (TwitterException te) {
                //te.printStackTrace();
                //System.out.println("Failed to get timeline: " + te.getMessage());
                //System.exit(-1);

            }
            return statuses;
        }
        @Override
        protected void onPostExecute(List<twitter4j.Status> st){
            String res="";
            for (twitter4j.Status status : st) {
                if(status.isRetweet())
                    res=res+(status.getRetweetedStatus().getText())+"\n";

                else
                    res=res+status.getText()+"\n";
            }
            tweets = res;
        }

    }
    public class GetSentiment extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            IamOptions options = new IamOptions.Builder()
                    .apiKey("wmixbbAZzlgRcii9fUWunaW07luuCLr_HAmDPe4dsMjv")
                    .build();
            ToneAnalyzer toneAnalyzer = new ToneAnalyzer("2018-09-26", options);
            toneAnalyzer.setEndPoint("https://gateway-wdc.watsonplatform.net/tone-analyzer/api");
            String text = tweets;
            ToneOptions toneOptions = new ToneOptions.Builder()
                    .text(text)
                    .build();
            ToneAnalysis toneAnalysis = toneAnalyzer.tone(toneOptions).execute();
            //Log.d("hey",toneAnalysis.toString());
            return toneAnalysis.toString();
        }
        @Override
        protected void onPostExecute(String s){
            String response="";
            try {
                JSONObject root = new JSONObject(s);
                Log.d("hey",s);
                JSONObject documenttone = root.getJSONObject("document_tone");
                JSONArray tones = documenttone.getJSONArray("tones");
                double maxscore=0;
                for(int i=0;i<tones.length();i++){
                    JSONObject contest = tones.getJSONObject(i);
                    double score = contest.getDouble("score");
                    if(score>maxscore)
                        response = contest.getString("tone_name");
                  /*  Log.d("hey",score+tone);
                    response+=(score+":"+tone)+"\n";*/

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            /*songDatabase = FirebaseDatabase.getInstance().getReference("Tentative");
            String id = songDatabase.push().getKey();
            Songs song = new Songs("“Default”—Django Django","4");
            songDatabase.child(id).setValue(song);
            Log.d("tone",response);*/
            if(response.equals("Sadness"))
                response="Sad";
            Toast.makeText(MainActivity.this,"Your mood is : "+response,Toast.LENGTH_LONG).show();
            songDatabase = FirebaseDatabase.getInstance().getReference(response);
            songcount=0;
            songDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    v = new Vector<>();
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Songs song = snapshot.getValue(Songs.class);
                            songcount++;
                            v.add(song.songName);
                        }
                    }
                    //tv.setText(res);
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    name.setText(" Hello, "+personname);
                    dealing.setText("Some songs for you...");
                    CustomAdapter customAdapter = new CustomAdapter();
                    playlist.setAdapter(customAdapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            Log.d("database","check");
        }

    }
    public class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return songcount;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.customlayout,null);
            TextView songname = (TextView)view.findViewById(R.id.songname);
            songname.setText(v.get(i));
            return view;
        }
    }




}