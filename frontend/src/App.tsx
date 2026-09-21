import { Routes, Route, Navigate } from "react-router-dom";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import GenerateContent from "./pages/GenerateContent";
import GenerateImage from "./pages/GenerateImage";
import ContentHistory from "./pages/ContentHistory";
import ImageHistory from "./pages/ImageHistory";

import DashboardLayout from "./layouts/DashboardLayout";
import ProtectedRoute from "./routes/ProtectedRoute";

function App() {
  return (
    <Routes>
      <Route
        path="/"
        element={<Navigate to="/login" replace />}
      />

      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route
        element={
          <ProtectedRoute>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/dashboard" element={<Dashboard />} />

        <Route
          path="/generate-content"
          element={<GenerateContent />}
        />

        <Route
          path="/generate-image"
          element={<GenerateImage />}
        />

        <Route
          path="/content-history"
          element={<ContentHistory />}
        />

        <Route
          path="/image-history"
          element={<ImageHistory />}
        />
      </Route>
    </Routes>
  );
}

export default App;