# app/api/chat.py

from fastapi import APIRouter
from pydantic import BaseModel

from app.core.vector_store import create_or_load_faiss
from app.core.embeddings import get_embeddings
from app.core.prompt import build_prompt
from app.core.llm import get_llm

router = APIRouter(prefix="/chat", tags=["Chat"])


# ---------- Request / Response Models ----------

class ChatRequest(BaseModel):
    query: str
    agentId: str = "plot"


class ChatResponse(BaseModel):
    answer: str
    sources: list


# ---------- Endpoint ----------

@router.post("/", response_model=ChatResponse)
def chat(req: ChatRequest):
    embeddings = get_embeddings()
    vectorstore = create_or_load_faiss()

    docs = vectorstore.similarity_search(
        req.query,
        k=5,
        filter={"agentId": req.agentId}
    )

    print(f"\nRetrieved {len(docs)} documents for query: {req.query}\n")

    prompt = build_prompt(req.query, docs)
    llm = get_llm()
    response = llm.invoke(prompt)

    return {
        "answer": response.content,
        "sources": [d.metadata for d in docs]
    }
