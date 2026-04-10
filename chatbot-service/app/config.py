from dotenv import load_dotenv
import os
import logging

logger = logging.getLogger(__name__)

load_dotenv()

# 🔑 Gemini
GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY")

# 📦 R2 CONFIG
R2_ACCESS_KEY = os.getenv("R2_ACCESS_KEY")
R2_SECRET_KEY = os.getenv("R2_SECRET_KEY")
R2_BUCKET_NAME = os.getenv("R2_BUCKET_NAME")
R2_ENDPOINT = os.getenv("R2_ENDPOINT")

# 📁 Local temp storage (for FAISS)
DATA_DIR = os.getenv("DATA_DIR", "./data")


# ✅ VALIDATION FUNCTION (USED IN main.py STARTUP)
def validate_env():
    logger.info("🔍 Validating environment variables...")

    required = {
        "GOOGLE_API_KEY": GOOGLE_API_KEY,
        "R2_ACCESS_KEY": R2_ACCESS_KEY,
        "R2_SECRET_KEY": R2_SECRET_KEY,
        "R2_BUCKET_NAME": R2_BUCKET_NAME,
        "R2_ENDPOINT": R2_ENDPOINT
    }

    missing = [key for key, value in required.items() if not value]

    if missing:
        logger.error(f"❌ Missing environment variables: {', '.join(missing)}")
        raise RuntimeError(f"Missing environment variables: {missing}")

    logger.info("✅ All environment variables validated successfully")

    # Optional debug logs (safe ones only)
    logger.info(f"📦 R2 bucket: {R2_BUCKET_NAME}")
    logger.info(f"📁 Data directory: {DATA_DIR}")