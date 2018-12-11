# -*- coding: utf-8 -*-
"""
Spyder Editor

This is a temporary script file.
"""
#%%
from neo4j import GraphDatabase


uri = "bolt://localhost:7687"
driver = GraphDatabase.driver(uri, auth=("neo4j", "password"))

#%%

duplicateTitlesQry = """
MATCH (n:Article), (m:Article) 
WHERE n.title <> '' 
AND n.title = m.title 
AND n<>m 
RETURN n.title,n.pmid,n.doi,m.title,m.pmid,m.doi
"""
   
with driver.session() as session:
    result = session.run(duplicateTitlesQry)
    session.detach(result)

#%%
result.data()