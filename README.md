#DoSeR - Disambiguation of Semantic Resources

**If you are interested in the entity disambiguation system of DoSeR only, please have a look at our DoSeR-Disambiguation repository at [Github DoSeR-Disambiguation](https://github.com/quhfus/DoSeR-Disambiguation)**

Doser is a framework, written during my PhD to easily integrate entity disambiguation algorithms for different knowledge bases. It is generic in the way that knowledge bases with general entities (e.g. Wikipedia) and more specialized entities (e.g. biomedical entities) can be used. This framework is being continuously developed and may contain bugs. 
The framework is written in Java and is deployed via Maven.

This package contains the following services: 

- Disambiguation Service 
- Table Disambiguation Service
- RDFSummarization
- Category Suggestion

Basically this project is split up into the following maven subpackages:

- doser-disambiguationserver
- doser-core
- doser-hbase
- doser-hadoop
- doser-extensions
- doser-experiments
- doser-gerbilrest

###Configuration 

To make this code runnable, this code must be deployed on a webserver. Additionally, the following configuration file located in src/main/resources of doser-disambiguationserver must be adapted to run this framework probably.

###Service

All services can be addressed with the following links: 

- Disambiguation Service: *serverurl*/doser-disambiguationserver/disambiguate-proxy
- TableDisambiguation Service: *serverurl*/doser-disambiguationserver/disambiguatetable-proxy
- RDFSummarization Service: *serverurl*/doser-disambiguationserver/summarize
- CategorySuggestion Service : *serverurl*/doser-disambiguationserver/categorysuggestion-proxy

##Citation
If you use DoSeR in your research, please cite the following paper:

    @inproceedings{DBLP:conf/esws/ZwicklbauerSG16,
    author    = {Stefan Zwicklbauer and Christin Seifert and Michael Granitzer},
    title     = {DoSeR - A Knowledge-Base-Agnostic Framework for Entity Disambiguation Using Semantic Embeddings},
    booktitle = {The Semantic Web. Latest Advances and New Domains - 13th International
               Conference, {ESWC} 2016, Heraklion, Crete, Greece, May 29 - June 2,
               2016, Proceedings},
    pages     = {182--198},
    year      = {2016},
    crossref  = {DBLP:conf/esws/2016},
    url       = {http://dx.doi.org/10.1007/978-3-319-34129-3_12},
    doi       = {10.1007/978-3-319-34129-3_12},
    timestamp = {Mon, 23 May 2016 13:46:28 +0200},
    biburl    = {http://dblp.uni-trier.de/rec/bib/conf/esws/ZwicklbauerSG16},
    bibsource = {dblp computer science bibliography, http://dblp.org}
    }

###Contact
If you have further question, please ask stefan.zwicklbauer@uni-passau.de
