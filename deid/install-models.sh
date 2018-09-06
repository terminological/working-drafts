#!/bin/bash

# /media/data/Data/coreNLP
# loc="$1"

cd "/tmp"

wget "http://nlp.stanford.edu/software/stanford-english-corenlp-2018-02-27-models.jar"
wget "http://nlp.stanford.edu/software/stanford-english-kbp-corenlp-2018-02-27-models.jar"

# loc="/media/data/Data/coreNLP"
mvn install:install-file -Dfile="stanford-english-corenlp-2018-02-27-models.jar" \
                         -DgroupId=edu.stanford.nlp \
                         -DartifactId=stanford-corenlp-eng \
                         -Dversion=3.9.1 \
                         -Dpackaging=jar \
                         -DgeneratePom=true
                         
mvn install:install-file -Dfile="stanford-english-corenlp-2018-02-27-models.jar" \
                         -DgroupId=edu.stanford.nlp \
                         -DartifactId=stanford-corenlp-eng-additional \
                         -Dversion=3.9.1 \
                         -Dpackaging=jar \
                         -DgeneratePom=true
                        