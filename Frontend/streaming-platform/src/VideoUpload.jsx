import React, { useState, useRef } from "react";
import { Upload, Film, Check, AlertCircle, X } from "lucide-react";
import "./VideoUpload.css";

const VideoUpload = () => {
    const [file, setFile] = useState(null);
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [isPublic, setIsPublic] = useState(true);
    const [status, setStatus] = useState("");
    const [dragActive, setDragActive] = useState(false);
    const [uploading, setUploading] = useState(false);
    const inputRef = useRef();

    const handleDrag = (e) => {
        e.preventDefault();
        e.stopPropagation();
        if (e.type === "dragenter" || e.type === "dragover") {
            setDragActive(true);
        } else if (e.type === "dragleave") {
            setDragActive(false);
        }
    };

    const handleDrop = (e) => {
        e.preventDefault();
        e.stopPropagation();
        setDragActive(false);
        if (e.dataTransfer.files && e.dataTransfer.files[0]) {
            setFile(e.dataTransfer.files[0]);
        }
    };

    const handleFileChange = (e) => {
        if (e.target.files && e.target.files[0]) {
            setFile(e.target.files[0]);
        }
    };

    const handleDragClick = () => {
        inputRef.current.click();
    };

    const removeFile = () => {
        setFile(null);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!file) {
            setStatus("Please select a video file.");
            return;
        }
        
        setUploading(true);
        const formData = new FormData();
        formData.append("file", file);
        formData.append(
            "data",
            JSON.stringify({
                title,
                description,
                isPublic,
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
                const text = await res.text();
                const match = text.match(/Video ID:\s*([\w-]+)/i);
                const videoId = match ? match[1] : null;
                setStatus(videoId ? `Upload successful! Video ID: ${videoId}` : text);
                setFile(null);
                setTitle("");
                setDescription("");
            } else {
                setStatus("Upload failed.");
            }
        } catch (err) {
            setStatus("Error: " + err.message);
        } finally {
            setUploading(false);
        }
    };

    const formatFileSize = (bytes) => {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    };

    return (
        <div className="upload-page">
            <div className="upload-container">
                {/* Header */}
                <div className="upload-header">
                    <div className="header-icon">
                        <Film size={32} />
                    </div>
                    <h1 className="header-title">Upload Video</h1>
                    <p className="header-subtitle">Share your content with the world</p>
                </div>

                {/* Main Upload Card */}
                <div className="upload-card">
                    <div className="upload-form">
                        {/* File Upload Area */}
                        <div
                            className={`drag-drop-area ${dragActive ? 'dragover' : ''}`}
                            onDragEnter={handleDrag}
                            onDragOver={handleDrag}
                            onDragLeave={handleDrag}
                            onDrop={handleDrop}
                            onClick={handleDragClick}
                        >
                            {file ? (
                                <div className="file-selected">
                                    <div className="file-icon success">
                                        <Check size={24} />
                                    </div>
                                    <div className="file-info">
                                        <p className="file-name">{file.name}</p>
                                        <p className="file-size">{formatFileSize(file.size)}</p>
                                    </div>
                                    <button
                                        type="button"
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            removeFile();
                                        }}
                                        className="remove-file-btn"
                                    >
                                        <X size={16} />
                                        Remove
                                    </button>
                                </div>
                            ) : (
                                <div className="file-placeholder">
                                    <div className="file-icon">
                                        <Upload size={24} />
                                    </div>
                                    <div className="file-text">
                                        <p className="primary-text">
                                            Drop your video here, or{" "}
                                            <span className="browse-text">browse</span>
                                        </p>
                                        <p className="secondary-text">
                                            Supports MP4, MOV, AVI and more
                                        </p>
                                    </div>
                                </div>
                            )}
                            <input
                                type="file"
                                accept="video/*"
                                className="file-input"
                                ref={inputRef}
                                onChange={handleFileChange}
                            />
                        </div>

                        {/* Form Fields */}
                        <div className="form-fields">
                            <div className="form-group">
                                <label className="form-label">Title</label>
                                <input
                                    type="text"
                                    value={title}
                                    onChange={(e) => setTitle(e.target.value)}
                                    required
                                    placeholder="Enter video title"
                                    className="form-input"
                                />
                            </div>

                            <div className="form-group">
                                <label className="form-label">Description</label>
                                <textarea
                                    value={description}
                                    onChange={(e) => setDescription(e.target.value)}
                                    required
                                    placeholder="Describe your video"
                                    rows={3}
                                    className="form-textarea"
                                />
                            </div>

                            <div className="form-group checkbox-group">
                                <label className="checkbox-label">
                                    <input
                                        type="checkbox"
                                        checked={isPublic}
                                        onChange={() => setIsPublic(!isPublic)}
                                        className="checkbox-input"
                                    />
                                    <span className="checkbox-text">Make video public</span>
                                </label>
                            </div>
                        </div>

                        {/* Submit Button */}
                        <button
                            onClick={handleSubmit}
                            disabled={uploading || !file}
                            className="upload-btn"
                        >
                            {uploading ? (
                                <div className="btn-content">
                                    <div className="spinner"></div>
                                    <span>Uploading...</span>
                                </div>
                            ) : (
                                <div className="btn-content">
                                    <Upload size={20} />
                                    <span>Upload Video</span>
                                </div>
                            )}
                        </button>
                    </div>

                    {/* Status Message */}
                    {status && (
                        <div className={`status-message ${status.includes('successful') ? 'success' : 'error'}`}>
                            {status.includes('successful') ? (
                                <Check size={20} />
                            ) : (
                                <AlertCircle size={20} />
                            )}
                            <span>{status}</span>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default VideoUpload;