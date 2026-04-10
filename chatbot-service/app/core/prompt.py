import logging

logger = logging.getLogger(__name__)

SYSTEM_PROMPT = """
You are an official assistant for Digital Sansad.
Answer using the provided context. Format of answering : give a short explanation, followed by what you inferred from context.
Cite sources clearly.
"""

def build_prompt(query, docs):
    try:
        if not query:
            logger.warning("⚠️ build_prompt called with empty query")
            raise ValueError("Query cannot be empty")
        
        if not docs:
            logger.warning("⚠️ build_prompt called with no documents")
        
        logger.debug(f"🔏 Building prompt for query: {query[:50]}... with {len(docs)} documents")
        
        context = "\n\n".join(
            f"[Source: {d.metadata['source']} | Page: {d.metadata['page']}]\n{d.page_content}"
            for d in docs
        )

        prompt = f"""
{SYSTEM_PROMPT}

Context:
{context}

Question:
{query}

Answer:
"""
        
        logger.debug("✅ Prompt built successfully")
        return prompt
    
    except Exception as e:
        logger.error(f"❌ Failed to build prompt: {str(e)}")
        raise
