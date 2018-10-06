from __future__ import print_function
from flask import Flask, redirect, url_for,request, render_template
from flask_admin import Admin
from flask_admin.contrib.sqla import ModelView
import tweepy,json,sys,spotipy
from watson_developer_cloud import ToneAnalyzerV3
from watson_developer_cloud import WatsonApiException
from spotipy.oauth2 import SpotifyClientCredentials
import spotipy.util as util
from flask_sqlalchemy import SQLAlchemy
from sql import db
from sql import songs


app = Flask(__name__,static_url_path='')
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///students.sqlite3'
app.config['SECRET_KEY'] = "random string"

db.init_app(app)

def main():
   app.run(port=5000, debug=True)


def twitter_auth():
    consumer_key = "your_consumer_key"
    consumer_secret = "your_consumer_secret"
    access_token = "your_access_token"
    access_token_secret =  "your_access_token_secret"
    auth = tweepy.OAuthHandler(consumer_key, consumer_secret)
    auth.set_access_token(access_token, access_token_secret)
    return auth
    
def spotify_auth():
    cid ='your_consumer_id' 
    secret = 'your_consumer_secret' 
    #username = 'x0c6166fhofk75xtxqgwybr6h' 
    client_credentials_manager = SpotifyClientCredentials(client_id=cid, client_secret=secret) 
    return client_credentials_manager

client_credentials_manager = spotify_auth()
sp = spotipy.Spotify(client_credentials_manager=client_credentials_manager)

auth = twitter_auth()
api = tweepy.API(auth)

nmbr = 5
tweet_sentiment = []
sng_lst = []
hlst = []

tone_analyzer = ToneAnalyzerV3(
    version='your_version',
    username='your_watson_tone_analyzer_username',
    password='your_password',
    url='https://gateway.watsonplatform.net/tone-analyzer/api'
)

def tone_analyze(text):
    try:
        tone_analysis = tone_analyzer.tone(
            {'text': text},
            'application/json'
        ).get_result()
        tone = tone_analysis["document_tone"]["tones"]
        return tone    
    except WatsonApiException as ex:
        msg = "Method failed with status code " + str(ex.code) + ": " + ex.message
        return msg

def fetch_tweet(user_handle):
	return api.user_timeline(screen_name = user_handle, count = nmbr) 



def analyzer(tweet):
    tone = tone_analyze(tweet)
    lst = []
    if(len(tone)):
        for tones in tone:
            toned = json.dumps(tones["tone_name"])
            score = json.dumps(tones["score"])
            t_score = toned + "  " + score
            tweet_sentiment.append(t_score)
            if(toned.strip('"') == "Joy" or toned.strip('"') == "Confident"):
                lst.append('pop')
                hlst.append(songs.happy)
            else:
                lst.append('sad')
                hlst.append(songs.tentative)
    return lst

def spotify_srch(lst):
    abd = sp.recommendations(seed_genres=lst,limit=8, country='US',min_popularity=50)
    if(len(abd['tracks'])):
        for sngs in abd['tracks']:
            sng_lst.append(sngs['album']['name'])


@app.route('/')
def index():
   return render_template('index.html')


@app.route('/login',methods = ['POST', 'GET'])
def login():
    del tweet_sentiment [:]
    del hlst [:]
    del sng_lst [:]
    if request.method == 'POST':
        user = request.form['handle']
        hindi = {}
        usernm = api.get_user(user).name
        public_tweets = fetch_tweet(user)
        if(len(public_tweets)>0):
            strn = " "
            for tweet in public_tweets:
                strn = strn + tweet.text + "\n"
            tweet_sentiment.append(strn)
            lst = analyzer(strn)
            if(len(lst)):
                spotify_srch(lst)
            if(len(hlst)):
                hindi = songs.query.with_entities(*hlst)
        return render_template('sentiment.html',result = tweet_sentiment, name = usernm, songs = sng_lst, hsongs = hindi)

if __name__ == '__main__':
    main()