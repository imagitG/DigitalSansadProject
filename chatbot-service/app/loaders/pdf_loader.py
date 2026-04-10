from langchain_core.documents import Document
from langchain_text_splitters import RecursiveCharacterTextSplitter
from pypdf import PdfReader
import io
import logging

logger = logging.getLogger(__name__)


def load_and_chunk_pdf_from_bytes(file_bytes: bytes, source: str, agent_id: str):
    try:
        if not file_bytes:
            logger.warning(f"⚠️ PDF file is empty: {source}")
            return []
        
        logger.info(f"📄 Loading PDF from bytes: {source} (size: {len(file_bytes)} bytes)")
        reader = PdfReader(io.BytesIO(file_bytes))
        
        num_pages = len(reader.pages)
        logger.info(f"✅ PDF has {num_pages} pages: {source}")

        splitter = RecursiveCharacterTextSplitter(
            chunk_size=1000,
            chunk_overlap=150
        )

        documents = []

        for page_num, page in enumerate(reader.pages):
            try:
                text = page.extract_text()
                if not text or text.strip() == "":
                    logger.warning(f"⚠️ No text extracted from page {page_num + 1}: {source}")
                    continue

                chunks = splitter.split_text(text)
                logger.debug(f"📄 Page {page_num + 1}: split into {len(chunks)} chunks")

                for chunk in chunks:
                    documents.append(
                        Document(
                            page_content=chunk,
                            metadata={
                                "agentId": agent_id,
                                "source": source,
                                "page": page_num + 1
                            }
                        )
                    )
            except Exception as e:
                logger.error(f"❌ Error processing page {page_num + 1} of {source}: {str(e)}")
                continue

        logger.info(f"✅ Created {len(documents)} chunks from {source}")
        return documents
    
    except Exception as e:
        logger.error(f"❌ Failed to load and chunk PDF {source}: {str(e)}")
        raise