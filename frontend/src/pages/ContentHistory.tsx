import { useEffect, useState } from "react";
import api from "../api/axios";

import type { GenerationResponse } from "../types/generation";
import type { PageResponse } from "../types/pagination";

function ContentHistory() {
  const [data, setData] =
    useState<PageResponse<GenerationResponse> | null>(null);

  const [page, setPage] = useState(0);
  const [searchInput, setSearchInput] = useState("");
  const [query, setQuery] = useState("");

  const [selectedGeneration, setSelectedGeneration] =
    useState<GenerationResponse | null>(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const pageSize = 5;

  const fetchHistory = async () => {
    setLoading(true);
    setError("");

    try {
      const endpoint = query.trim()
        ? "/generations/search"
        : "/generations";

      const response = await api.get<
        PageResponse<GenerationResponse>
      >(endpoint, {
        params: query.trim()
          ? {
              query,
              page,
              size: pageSize,
            }
          : {
              page,
              size: pageSize,
            },
      });

      setData(response.data);
    } catch (error: any) {
      setError(
        error.response?.data?.message ||
          "Failed to load content history"
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, [page, query]);

  const handleSearch = (
    event: React.FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();
    setPage(0);
    setQuery(searchInput.trim());
  };

  const handleClearSearch = () => {
    setSearchInput("");
    setQuery("");
    setPage(0);
  };

  const handleView = async (id: number) => {
    setError("");

    try {
      const response =
        await api.get<GenerationResponse>(
          `/generations/${id}`
        );

      setSelectedGeneration(response.data);
    } catch (error: any) {
      setError(
        error.response?.data?.message ||
          "Failed to load generation"
      );
    }
  };

  const handleDelete = async (id: number) => {
    const confirmed = window.confirm(
      "Are you sure you want to delete this generation?"
    );

    if (!confirmed) return;

    setError("");

    try {
      await api.delete(`/generations/${id}`);

      if (selectedGeneration?.id === id) {
        setSelectedGeneration(null);
      }

      if (
        data &&
        data.content.length === 1 &&
        page > 0
      ) {
        setPage((current) => current - 1);
      } else {
        await fetchHistory();
      }
    } catch (error: any) {
      setError(
        error.response?.data?.message ||
          "Failed to delete generation"
      );
    }
  };

  return (
    <div>
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">
          Content History
        </h1>

        <p className="mt-2 text-gray-600">
          Search and manage your previous AI content
          generations.
        </p>
      </div>

      {/* Search */}
      <div className="mb-6 rounded-xl border border-gray-200 bg-white p-5 shadow-sm">
        <form
          onSubmit={handleSearch}
          className="flex flex-col gap-3 sm:flex-row"
        >
          <input
            type="text"
            value={searchInput}
            onChange={(e) =>
              setSearchInput(e.target.value)
            }
            placeholder="Search generated content..."
            className="flex-1 rounded-lg border border-gray-300 px-4 py-2.5 outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
          />

          <button
            type="submit"
            className="rounded-lg bg-indigo-600 px-5 py-2.5 text-sm font-medium text-white transition hover:bg-indigo-700"
          >
            Search
          </button>

          {query && (
            <button
              type="button"
              onClick={handleClearSearch}
              className="rounded-lg border border-gray-300 px-5 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50"
            >
              Clear
            </button>
          )}
        </form>

        {query && (
          <p className="mt-3 text-sm text-gray-500">
            Showing results for{" "}
            <span className="font-medium text-gray-700">
              "{query}"
            </span>
          </p>
        )}
      </div>

      {error && (
        <div className="mb-6 rounded-lg bg-red-50 p-4 text-sm text-red-700">
          {error}
        </div>
      )}

      {/* Loading */}
      {loading ? (
        <div className="rounded-xl border border-gray-200 bg-white p-12 text-center shadow-sm">
          <div className="mx-auto h-8 w-8 animate-spin rounded-full border-4 border-gray-200 border-t-indigo-600" />

          <p className="mt-4 text-gray-500">
            Loading your history...
          </p>
        </div>
      ) : !data || data.content.length === 0 ? (
        <div className="rounded-xl border border-gray-200 bg-white p-12 text-center shadow-sm">
          <p className="font-medium text-gray-700">
            {query
              ? "No matching generations found."
              : "No content generations yet."}
          </p>
        </div>
      ) : (
        <>
          {/* History cards */}
          <div className="space-y-4">
            {data.content.map((generation) => (
              <div
                key={generation.id}
                className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm transition hover:shadow-md"
              >
                <div className="flex flex-col justify-between gap-4 sm:flex-row">
                  <div className="min-w-0 flex-1">
                    <div className="mb-3 flex flex-wrap items-center gap-2">
                      <span className="rounded-full bg-indigo-50 px-3 py-1 text-xs font-medium text-indigo-700">
                        {generation.type}
                      </span>

                      <span
                        className={`rounded-full px-3 py-1 text-xs font-medium ${
                          generation.status === "COMPLETED"
                            ? "bg-green-50 text-green-700"
                            : generation.status === "FAILED"
                            ? "bg-red-50 text-red-700"
                            : "bg-yellow-50 text-yellow-700"
                        }`}
                      >
                        {generation.status}
                      </span>
                    </div>

                    <p className="line-clamp-2 font-medium text-gray-900">
                      {generation.input}
                    </p>

                    {generation.output && (
                      <p className="mt-3 line-clamp-2 text-sm leading-6 text-gray-500">
                        {generation.output}
                      </p>
                    )}

                    <p className="mt-4 text-xs text-gray-400">
                      {new Date(
                        generation.createdAt
                      ).toLocaleString()}
                    </p>
                  </div>

                  <div className="flex shrink-0 gap-2 sm:flex-col">
                    <button
                      type="button"
                      onClick={() =>
                        handleView(generation.id)
                      }
                      className="rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 transition hover:bg-gray-50"
                    >
                      View
                    </button>

                    <button
                      type="button"
                      onClick={() =>
                        handleDelete(generation.id)
                      }
                      className="rounded-lg border border-red-200 px-4 py-2 text-sm font-medium text-red-600 transition hover:bg-red-50"
                    >
                      Delete
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Pagination */}
          <div className="mt-6 flex flex-col items-center justify-between gap-4 rounded-xl border border-gray-200 bg-white p-4 sm:flex-row">
            <p className="text-sm text-gray-500">
              {data.totalElements} total generation
              {data.totalElements !== 1 ? "s" : ""}
            </p>

            <div className="flex items-center gap-3">
              <button
                type="button"
                disabled={data.first}
                onClick={() =>
                  setPage((current) => current - 1)
                }
                className="rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-40"
              >
                Previous
              </button>

              <span className="text-sm text-gray-600">
                Page {data.number + 1} of{" "}
                {data.totalPages}
              </span>

              <button
                type="button"
                disabled={data.last}
                onClick={() =>
                  setPage((current) => current + 1)
                }
                className="rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-40"
              >
                Next
              </button>
            </div>
          </div>
        </>
      )}

      {/* Details modal */}
      {selectedGeneration && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="max-h-[85vh] w-full max-w-3xl overflow-y-auto rounded-2xl bg-white p-6 shadow-xl">

            <div className="mb-6 flex items-start justify-between gap-4">
              <div>
                <h2 className="text-xl font-semibold text-gray-900">
                  Generation Details
                </h2>

                <p className="mt-1 text-sm text-gray-500">
                  Generation #{selectedGeneration.id}
                </p>
              </div>

              <button
                type="button"
                onClick={() =>
                  setSelectedGeneration(null)
                }
                className="rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-600 hover:bg-gray-50"
              >
                Close
              </button>
            </div>

            <div className="mb-5 flex flex-wrap gap-2">
              <span className="rounded-full bg-indigo-50 px-3 py-1 text-xs font-medium text-indigo-700">
                {selectedGeneration.type}
              </span>

              <span className="rounded-full bg-green-50 px-3 py-1 text-xs font-medium text-green-700">
                {selectedGeneration.status}
              </span>
            </div>

            <div className="mb-6">
              <p className="mb-2 text-sm font-medium text-gray-500">
                Input
              </p>

              <div className="rounded-lg bg-gray-50 p-4 leading-7 text-gray-700">
                {selectedGeneration.input}
              </div>
            </div>

            <div>
              <div className="mb-2 flex items-center justify-between">
                <p className="text-sm font-medium text-gray-500">
                  AI Output
                </p>

                {selectedGeneration.output && (
                  <button
                    type="button"
                    onClick={() =>
                      navigator.clipboard.writeText(
                        selectedGeneration.output!
                      )
                    }
                    className="text-sm font-medium text-indigo-600 hover:text-indigo-700"
                  >
                    Copy output
                  </button>
                )}
              </div>

              <div className="whitespace-pre-wrap rounded-lg bg-gray-50 p-4 leading-7 text-gray-700">
                {selectedGeneration.output ||
                  "No output available."}
              </div>
            </div>

            <p className="mt-5 text-xs text-gray-400">
              Created{" "}
              {new Date(
                selectedGeneration.createdAt
              ).toLocaleString()}
            </p>
          </div>
        </div>
      )}
    </div>
  );
}

export default ContentHistory;