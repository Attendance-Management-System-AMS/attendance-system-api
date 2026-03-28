-- face-api.js FaceRecognitionNet: mảng 128 float lưu dạng JSON
ALTER TABLE employees ADD COLUMN IF NOT EXISTS face_embedding TEXT;
