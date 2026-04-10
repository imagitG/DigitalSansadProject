from sentence_transformers import SentenceTransformer
from langchain_core.embeddings import Embeddings
import os
import logging

logger = logging.getLogger(__name__)

# 🔥 Use HuggingFace model name (NOT local path)
MODEL_NAME = "sentence-transformers/all-MiniLM-L6-v2"

# 🔥 USE LOCAL PATH
# MODEL_PATH = os.path.join(os.getcwd(), "models", "all-MiniLM-L6-v2")

_model = None


class SentenceTransformerEmbeddings(Embeddings):
    """
    LangChain-compatible embeddings wrapper using sentence-transformers
    """

    def __init__(self):
        try:
            global _model
            if _model is None:
                logger.info(f"🔄 Loading embedding model: {MODEL_NAME}")
                _model = SentenceTransformer(
                    # MODEL_PATH,
                    MODEL_NAME,
                    device="cpu",
                    cache_folder=os.getenv("HF_HOME", "./models"),  
                    # local_files_only=True   # 🔥 FORCE OFFLINE
                )
                logger.info(f"✅ Embedding model loaded successfully")
            self.model = _model
        except Exception as e:
            logger.error(f"❌ Failed to load embedding model: {str(e)}")
            raise

    def embed_documents(self, texts: list[str]) -> list[list[float]]:
        try:
            if not texts:
                logger.warning("⚠️ embed_documents called with empty text list")
                return []
            logger.debug(f"📄 Embedding {len(texts)} documents")
            embeddings = self.model.encode(
                texts,
                normalize_embeddings=True
            ).tolist()
            return embeddings
        except Exception as e:
            logger.error(f"❌ Error embedding documents: {str(e)}")
            raise

    def embed_query(self, text: str) -> list[float]:
        try:
            if not text:
                logger.warning("⚠️ embed_query called with empty text")
                return []
            logger.debug(f"🔍 Embedding query: {text[:50]}...")
            embedding = self.model.encode(
                text,
                normalize_embeddings=True
            ).tolist()
            return embedding
        except Exception as e:
            logger.error(f"❌ Error embedding query: {str(e)}")
            raise


def get_embeddings():
    try:
        logger.debug("🔏 Getting embeddings instance")
        return SentenceTransformerEmbeddings()
    except Exception as e:
        logger.error(f"❌ Failed to get embeddings: {str(e)}")
        raise