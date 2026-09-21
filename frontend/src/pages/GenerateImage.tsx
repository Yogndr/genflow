import { useState } from "react";
import api from "../api/axios";
import type { ImageGenerationResponse } from "../types/imageGeneration";

function GenerateImage() {
  const [prompt, setPrompt] = useState("");

  const [result, setResult] =
    useState<ImageGenerationResponse | null>(null);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleGenerate = async (
    event: React.FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    setError("");
    setResult(null);

    if (!prompt.trim()) {
      setError("Please enter an image prompt.");
      return;
    }

    setLoading(true);

    try {
      const response =
        await api.post<ImageGenerationResponse>(
          "/images/generate",
          {
            prompt,
          }
        );

      setResult(response.data);
    } catch (error: any) {
      setError(
        error.response?.data?.message ||
          "Failed to generate image"
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
          Generate Image
        </h1>

        <p className="mt-2 text-gray-600">
          Turn your ideas into AI-generated images.
        </p>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">

        {/* Prompt panel */}
        <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
          <h2 className="mb-5 text-lg font-semibold text-gray-900">
            Image Prompt
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
                Describe your image
              </label>

              <textarea
                value={prompt}
                onChange={(e) =>
                  setPrompt(e.target.value)
                }
                placeholder="Example: A futuristic city at night with neon lights and flying cars..."
                rows={10}
                className="w-full resize-none rounded-lg border border-gray-300 px-4 py-3 outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
              />
            </div>

            <div className="rounded-lg bg-gray-50 p-4">
              <p className="text-sm font-medium text-gray-700">
                Prompt tip
              </p>

              <p className="mt-1 text-sm leading-6 text-gray-500">
                Include the subject, environment, lighting,
                mood, and visual details for better results.
              </p>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full rounded-lg bg-indigo-600 px-5 py-3 font-medium text-white transition hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {loading
                ? "Generating image..."
                : "Generate Image"}
            </button>
          </form>
        </div>

        {/* Result panel */}
        <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
          <div className="mb-5 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-gray-900">
              Generated Image
            </h2>

            {result && (
              <span
                className={`rounded-full px-3 py-1 text-xs font-medium ${
                  result.status === "COMPLETED"
                    ? "bg-green-50 text-green-700"
                    : result.status === "FAILED"
                    ? "bg-red-50 text-red-700"
                    : "bg-yellow-50 text-yellow-700"
                }`}
              >
                {result.status}
              </span>
            )}
          </div>

          {loading ? (
            <div className="flex min-h-96 items-center justify-center rounded-xl bg-gray-50">
              <div className="text-center">
                <div className="mx-auto h-8 w-8 animate-spin rounded-full border-4 border-gray-200 border-t-indigo-600" />

                <p className="mt-4 font-medium text-gray-600">
                  Creating your image...
                </p>

                <p className="mt-1 text-sm text-gray-400">
                  AI image generation may take a little
                  longer than text generation.
                </p>
              </div>
            </div>
          ) : result?.imageUrl ? (
            <div>
              <div className="overflow-hidden rounded-xl bg-gray-100">
                <img
                  src={result.imageUrl}
                  alt={result.prompt}
                  className="h-auto w-full object-cover"
                />
              </div>

              <div className="mt-5">
                <p className="text-xs font-medium uppercase tracking-wide text-gray-400">
                  Prompt
                </p>

                <p className="mt-2 leading-6 text-gray-700">
                  {result.prompt}
                </p>
              </div>

              <div className="mt-5 flex gap-3">
                <a
                  href={result.imageUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-indigo-700"
                >
                  Open Full Image
                </a>

                <button
                  type="button"
                  onClick={() =>
                    navigator.clipboard.writeText(
                      result.imageUrl!
                    )
                  }
                  className="rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 transition hover:bg-gray-50"
                >
                  Copy URL
                </button>
              </div>
            </div>
          ) : result?.status === "FAILED" ? (
            <div className="flex min-h-96 items-center justify-center rounded-xl bg-red-50 text-center">
              <div>
                <p className="font-medium text-red-700">
                  Image generation failed
                </p>

                <p className="mt-2 text-sm text-red-500">
                  Try changing your prompt and generating
                  again.
                </p>
              </div>
            </div>
          ) : (
            <div className="flex min-h-96 items-center justify-center rounded-xl bg-gray-50 text-center">
              <div>
                <p className="font-medium text-gray-600">
                  Your generated image will appear here.
                </p>

                <p className="mt-2 text-sm text-gray-400">
                  Describe an image and click Generate Image.
                </p>
              </div>
            </div>
          )}
        </div>

      </div>
    </div>
  );
}

export default GenerateImage;