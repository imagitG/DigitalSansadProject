from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api import ingest, chat

app = FastAPI(title="Digital Sansad Chatbot")

# ✅ CORS CONFIGURATION
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:3000",   # Next.js frontend
        "http://127.0.0.1:3000",
        "http://localhost:8081",   # sow-service
        "http://127.0.0.1:8081"
    ],
    allow_credentials=True,
    allow_methods=["*"],          # GET, POST, PUT, DELETE
    allow_headers=["*"],          # Authorization, Content-Type
)

# ✅ Register routers
app.include_router(ingest.router)
app.include_router(chat.router)
