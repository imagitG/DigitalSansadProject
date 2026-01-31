# app/api/ingest.py

from fastapi import APIRouter, UploadFile, File
from pathlib import Path
from app.loaders.pdf_loader import load_and_chunk_pdf
from app.core.vector_store import add_documents_to_faiss

router = APIRouter(prefix="/ingest", tags=["Ingestion"])

DATA_DIR = Path("data/raw")

@router.post("/")
def ingest_all_documents():
    all_docs = []

    for domain_dir in DATA_DIR.iterdir():
        if not domain_dir.is_dir():
            continue

        agent_id = domain_dir.name  # loksabha / sow / business

        for pdf_path in domain_dir.glob("*.pdf"):
             docs = load_and_chunk_pdf(
                path=str(pdf_path),
                agent_id=agent_id
            )
             all_docs.extend(docs)

    add_documents_to_faiss(all_docs)

    return {
        "status": "success",
        "documents_indexed": len(all_docs)
    }


@router.post("/upload")
async def ingest_uploaded_file(file: UploadFile = File(...)):
    """
    Receive a PDF file from sow-service, save it to data/raw/general,
    and ingest it with agent_id = "general"
    """
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
