import React, { useState } from "react";
import "./App.css";

const VideoUpload = () => {
    const [file, setFile] = useState(null);
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [isPublic, setIsPublic] = useState(true);
    const [channelId, setChannelId] = useState("");
    const [uploaderId, setUploaderId] = useState("");
    const [status, setStatus] = useState("");

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!file) {
            setStatus("Please select a video file.");
            return;
        }
        const formData = new FormData();
        formData.append("file", file);
        formData.append(
            "data",
            JSON.stringify({
                title,
                description,
                isPublic,
                channelId,
                uploaderId,
            })
        );
        try {
            const token = localStorage.getItem('token');
            const res = await fetch("http://localhost:8765/video/upload", {
                method: "POST",
                body: formData,
                headers: token ? { Authorization: `Bearer ${token}` } : {},
            });
            if (res.ok) {
                setStatus("Upload successful!");
            } else {
                setStatus("Upload failed.");
            }
        } catch (err) {
            setStatus("Error: " + err.message);
        }
    };

    return (
        <div className="upload-container">
            <h2>Upload Video</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label>Video File:</label>
                    <input
                        type="file"
                        accept="video/*"
                        onChange={(e) => setFile(e.target.files[0])}
                        required
                    />
                </div>
                <div>
                    <label>Title:</label>
                    <input
                        type="text"
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label>Description:</label>
                    <textarea
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label>Public:</label>
                    <input
                        type="checkbox"
                        checked={isPublic}
                        onChange={() => setIsPublic((v) => !v)}
                    />
                </div>
                <div>
                    <label>Channel ID:</label>
                    <input
                        type="text"
                        value={channelId}
                        onChange={(e) => setChannelId(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label>Uploader ID:</label>
                    <input
                        type="text"
                        value={uploaderId}
                        onChange={(e) => setUploaderId(e.target.value)}
                        required
                    />
                </div>
                <button type="submit">Upload</button>
            </form>
            {status && <p>{status}</p>}
        </div>
    );
};

export default VideoUpload;
