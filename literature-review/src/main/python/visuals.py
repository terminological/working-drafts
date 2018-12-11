# -*- coding: utf-8 -*-
"""
Spyder Editor

This is a temporary script file.
"""

from neo4j import GraphDatabase

uri = "bolt://localhost:7687"
driver = GraphDatabase.driver(uri, auth=("neo4j", "password"))

def print_friends_of(tx):
    for record in tx.run("match (n:Article), (m:Article) WHERE n.title <> '' AND n.title = m.title AND n<>m RETURN n.title,n.pmid,n.doi,m.title,m.pmid,m.doi"):
        print(record["m.title"])
#%%
with driver.session() as session:
    session.read_transaction(print_friends_of)