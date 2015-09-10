# DoSeR - Disambiguation of Semantic Resources

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

###Cite
If you use this framework, please use the following snippet to cite this work:

Zwicklbauer, S.; Seifert, C. & Granitzer, M.: From General to Specialized Domain: Analyzing Three Crucial Problems of Biomedical Entity Disambiguation, Proceedings of 26th International Conference on Database and Expert Systems Applications (DEXA), 76-93, Springer,

###Contact
If you have further question, please ask stefan.zwicklbauer@uni-passau.de