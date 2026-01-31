SYSTEM_PROMPT = """
You are an official assistant for Digital Sansad.
Answer using the provided context. Format of answering : give a short explanation, followed by what you inferred from context.
Cite sources clearly.
"""

def build_prompt(query, docs):
    context = "\n\n".join(
        f"[Source: {d.metadata['source']} | Page: {d.metadata['page']}]\n{d.page_content}"
        for d in docs
    )

    return f"""
{SYSTEM_PROMPT}

Context:
{context}

Question:
{query}

Answer:
"""
