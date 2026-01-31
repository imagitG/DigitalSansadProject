# app/core/embeddings.py

from sentence_transformers import SentenceTransformer
from langchain_core.embeddings import Embeddings

MODEL_PATH = "models/all-MiniLM-L6-v2"

_model = None


class SentenceTransformerEmbeddings(Embeddings):
    """
    LangChain-compatible embeddings wrapper using sentence-transformers
    """

    def __init__(self):
        global _model
        if _model is None:
            _model = SentenceTransformer(MODEL_PATH, device="cpu")
        self.model = _model

    def embed_documents(self, texts: list[str]) -> list[list[float]]:
        return self.model.encode(
            texts,
            normalize_embeddings=True
        ).tolist()

    def embed_query(self, text: str) -> list[float]:
        return self.model.encode(
            text,
            normalize_embeddings=True
        ).tolist()


def get_embeddings():
    return SentenceTransformerEmbeddings()
