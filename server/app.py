#!flask/bin/python
from flask import Flask, jsonify, g, request
import sqlite3

app = Flask(__name__)

def get_db():
	db = getattr(g, '_database', None)
	if db is None:
		db = g._database = sqlite3.connect('./Govern.db')
	db.row_factory = make_dicts
	return db

def close_connection(exception):
	db = getattr(g, '_database', None)
	if db is not None:
		db.close()

def make_dicts(cursor, row):
	return dict((cursor.description[idx][0], value) for idx, value in enumerate(row))

def query_db(db, query, args=(), one=False):
	cur = db.execute(query, args)
	rv = cur.fetchall()
	cur.close()
	return (rv[0] if rv else None) if one else rv

def insert(db, table, fields=(), values=()):
    cur = db.cursor()
    query = 'INSERT INTO %s (%s) VALUES (%s)' % (
        table,
        ', '.join(fields),
        ', '.join(['?'] * len(values))
    )
    cur.execute(query, values)
    db.commit()
    cur.close()


@app.route('/login', methods=['GET'])
def login():
	db = get_db()
	google_id = request.args.get('google_id', '')
	user = query_db(db, 'select * from Users where GoogleId=' + google_id, (), True)
	return jsonify(user)

if __name__ == '__main__':
    app.run(host='0.0.0.0')