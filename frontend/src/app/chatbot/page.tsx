"use client";

import { useState } from "react";
import Navbar from "@/components/navbar";
import ReactMarkdown from "react-markdown";
import { CHATBOT_SERVICE_API } from "@/utils/api";

type ChatMessage = {
  role: "user" | "assistant";
  content: string;
  sources?: any[];
};

type ChatResponse = {
  answer: string;
  sources: any[];
};

const AGENTS = [
  { id: "plot", label: "Papers Laid on the Table" },
  { id: "bulletin", label: "Bulletins" },
  { id: "question_list", label: "Questions List" },
  { id: "general", label: "general" },
];

export default function ChatbotPage() {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState("");
  const [agentId, setAgentId] = useState("loksabha");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const sendMessage = async () => {
    if (!input.trim()) return;

    const userMessage: ChatMessage = {
      role: "user",
      content: input,
    };

    setMessages((prev) => [...prev, userMessage]);
    setInput("");
    setLoading(true);
    setError(null);

    try {
      const res = await fetch(`${CHATBOT_SERVICE_API}/chat`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          query: userMessage.content,
          agentId,
        }),
      });

      if (!res.ok) throw new Error("Chat request failed");

      const data: ChatResponse = await res.json();

      const botMessage: ChatMessage = {
        role: "assistant",
        content: data.answer,
        sources: data.sources,
      };

      setMessages((prev) => [...prev, botMessage]);
    } catch (err: any) {
      setError(err.message || "Something went wrong");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="min-h-screen bg-white flex flex-col">
      <Navbar />

      {/* Header */}
      <header className="px-6 py-4 bg-white border-b border-[#E5E5E5] space-y-2">
        <h1 className="heading-font text-2xl text-center text-[#0B0B0B]">
          Parliamentary Assistant
        </h1>
        <p className="text-sm text-center text-[#5A5A5A]">
          Ask questions about parliamentary documents and procedures
        </p>

        {/* Category Selector */}
        <div className="flex justify-center mt-3">
          <select
            value={agentId}
            onChange={(e) => setAgentId(e.target.value)}
            className="
              border border-[#E5E5E5] rounded-lg
              px-4 py-2 text-sm text-[#2E2E2E]
              bg-white
              focus:outline-none
              focus:border-[#D4A017]
              focus:ring-2 focus:ring-[rgba(212,160,23,0.4)]
            "
          >
            {AGENTS.map((a) => (
              <option key={a.id} value={a.id}>
                {a.label}
              </option>
            ))}
          </select>
        </div>
      </header>

      {/* Chat Window */}
      <section className="flex-1 overflow-y-auto px-6 py-6 space-y-4">
        {messages.length === 0 && (
          <div className="text-[#8A8A8A] text-center mt-20">
            Select a category and ask your question 👇
          </div>
        )}

        {messages.map((msg, idx) => (
          <div key={idx} className="space-y-2">

            {/* Message Bubble */}
            <div
              className={`max-w-3xl px-4 py-3 rounded-lg whitespace-pre-wrap text-sm ${msg.role === "user"
                ? "ml-auto bg-[#D4A017] text-[#0B0B0B]"
                : "mr-auto bg-white border border-[#E5E5E5] text-[#2E2E2E]"
                }`}
            >
              <ReactMarkdown>
                {msg.content}
              </ReactMarkdown>
            </div>

            {/* Sources */}
            {msg.role === "assistant" && msg.sources?.length ? (
              <details className="max-w-3xl mr-auto text-sm bg-[#F9F9F9] border border-[#E5E5E5] rounded-lg px-4 py-2">
                <summary className="cursor-pointer text-[#2563EB] font-medium">
                  Sources ({msg.sources.length})
                </summary>
                <ul className="mt-2 space-y-1 text-[#2E2E2E]">
                  {msg.sources.map((src, i) => (
                    <li key={i} className="text-xs border-l-2 border-[#D4A017] pl-2">
                      {Object.entries(src).map(([k, v]) => (
                        <div key={k}>
                          <span className="font-medium">{k}:</span>{" "}
                          {String(v)}
                        </div>
                      ))}
                    </li>
                  ))}
                </ul>
              </details>
            ) : null}
          </div>
        ))}

        {loading && (
          <div className="text-[#5A5A5A] text-sm">Thinking…</div>
        )}
        {error && (
          <div className="text-red-700 text-sm">{error}</div>
        )}
      </section>

      {/* Input */}
      <footer className="border-t border-[#E5E5E5] bg-white px-6 py-4">
        <div className="flex gap-3">
          <input
            type="text"
            placeholder="Ask a question…"
            className="
              flex-1 border border-[#E5E5E5] rounded-lg
              px-10 py-2 mx-5 text-sm text-[#2E2E2E]
              placeholder:text-[#8A8A8A]
              focus:outline-none
              focus:border-[#D4A017]
              focus:ring-2 focus:ring-[rgba(212,160,23,0.4)]
            "
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && sendMessage()}
            disabled={loading}
          />
          <button
            onClick={sendMessage}
            disabled={loading}
            className="
              px-5 py-2 rounded-lg text-sm font-semibold
              bg-[#D4A017] text-[#0B0B0B]
              hover:bg-[#E6B325]
              disabled:bg-[#F1E4B3]
              disabled:text-[#8A8A8A]
            "
          >
            Send
          </button>
        </div>
      </footer>
    </main>
  );
}
