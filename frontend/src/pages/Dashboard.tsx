import { Link } from "react-router-dom";

function Dashboard() {
  return (
    <div>
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">
          Dashboard
        </h1>

        <p className="mt-2 text-gray-600">
          Create AI-powered content and images with GenFlow.
        </p>
      </div>

      <div className="grid gap-6 md:grid-cols-2">

        <Link
          to="/generate-content"
          className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm transition hover:shadow-md"
        >
          <h2 className="text-xl font-semibold text-gray-900">
            Generate Content
          </h2>

          <p className="mt-2 text-sm text-gray-600">
            Generate, rewrite, expand, or shorten content
            using AI.
          </p>

          <p className="mt-5 font-medium text-indigo-600">
            Start generating →
          </p>
        </Link>

        <Link
          to="/generate-image"
          className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm transition hover:shadow-md"
        >
          <h2 className="text-xl font-semibold text-gray-900">
            Generate Image
          </h2>

          <p className="mt-2 text-sm text-gray-600">
            Turn your prompts into AI-generated images.
          </p>

          <p className="mt-5 font-medium text-indigo-600">
            Create an image →
          </p>
        </Link>

        <Link
          to="/content-history"
          className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm transition hover:shadow-md"
        >
          <h2 className="text-xl font-semibold text-gray-900">
            Content History
          </h2>

          <p className="mt-2 text-sm text-gray-600">
            Search and manage your previous content
            generations.
          </p>

          <p className="mt-5 font-medium text-indigo-600">
            View history →
          </p>
        </Link>

        <Link
          to="/image-history"
          className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm transition hover:shadow-md"
        >
          <h2 className="text-xl font-semibold text-gray-900">
            Image History
          </h2>

          <p className="mt-2 text-sm text-gray-600">
            Browse and manage your previously generated
            images.
          </p>

          <p className="mt-5 font-medium text-indigo-600">
            View images →
          </p>
        </Link>

      </div>
    </div>
  );
}

export default Dashboard;