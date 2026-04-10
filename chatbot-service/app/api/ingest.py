from fastapi import APIRouter, HTTPException
import logging
from app.utils.r2_client import R2Client
from app.loaders.pdf_loader import load_and_chunk_pdf_from_bytes
from app.core.vector_store import add_documents_to_faiss

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/ingest", tags=["Ingestion"])

r2 = R2Client()


@router.post("/")
def ingest_from_r2(prefix: str = "general/"):
    try:
        logger.info(f"🔍 Ingest request started with prefix: {prefix}")
        
        if not prefix:
            logger.warning("⚠️ Empty prefix provided")
            raise HTTPException(status_code=400, detail="Prefix cannot be empty")
        
        all_docs = []

        logger.debug(f"📄 Listing files from R2 with prefix: {prefix}")
        keys = r2.list_files(prefix)
        
        if not keys:
            logger.warning(f"⚠️ No files found with prefix: {prefix}")
            return {
                "status": "success",
                "files_processed": 0,
                "documents_indexed": 0,
                "warning": "No files found for ingestion"
            }
        
        logger.info(f"📄 Found {len(keys)} files to process")

        for idx, key in enumerate(keys, 1):
            try:
                logger.debug(f"Processing file {idx}/{len(keys)}: {key}")
                file_bytes = r2.download_file_bytes(key)

                agent_id = prefix.strip("/")

                docs = load_and_chunk_pdf_from_bytes(
                    file_bytes=file_bytes,
                    source=key,
                    agent_id=agent_id
                )

                all_docs.extend(docs)
                logger.info(f"✅ Successfully processed {key}: {len(docs)} documents")
            
            except Exception as e:
                logger.error(f"❌ Error processing file {key}: {str(e)}")
                continue
        
        if all_docs:
            logger.debug(f"💎 Adding {len(all_docs)} documents to FAISS")
            add_documents_to_faiss(all_docs)
            logger.info(f"✅ Successfully indexed {len(all_docs)} documents")
        else:
            logger.warning(f"⚠️ No documents were successfully processed from {len(keys)} files")

        return {
            "status": "success",
            "files_processed": len(keys),
            "documents_indexed": len(all_docs)
        }
    
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"❌ Error during ingestion: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Ingestion failed: {str(e)}")


@router.post("/upload")
async def ingest_uploaded_file():
    """
    Receive a PDF file from sow-service, save it to data/raw/general,
    and ingest it with agent_id = "general"
    
    # Ensure data/raw/general directory exists
    general_dir = DATA_DIR / "general"
    general_dir.mkdir(parents=True, exist_ok=True)

    # Save file to general directory
    file_path = general_dir / file.filename
    with open(file_path, "wb") as f:
        content = await file.read()
        f.write(content)

    # Load and chunk the PDF
    docs = load_and_chunk_pdf(
        path=str(file_path),
        agent_id="general"
    )

    # Add to FAISS vector store
    add_documents_to_faiss(docs)

    return {
        "status": "success",
        "filename": file.filename,
        "documents_indexed": len(docs)
    }
    """
    logger.debug("🔍 Upload endpoint called - currently disabled (automatic ingestion)")
    pass
