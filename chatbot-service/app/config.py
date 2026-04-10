from dotenv import load_dotenv
import os
import logging

logger = logging.getLogger(__name__)

load_dotenv()

# 🔑 Gemini
GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY")
if not GOOGLE_API_KEY:
    logger.error("GOOGLE_API_KEY environment variable is missing")
    raise RuntimeError("GOOGLE_API_KEY is missing")

logger.info("✅ Google API key configured")

# 📦 R2 CONFIG
R2_ACCESS_KEY = os.getenv("R2_ACCESS_KEY")
R2_SECRET_KEY = os.getenv("R2_SECRET_KEY")
R2_BUCKET_NAME = os.getenv("R2_BUCKET_NAME")
R2_ENDPOINT = os.getenv("R2_ENDPOINT")

if not all([R2_ACCESS_KEY, R2_SECRET_KEY, R2_BUCKET_NAME, R2_ENDPOINT]):
    missing = []
    if not R2_ACCESS_KEY:
        missing.append("R2_ACCESS_KEY")
    if not R2_SECRET_KEY:
        missing.append("R2_SECRET_KEY")
    if not R2_BUCKET_NAME:
        missing.append("R2_BUCKET_NAME")
    if not R2_ENDPOINT:
        missing.append("R2_ENDPOINT")
    logger.error(f"R2 configuration is incomplete. Missing: {', '.join(missing)}")
    raise RuntimeError(f"R2 configuration is missing: {', '.join(missing)}")

logger.info(f"✅ R2 configured with bucket: {R2_BUCKET_NAME}")

# 📁 Local temp storage (for FAISS download)
DATA_DIR = os.getenv("DATA_DIR", "./data")
logger.info(f"✅ Data directory set to: {DATA_DIR}")