from langchain_google_genai import ChatGoogleGenerativeAI
import logging
from app.config import GOOGLE_API_KEY

logger = logging.getLogger(__name__)

def get_llm():
    try:
        logger.info("🔄 Initializing Gemini LLM")
        llm = ChatGoogleGenerativeAI(
            model="gemini-2.5-flash",
            temperature=0.4,
            google_api_key=GOOGLE_API_KEY
        )
        logger.info("✅ Gemini LLM initialized successfully")
        return llm
    except Exception as e:
        logger.error(f"❌ Failed to initialize LLM: {str(e)}")
        raise
