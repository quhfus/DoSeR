__author__ = 'quh'

from flask import Flask, request, jsonify
from gensim.models.word2vec import Word2Vec
from gensim.models.doc2vec import Doc2Vec
from gensim import matutils
from numpy import dot
from math import pi, e, fabs
from time import *
import logging
from gunicorn.app.base import BaseApplication

logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)

class Word2VecRest(Flask):

    def __init__(self, *args, **kwargs):
        super(Word2VecRest, self).__init__(*args, **kwargs)

    def compute_w2vsimilarity(self, s1, s2):
        try:
            similarity = GunicornApplication.w2vmodel.similarity(s1, s2)
        except Exception:
            similarity = 0
        return similarity

    def compute_w2vsimilarity_multi(self, l1, str):
        l2 = list()
        l2.append(str)
	similarity = 0
        try :
            similarity = GunicornApplication.w2vmodel.n_similarity(l1, l2)
        except Exception:
            similarity = 0
        return similarity

    def infer_docvector(self, s1):
#	with GunicornApplication.d2vmodel.lock:
        vec = GunicornApplication.d2vmodel.infer_vector(s1, steps=300)
	return vec

    def compute_d2vsimilarity(self, d1, vec2):
        try:
            vec1 = GunicornApplication.d2vmodel.docvecs[d1]
	    # Check whether entity is in model
	    if(len(vec1) != GunicornApplication.d2vmodel.layer1_size) :
	    	similarity = 0
	    else : 
		similarity = dot(matutils.unitvec(vec1), matutils.unitvec(vec2))
	        if(similarity > 0) :
  	    	    similarity = pi*similarity
	        else :
		    similarity = -(pi*fabs(similarity))
        except Exception:
            similarity = 0
        return similarity

w2v = Word2VecRest(__name__)


@w2v.route('/w2vsim', methods = ['POST'])
def w2vsim():
    json = request.get_json(force=True)
    data = json['data']
    li = list()
    for q in data:
        split = q.split('|')
        if len(split) > 2 :
            l = list()
            for a in range(0, (len(split) - 1)):
                l.append(split[a])
            sim = w2v.compute_w2vsimilarity_multi(l, split[len(split) - 1])
        else :
            sim = w2v.compute_w2vsimilarity(split[0], split[1])
        result = {"ents":q, "sim":sim}
        li.append(result)
    return jsonify(data=li)


@w2v.route('/d2vsim', methods = ['POST'])
def infer():
    json = request.get_json(force=True)
    print json
    sfs = json['data']
    f = list()
    for sf in sfs:
        contextvec = w2v.infer_docvector(sf['context'].split())
        candidates = sf['candidates']
        cansim = list()
        for can in candidates:
            similarity = w2v.compute_d2vsimilarity(can, contextvec)
	    cansim.append(similarity)
        result = {"qryNr":sf['qryNr'], "surfaceForm":sf['surfaceForm'], "sim":cansim}
        f.append(result)
        print result
    return jsonify(data=f)



class GunicornApplication(BaseApplication):
    
    d2vmodel = Doc2Vec.load('')
    w2vmodel = Word2Vec.load_word2vec_format('', binary=True)    

    def __init__(self, wsgi_app, port=5000):
	self.options = {
            'bind': "127.0.0.1:{port}".format(port=port),
             'workers': 5,
             'preload_app': True,
        }
        self.application = wsgi_app
        super(GunicornApplication, self).__init__()

    def load_config(self):
        config = dict([(key, value) for key, value in self.options.iteritems()
                       if key in self.cfg.settings and value is not None])
        for key, value in config.iteritems():
            self.cfg.set(key.lower(), value)

    def load(self):
        return self.application


if __name__ == '__main__':
    gapp = GunicornApplication(w2v)
    gapp.run()
