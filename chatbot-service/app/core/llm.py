from langchain_google_genai import ChatGoogleGenerativeAI
from app.config import GOOGLE_API_KEY

def get_llm():
    return ChatGoogleGenerativeAI(
        model="gemini-2.5-flash",
        temperature=0.4,
        google_api_key=GOOGLE_API_KEY
    )
