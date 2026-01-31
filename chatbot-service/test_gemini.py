# test_embeddings.py
from app.core.embeddings import get_embeddings

emb = get_embeddings()
vec = emb.embed_query("Lok Sabha bill process")
print(len(vec))
