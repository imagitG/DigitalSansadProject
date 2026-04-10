from pathlib import Path
import logging
from langchain_community.vectorstores import FAISS
from app.core.embeddings import get_embeddings
from app.utils.r2_client import R2Client

logger = logging.getLogger(__name__)

FAISS_DIR = Path("data/faiss")
FAISS_DIR.mkdir(parents=True, exist_ok=True)
logger.debug(f"📁 FAISS directory initialized: {FAISS_DIR}")

FAISS_INDEX_KEY = "faiss/index.faiss"
FAISS_STORE_KEY = "faiss/index.pkl"

r2 = R2Client()



def download_faiss_from_r2():
    try:
        logger.info("📥 Attempting to download FAISS index from R2")
        index_bytes = r2.download_file_bytes(FAISS_INDEX_KEY)
        store_bytes = r2.download_file_bytes(FAISS_STORE_KEY)

        (FAISS_DIR / "index.faiss").write_bytes(index_bytes)
        (FAISS_DIR / "index.pkl").write_bytes(store_bytes)

        logger.info(f"✅ FAISS index downloaded from R2 successfully")

    except Exception as e:
        logger.warning(f"⚠️ No FAISS found in R2 (first run or error): {str(e)}")



def upload_faiss_to_r2():
    try:
        logger.info("📤 Uploading FAISS index to R2")
        r2.upload_file(
            FAISS_INDEX_KEY,
            (FAISS_DIR / "index.faiss").read_bytes()
        )
        r2.upload_file(
            FAISS_STORE_KEY,
            (FAISS_DIR / "index.pkl").read_bytes()
        )

        logger.info("✅ FAISS uploaded to R2 successfully")

    except FileNotFoundError as e:
        logger.error(f"❌ FAISS files not found locally: {str(e)}")
    except Exception as e:
        logger.error(f"❌ Failed to upload FAISS to R2: {str(e)}")



def create_or_load_faiss():
    try:
        logger.debug("🔏 Creating or loading FAISS index")
        embeddings = get_embeddings()

        index_file = FAISS_DIR / "index.faiss"

        if not index_file.exists():
            logger.info("🗣 FAISS index not found locally, attempting to download from R2")
            download_faiss_from_r2()

        if index_file.exists():
            logger.info("🗒 Loading FAISS index from local storage")
            vectorstore = FAISS.load_local(
                FAISS_DIR,
                embeddings,
                allow_dangerous_deserialization=True
            )
            logger.info("✅ FAISS index loaded successfully")
            return vectorstore
        
        logger.info("📄 Creating new FAISS index")
        vectorstore = FAISS.from_texts([], embedding=embeddings)
        logger.info("✅ New FAISS index created")
        return vectorstore
    
    except Exception as e:
        logger.error(f"❌ Failed to create or load FAISS index: {str(e)}")
        raise



def add_documents_to_faiss(docs):
    try:
        if not docs:
            logger.warning("⚠️ No documents provided to add to FAISS")
            return
        
        logger.info(f"📄 Adding {len(docs)} documents to FAISS")
        vectorstore = create_or_load_faiss()

        vectorstore.add_documents(docs)
        logger.info("📄 Documents added to FAISS index")

        vectorstore.save_local(FAISS_DIR)
        logger.info("📁 FAISS index saved locally")

        # 🔥 upload after update
        upload_faiss_to_r2()
    
    except Exception as e:
        logger.error(f"❌ Failed to add documents to FAISS: {str(e)}")
        raise



def get_retriever(k: int = 5):
    try:
        logger.debug(f"🔏 Creating retriever with k={k}")
        vectorstore = create_or_load_faiss()
        retriever = vectorstore.as_retriever(
            search_type="similarity",
            search_kwargs={"k": k}
        )
        logger.info(f"✅ Retriever created with k={k}")
        return retriever
    except Exception as e:
        logger.error(f"❌ Failed to create retriever: {str(e)}")
        raise