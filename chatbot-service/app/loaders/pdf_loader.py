from langchain_core.documents import Document
from langchain_text_splitters import RecursiveCharacterTextSplitter
from pypdf import PdfReader

def load_and_chunk_pdf(path: str, agent_id: str):
    reader = PdfReader(path)
    splitter = RecursiveCharacterTextSplitter(
        chunk_size=1000,
        chunk_overlap=150
    )

    documents = []

    for page_num, page in enumerate(reader.pages):
        text = page.extract_text()
        if not text:
            continue

        chunks = splitter.split_text(text)

        for chunk in chunks:
            documents.append(
                Document(
                    page_content=chunk,
                    metadata={
                        "agentId": agent_id,
                        "source": path,
                        "page": page_num + 1
                    }
                )
            )

    return documents
