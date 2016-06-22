__author__ = 'quh'

from flask import Flask, request, jsonify
from gensim.models.word2vec import Word2Vec
from gensim.models.doc2vec import Doc2Vec
from gensim import matutils
from numpy import dot
from math import pi, e, fabs
from time import *
import logging, codecs
from ConfigParser import SafeConfigParser
import os.path
from gunicorn.app.base import BaseApplication

import traceback

logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)
logger = logging.getLogger(__name__)

class Word2VecRest(Flask):

    def __init__(self, *args, **kwargs):
        super(Word2VecRest, self).__init__(*args, **kwargs)

    def compute_w2vsimilarity(self, s1, s2, domain):
        similarity = 0
	if(domain == 'DBpedia'):
            try:
                similarity = GunicornApplication.w2vmodel_dbpedia.similarity(s1, s2)
            except Exception:
                similarity = 0
	elif(domain == 'Biomed'):
	    try:
                similarity = GunicornApplication.w2vmodel_biomed.similarity(s1, s2)
            except Exception:
		similarity = 0

        return similarity

    def compute_w2vsimilarity_multi(self, l1, str, domain):
        l2 = list()
        l2.append(str)
	l_conform = list()
	similarity = 0
        if(domain == 'DBpedia'):
	    for word in l1:
                try:
	            testword = GunicornApplication.w2vmodel_dbpedia.__getitem__(word)
	    	    l_conform.append(word)
	        except:
		    pass
                
	    if(len(l_conform)):
                try :
                    similarity = GunicornApplication.w2vmodel_dbpedia.n_similarity(l_conform, l2)
                except Exception:
                    similarity = 0

        elif(domain == 'Biomed'):
            for word in l1:
                try:
                    testword = GunicornApplication.w2vmodel_biomed.__getitem__(word)
                    l_conform.append(word)
                except:
                    pass

            if(len(l_conform)):
                try :
                    similarity = GunicornApplication.w2vmodel_biomed.n_similarity(l_conform, l2)
                except Exception:
                    similarity = 0

        return similarity

    def infer_docvector(self, s1, domain):
        if(domain == 'wiki_german'):
             return GunicornApplication.d2vmodel_german.infer_vector(s1, steps=25)
	else:
             if GunicornApplication.d2vmodel is None:
                 return 0
             else : 
                 return GunicornApplication.d2vmodel.infer_vector(s1, steps=25)

    def compute_d2vsimilarity(self, d1, vec2, domain):
	if(domain == 'wiki_german'):
            try:
                vec1 = GunicornApplication.d2vmodel_german.docvecs[d1]
                # Check whether entity is in model
                if(len(vec1) != GunicornApplication.d2vmodel_german.layer1_size) :
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
        else:   
            # Allows to use disambiguation service without loading a doc2vec embeddings. Simply return neutral 0
 #           if GunicornApplication.d2vmodel is None:
#		print 'I return the doc2vec similarity 0'
#                return 0
#            else :
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
                tb = traceback.format_exc()
		logger.exception(tb)
		similarity = 0
	    return similarity

w2v = Word2VecRest(__name__)


@w2v.route('/w2vsim', methods = ['POST'])
def w2vsim():
    json = request.get_json(force=True)
    data = json['data']
    domain = json['domain']
#    print 'variable domain'
#    print domain
    li = list()
    for q in data:
        split = q.split('|')

        if len(split) > 2 :
            l = list()
            for a in range(0, (len(split) - 1)):
                l.append(split[a])
            sim = w2v.compute_w2vsimilarity_multi(l, split[len(split) - 1], domain)
        else :
            sim = w2v.compute_w2vsimilarity(split[0], split[1], domain)
        result = {"ents":q, "sim":sim}
        li.append(result)
    return jsonify(data=li, domain=domain)


@w2v.route('/d2vsim', methods = ['POST'])
def d2vsim():
    json = request.get_json(force=True)
    logger.info("callup")
    sfs = json['data']
    domain = json['domain']
    f = list()
    for sf in sfs:
        candidates = sf['candidates']
	cansim = list()
	for i in range(0,len(candidates)):
            cansim.append(0)
	for i in range(0,10):
            contextvec = w2v.infer_docvector(sf['context'].split(), domain)
	    it = 0
            for can in candidates:
                similarity = w2v.compute_d2vsimilarity(can, contextvec, domain)
                val = cansim[it]
                val += similarity
                cansim[it] = val 
                it = it+1           
	for i in range(0, len(candidates)):
	    cansim[i] /= 10
        result = {"qryNr":sf['qryNr'], "surfaceForm":sf['surfaceForm'], "sim":cansim}
	f.append(result)
    return jsonify(data=f)



class GunicornApplication(BaseApplication):

    parser = SafeConfigParser()
    with codecs.open('config.ini', 'r', encoding='utf-8') as f:
        parser.readfp(f)

    #Mandatory Loading for standard disambiguation
    wiki_w2v_embeddings_file = parser.get('Word2VecRest', 'embeddings_w2v_wikipedia')
    w2vmodel_dbpedia = Word2Vec.load_word2vec_format(wiki_w2v_embeddings_file, binary=True)

    #If no doc2vec embeddings are loaded (due to memory constraints), we always return 0 as cosine similarity
    wiki_d2v_embeddings_file = parser.get('Word2VecRest', 'embeddings_d2v_wikipedia')
    if os.path.isfile(wiki_d2v_embeddings_file):
        d2vmodel = Doc2Vec.load(wiki_d2v_embeddings_file)
    else :
        d2vmodel = None

    #Optional Embeddings
    biomed_w2v_embedings_file = parser.get('Word2VecRest', 'embeddings_w2v_calbc')
    if os.path.isfile(biomed_w2v_embedings_file):
        w2vmodel_biomed = Word2Vec.load_word2vec_format(biomed_w2v_embedings_file, binary=True)

    wiki_d2v_german_embeddings = parser.get('Word2VecRest', 'embeddings_d2v_wikipedia_german')
    if os.path.isfile(wiki_d2v_german_embeddings):
        d2vmodel_german = Doc2Vec.load(wiki_d2v_german_embeddings)
    
    def __init__(self, wsgi_app, port=5000):
	self.options = {
            'bind': "127.0.0.1:{port}".format(port=port),
             'workers': 3,
             'preload_app': True,
	     'timeout': 200,
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
