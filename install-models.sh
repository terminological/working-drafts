#!/bin/bash

# /media/data/Data/coreNLP
# loc="$1"

loc="/media/data/Data/coreNLP"
mvn install:install-file -Dfile="$loc/stanford-english-corenlp-2018-02-27-models.jar" \
                         -DgroupId=edu.stanford.nlp \
                         -DartifactId=stanford-corenlp-eng \
                         -Dversion=3.9.1 \
                         -Dpackaging=jar \
                         -DgeneratePom=true
                         
mvn install:install-file -Dfile="$loc/stanford-english-corenlp-2018-02-27-models.jar" \
                         -DgroupId=edu.stanford.nlp \
                         -DartifactId=stanford-corenlp-eng-additional \
                         -Dversion=3.9.1 \
                         -Dpackaging=jar \
                         -DgeneratePom=true
                        