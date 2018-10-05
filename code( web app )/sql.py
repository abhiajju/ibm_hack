from flask import Flask
from flask_sqlalchemy import SQLAlchemy

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///students.sqlite3'
app.config['SECRET_KEY'] = "random string"

db = SQLAlchemy(app)


class songs(db.Model):
   __tablename__ = 'songs'
   id = db.Column('songs_id', db.Integer, primary_key = True)
   sad = db.Column(db.String(200))
   happy = db.Column(db.String(200))
   tentative = db.Column(db.String(200)) 
   


   def __init__(self, sad, happy, tentative):
      self.sad = sad
      self.happy = happy
      self.tentative = tentative

'''class stud(db.Model):
   __tablename__ = 'stud'
   id = db.Column('student_id', db.Integer, primary_key = True)
   name = db.Column(db.String(100))
   city = db.Column(db.String(50))

   def __init__(self, name, city):
      self.name = name
      self.city = city


class studs(db.Model):
   __tablename__ = 'studs'
   id = db.Column('student_id', db.Integer, primary_key = True)
   name = db.Column(db.String(100))
   city = db.Column(db.String(50))
   addr = db.Column(db.String(200))
   pin = db.Column(db.String(10))

   def __init__(self, name, city, addr,pin):
      self.name = name
      self.city = city
      self.addr = addr
      self.pin = pin'''

def fnc():
   db.create_all()

fnc()