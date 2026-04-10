# app/api/chat.py

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
import logging

from app.core.vector_store import create_or_load_faiss
from app.core.embeddings import get_embeddings
from app.core.prompt import build_prompt
from app.core.llm import get_llm

logger = logging.getLogger(__name__)

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
    try:
        logger.info(f"🔍 Chat request received. Query: {req.query[:50]}..., Agent: {req.agentId}")
        
        if not req.query or not req.query.strip():
            logger.warning("⚠️ Empty query received")
            raise HTTPException(status_code=400, detail="Query cannot be empty")
        
        if not req.agentId:
            logger.warning("⚠️ Missing agentId")
            raise HTTPException(status_code=400, detail="agentId is required")
        
        logger.debug("🔏 Loading embeddings")
        embeddings = get_embeddings()
        
        logger.debug("🔏 Loading vector store")
        vectorstore = create_or_load_faiss()

        logger.info(f"🔍 Searching for similar documents for agent: {req.agentId}")
        docs = vectorstore.similarity_search(
            req.query,
            k=5,
            filter={"agentId": req.agentId}
        )

        logger.info(f"✅ Retrieved {len(docs)} documents for query: {req.query}")
        
        if not docs:
            logger.warning(f"⚠️ No documents found for agent {req.agentId}")

        logger.debug("🔏 Building prompt")
        prompt = build_prompt(req.query, docs)
        
        logger.debug("🔏 Initializing LLM")
        llm = get_llm()
        
        logger.debug("🔏 Invoking LLM")
        response = llm.invoke(prompt)
        
        logger.info("✅ Chat response generated successfully")

        return {
            "answer": response.content,
            "sources": [d.metadata for d in docs]
        }
    
    except HTTPException:
        raise
    except ValueError as e:
        logger.error(f"❌ Validation error: {str(e)}")
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logger.error(f"❌ Unexpected error in chat endpoint: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail="An error occurred while processing your request")
