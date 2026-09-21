import { useState } from "react";
import api from "../api/axios";

import type {
  GenerationResponse,
  GenerationType,
} from "../types/generation";

function GenerateContent() {
  const [type, setType] =
    useState<GenerationType>("GENERATE");

  const [input, setInput] = useState("");

  const [result, setResult] =
    useState<GenerationResponse | null>(null);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleGenerate = async (
    event: React.FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    setError("");
    setResult(null);

    if (!input.trim()) {
      setError("Please enter some content.");
      return;
    }

    setLoading(true);

    try {
      const response =
        await api.post<GenerationResponse>(
          "/generations",
          {
            type,
            input,
          }
        );

      setResult(response.data);
    } catch (error: any) {
      setError(
        error.response?.data?.message ||
          "Failed to generate content"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">
          Generate Content
        </h1>

        <p className="mt-2 text-gray-600">
          Generate, rewrite, expand, or shorten content
          using AI.
        </p>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">

        {/* Input panel */}
        <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
          <h2 className="mb-5 text-lg font-semibold text-gray-900">
            Your Content
          </h2>

          {error && (
            <div className="mb-5 rounded-lg bg-red-50 p-3 text-sm text-red-700">
              {error}
            </div>
          )}

          <form
            onSubmit={handleGenerate}
            className="space-y-5"
          >
            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Action
              </label>

              <select
                value={type}
                onChange={(e) =>
                  setType(
                    e.target.value as GenerationType
                  )
                }
                className="w-full rounded-lg border border-gray-300 bg-white px-4 py-3 outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
              >
                <option value="GENERATE">
                  Generate
                </option>

                <option value="REWRITE">
                  Rewrite
                </option>

                <option value="EXPAND">
                  Expand
                </option>

                <option value="SHORTEN">
                  Shorten
                </option>
              </select>
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                {type === "GENERATE"
                  ? "Prompt"
                  : "Content"}
              </label>

              <textarea
                value={input}
                onChange={(e) =>
                  setInput(e.target.value)
                }
                placeholder={
                  type === "GENERATE"
                    ? "What would you like GenFlow to write about?"
                    : "Paste the content you want to transform..."
                }
                rows={14}
                className="w-full resize-none rounded-lg border border-gray-300 px-4 py-3 outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full rounded-lg bg-indigo-600 px-5 py-3 font-medium text-white transition hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {loading
                ? "Generating..."
                : "Generate Content"}
            </button>
          </form>
        </div>

        {/* Output panel */}
        <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
          <div className="mb-5 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-gray-900">
              AI Result
            </h2>

            {result?.output && (
              <button
                type="button"
                onClick={() =>
                  navigator.clipboard.writeText(
                    result.output!
                  )
                }
                className="rounded-lg border border-gray-300 px-3 py-2 text-sm font-medium text-gray-700 transition hover:bg-gray-50"
              >
                Copy
              </button>
            )}
          </div>

          {loading ? (
            <div className="flex min-h-80 items-center justify-center">
              <p className="text-gray-500">
                GenFlow is generating your content...
              </p>
            </div>
          ) : result ? (
            <div>
              <div className="mb-4 flex items-center gap-3">
                <span className="rounded-full bg-green-50 px-3 py-1 text-xs font-medium text-green-700">
                  {result.status}
                </span>

                <span className="text-xs text-gray-500">
                  {result.type}
                </span>
              </div>

              <div className="whitespace-pre-wrap leading-7 text-gray-700">
                {result.output ||
                  "No output was generated."}
              </div>
            </div>
          ) : (
            <div className="flex min-h-80 items-center justify-center text-center">
              <div>
                <p className="font-medium text-gray-600">
                  Your generated content will appear here.
                </p>

                <p className="mt-2 text-sm text-gray-400">
                  Select an action, enter your content,
                  and click Generate Content.
                </p>
              </div>
            </div>
          )}
        </div>

      </div>
    </div>
  );
}

export default GenerateContent;