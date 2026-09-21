import { useEffect, useState } from "react";
import api from "../api/axios";

import type { ImageGenerationResponse } from "../types/imageGeneration";
import type { PageResponse } from "../types/pagination";

function ImageHistory() {
  const [data, setData] =
    useState<PageResponse<ImageGenerationResponse> | null>(null);

  const [page, setPage] = useState(0);
  const [searchInput, setSearchInput] = useState("");
  const [query, setQuery] = useState("");

  const [selectedImage, setSelectedImage] =
    useState<ImageGenerationResponse | null>(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const pageSize = 6;

  const fetchImages = async () => {
    setLoading(true);
    setError("");

    try {
      const endpoint = query.trim()
        ? "/images/search"
        : "/images";

      const response = await api.get<
        PageResponse<ImageGenerationResponse>
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
          "Failed to load image history"
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchImages();
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
        await api.get<ImageGenerationResponse>(
          `/images/${id}`
        );

      setSelectedImage(response.data);
    } catch (error: any) {
      setError(
        error.response?.data?.message ||
          "Failed to load image"
      );
    }
  };

  const handleDelete = async (id: number) => {
    const confirmed = window.confirm(
      "Are you sure you want to delete this image?"
    );

    if (!confirmed) return;

    setError("");

    try {
      await api.delete(`/images/${id}`);

      if (selectedImage?.id === id) {
        setSelectedImage(null);
      }

      if (
        data &&
        data.content.length === 1 &&
        page > 0
      ) {
        setPage((current) => current - 1);
      } else {
        await fetchImages();
      }
    } catch (error: any) {
      setError(
        error.response?.data?.message ||
          "Failed to delete image"
      );
    }
  };

  return (
    <div>
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">
          Image History
        </h1>

        <p className="mt-2 text-gray-600">
          Browse and manage your previously generated
          images.
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
            placeholder="Search images by prompt..."
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
            Loading your images...
          </p>
        </div>
      ) : !data || data.content.length === 0 ? (
        <div className="rounded-xl border border-gray-200 bg-white p-12 text-center shadow-sm">
          <p className="font-medium text-gray-700">
            {query
              ? "No matching images found."
              : "No generated images yet."}
          </p>
        </div>
      ) : (
        <>
          {/* Image gallery */}
          <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
            {data.content.map((image) => (
              <div
                key={image.id}
                className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm transition hover:shadow-md"
              >
                {/* Image */}
                <div className="aspect-square bg-gray-100">
                  {image.imageUrl ? (
                    <img
                      src={image.imageUrl}
                      alt={image.prompt}
                      className="h-full w-full object-cover"
                    />
                  ) : (
                    <div className="flex h-full items-center justify-center text-sm text-gray-400">
                      Image unavailable
                    </div>
                  )}
                </div>

                {/* Details */}
                <div className="p-5">
                  <div className="mb-3 flex items-center justify-between gap-3">
                    <span
                      className={`rounded-full px-3 py-1 text-xs font-medium ${
                        image.status === "COMPLETED"
                          ? "bg-green-50 text-green-700"
                          : image.status === "FAILED"
                          ? "bg-red-50 text-red-700"
                          : "bg-yellow-50 text-yellow-700"
                      }`}
                    >
                      {image.status}
                    </span>

                    <span className="text-xs text-gray-400">
                      #{image.id}
                    </span>
                  </div>

                  <p className="line-clamp-2 min-h-12 text-sm leading-6 text-gray-700">
                    {image.prompt}
                  </p>

                  <p className="mt-3 text-xs text-gray-400">
                    {new Date(
                      image.createdAt
                    ).toLocaleString()}
                  </p>

                  <div className="mt-5 flex gap-2">
                    <button
                      type="button"
                      onClick={() =>
                        handleView(image.id)
                      }
                      className="flex-1 rounded-lg border border-gray-300 px-3 py-2 text-sm font-medium text-gray-700 transition hover:bg-gray-50"
                    >
                      View
                    </button>

                    <button
                      type="button"
                      onClick={() =>
                        handleDelete(image.id)
                      }
                      className="flex-1 rounded-lg border border-red-200 px-3 py-2 text-sm font-medium text-red-600 transition hover:bg-red-50"
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
              {data.totalElements} total image
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

      {/* Image details modal */}
      {selectedImage && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="max-h-[90vh] w-full max-w-3xl overflow-y-auto rounded-2xl bg-white p-6 shadow-xl">

            <div className="mb-5 flex items-start justify-between gap-4">
              <div>
                <h2 className="text-xl font-semibold text-gray-900">
                  Image Details
                </h2>

                <p className="mt-1 text-sm text-gray-500">
                  Image #{selectedImage.id}
                </p>
              </div>

              <button
                type="button"
                onClick={() =>
                  setSelectedImage(null)
                }
                className="rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-600 hover:bg-gray-50"
              >
                Close
              </button>
            </div>

            {selectedImage.imageUrl && (
              <div className="overflow-hidden rounded-xl bg-gray-100">
                <img
                  src={selectedImage.imageUrl}
                  alt={selectedImage.prompt}
                  className="mx-auto max-h-[500px] w-full object-contain"
                />
              </div>
            )}

            <div className="mt-5">
              <div className="mb-3 flex items-center gap-2">
                <span
                  className={`rounded-full px-3 py-1 text-xs font-medium ${
                    selectedImage.status === "COMPLETED"
                      ? "bg-green-50 text-green-700"
                      : selectedImage.status === "FAILED"
                      ? "bg-red-50 text-red-700"
                      : "bg-yellow-50 text-yellow-700"
                  }`}
                >
                  {selectedImage.status}
                </span>
              </div>

              <p className="text-sm font-medium text-gray-500">
                Prompt
              </p>

              <p className="mt-2 leading-7 text-gray-700">
                {selectedImage.prompt}
              </p>

              <p className="mt-4 text-xs text-gray-400">
                Created{" "}
                {new Date(
                  selectedImage.createdAt
                ).toLocaleString()}
              </p>
            </div>

            <div className="mt-6 flex flex-wrap gap-3">
              {selectedImage.imageUrl && (
                <>
                  <a
                    href={selectedImage.imageUrl}
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
                        selectedImage.imageUrl!
                      )
                    }
                    className="rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
                  >
                    Copy URL
                  </button>
                </>
              )}

              <button
                type="button"
                onClick={() =>
                  handleDelete(selectedImage.id)
                }
                className="rounded-lg border border-red-200 px-4 py-2 text-sm font-medium text-red-600 hover:bg-red-50"
              >
                Delete Image
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default ImageHistory;