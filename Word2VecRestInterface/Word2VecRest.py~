__author__ = 'quh'

from flask import Flask, request, jsonify
from gensim.models.word2vec import Word2Vec
from gensim.models.doc2vec import Doc2Vec
from gensim import matutils
from numpy import dot
import logging

logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)

class Word2VecRest(Flask):

    def __init__(self, *args, **kwargs):
        super(Word2VecRest, self).__init__(*args, **kwargs)
        W2VPATH = '/mnt/ssd1/disambiguation/word2vec/WikiEntityModel_400_neg10_iter5.seq'
        D2VPATH = '/mnt/ssd1/disambiguation/word2vec/doc2vec/doc2vec_wiki_model.d2v'
#        self.w2vmodel = Word2Vec.load_word2vec_format(W2VPATH, binary=True)
        self.d2vmodel = Doc2Vec.load(D2VPATH)


    def compute_w2vsimilarity(self, s1, s2):
        try:
            similarity = self.w2vmodel.similarity(s1, s2)
        except Exception:
            similarity = 0
        return similarity

    def compute_w2vsimilarity_multi(self, l1, str):
        l2 = list()
        l2.append(str)
        similarity = 0
        try :
            similarity = self.w2vmodel.n_similarity(l1, l2)
        except Exception:
            similarity = 0
        return similarity

    def infer_docvector(self, s1):
        print s1
        return self.d2vmodel.infer_vector(s1)

    def compute_d2vsimilarity(self, d1, vec2):
        try:
            vec1 = self.d2vmodel.docvecs[d1]
            similarity = dot(matutils.unitvec(vec1), matutils.unitvec(vec2))
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
    sfs = json['document']
    print sfs
    f = list()
    for sf in sfs:
#        print sf['context']
        contextvec = w2v.infer_docvector(sf['context'].split())
#       print contextvec
        candidates = sf['candidates']
        cansim = list()
        for can in candidates:
            similarity = w2v.compute_d2vsimilarity(can, contextvec)
#            print similarity
            cansim.append(similarity)
        result = {"qryNr":sf['qryNr'], "surfaceForm":sf['surfaceForm'], "sim":cansim}
#        print result
        f.append(result)
        print f
    return jsonify(surfaceForms=f)

if __name__ == "__main__":
    w2v.run()
