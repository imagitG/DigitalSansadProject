from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
import logging

from app.api import ingest, chat
from app.config import validate_env
from app.core.vector_store import create_or_load_faiss

# ✅ LOGGING CONFIGURATION
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

logger = logging.getLogger(__name__)

app = FastAPI(title="Digital Sansad Chatbot")

logger.info("🚀 Initializing Digital Sansad Chatbot API")

# ✅ CORS CONFIGURATION
try:
    ALLOWED_ORIGINS = [
        "https://digitalsansadproject-auth-service.onrender.com",
        "https://digitalsansadproject-sow-service.onrender.com",
        "https://digital-sansad-project.vercel.app",  # ❗ removed trailing /
        "http://localhost:3000",
        "http://127.0.0.1:3000",
        "http://localhost:8081",
        "http://127.0.0.1:8081"
    ]

    app.add_middleware(
        CORSMiddleware,
        allow_origins=ALLOWED_ORIGINS,
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    logger.info("✅ CORS middleware configured successfully")

except Exception as e:
    logger.error(f"❌ Failed to configure CORS middleware: {str(e)}")
    raise


# ✅ REGISTER ROUTERS
try:
    app.include_router(ingest.router)
    logger.info("✅ Ingest router registered")

    app.include_router(chat.router)
    logger.info("✅ Chat router registered")

except Exception as e:
    logger.error(f"❌ Failed to register routers: {str(e)}")
    raise


# ✅ ROOT ENDPOINT (Render health check)
@app.get("/")
def home():
    return {"status": "Chatbot Service Running"}


# ✅ HEALTH ENDPOINT
@app.get("/health")
def health():
    return {"status": "OK"}


# ✅ GLOBAL ERROR HANDLER
@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    logger.error(f"❌ Unhandled error: {str(exc)}", exc_info=True)
    return JSONResponse(
        status_code=500,
        content={"detail": f"Internal server error: {str(exc)}"}
    )


# ✅ STARTUP EVENT
@app.on_event("startup")
async def startup_event():
    logger.info("🔄 Application startup initiated")

    try:
        # 🔥 1. Validate ENV
        validate_env()
        logger.info("✅ Environment variables validated")

        # 🔥 2. Load FAISS (important for cold start)
        create_or_load_faiss()
        logger.info("✅ FAISS loaded successfully")

        logger.info("🚀 Digital Sansad Chatbot API started successfully")

    except Exception as e:
        logger.error(f"❌ Error during startup: {str(e)}", exc_info=True)
        raise


# ✅ SHUTDOWN EVENT
@app.on_event("shutdown")
async def shutdown_event():
    logger.info("🛑 Application shutdown initiated")

    try:
        logger.info("✅ Digital Sansad Chatbot API shut down gracefully")

    except Exception as e:
        logger.error(f"❌ Error during shutdown: {str(e)}", exc_info=True)