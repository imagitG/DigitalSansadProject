# app/core/vector_store.py

from pathlib import Path
from langchain_community.vectorstores import FAISS
from app.core.embeddings import get_embeddings

FAISS_DIR = Path("data/faiss")
FAISS_DIR.mkdir(parents=True, exist_ok=True)


def create_or_load_faiss():
    embeddings = get_embeddings()
    index_file = FAISS_DIR / "index.faiss"

    if index_file.exists():
        return FAISS.load_local(
            FAISS_DIR,
            embeddings,
            allow_dangerous_deserialization=True
        )

    # First-time empty index
    return FAISS.from_texts(
        texts=["init"],
        embedding=embeddings
    )


def add_documents_to_faiss(docs):
    vectorstore = create_or_load_faiss()
    vectorstore.add_documents(docs)
    vectorstore.save_local(FAISS_DIR)


def get_retriever(k: int = 5):
    vectorstore = create_or_load_faiss()
    return vectorstore.as_retriever(
        search_type="similarity",
        search_kwargs={"k": k}
    )
