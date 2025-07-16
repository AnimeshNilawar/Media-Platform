import React, { useEffect, useRef, useState } from 'react';
import { useLocation } from 'react-router-dom';
import './VideoPlayer.css';

const VideoPlayer = () => {
    const videoRef = useRef(null);
    const playerRef = useRef(null);
    const containerRef = useRef(null);
    const location = useLocation();
    const [videoId, setVideoId] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [videoDetails, setVideoDetails] = useState(null);
    const [isVideoLoaded, setIsVideoLoaded] = useState(false);
    const [aspectRatio, setAspectRatio] = useState('16:9'); // Used only for initial setup
    const [videoJsLoaded, setVideoJsLoaded] = useState(false);

    // Extract videoId from URL query param
    useEffect(() => {
        const params = new URLSearchParams(location.search);
        const id = params.get('id') || '';
        setVideoId(id);
    }, [location.search]);

    // Load Video.js only once
    useEffect(() => {
        const loadVideoJS = () => {
            if (window.videojs) {
                setVideoJsLoaded(true);
                return;
            }

            // Load CSS
            const link = document.createElement('link');
            link.rel = 'stylesheet';
            link.href = 'https://vjs.zencdn.net/8.2.0/video-js.css';
            document.head.appendChild(link);

            // Load JS
            const script = document.createElement('script');
            script.src = 'https://vjs.zencdn.net/8.2.0/video.js';
            script.onload = () => {
                setVideoJsLoaded(true);
            };
            document.head.appendChild(script);
        };

        loadVideoJS();
    }, []);

    // Initialize player only when Video.js is loaded
    useEffect(() => {
        if (!videoJsLoaded || !videoRef.current) return;

        // Dispose existing player if it exists
        if (playerRef.current) {
            playerRef.current.dispose();
            playerRef.current = null;
        }

        playerRef.current = window.videojs(videoRef.current, {
            fluid: true,
            responsive: true,
            aspectRatio: aspectRatio,
            html5: {
                hls: {
                    enableLowInitialPlaylist: true,
                    smoothQualityChange: true
                }
            },
            playbackRates: [0.5, 1, 1.25, 1.5, 2],
            controls: true,
            preload: 'auto'
        });

        playerRef.current.ready(() => {
            console.log('Player is ready');

            // Listen for video metadata to determine aspect ratio
            playerRef.current.on('loadedmetadata', () => {
                const videoElement = playerRef.current.el().querySelector('video');
                if (videoElement) {
                    const { videoWidth, videoHeight } = videoElement;
                    console.log('Video dimensions:', videoWidth, 'x', videoHeight);

                    const ratio = videoWidth / videoHeight;
                    // Determine if video is portrait or landscape
                    const isPortrait = ratio < 1;

                    // Only update container class, do NOT re-initialize player
                    if (containerRef.current) {
                        if (isPortrait) {
                            containerRef.current.classList.add('portrait-video');
                            containerRef.current.classList.remove('landscape-video');
                        } else {
                            containerRef.current.classList.add('landscape-video');
                            containerRef.current.classList.remove('portrait-video');
                        }
                    }
                }
            });

            // Handle errors
            playerRef.current.on('error', (error) => {
                console.error('Video player error:', error);
            });
        });

        // Cleanup function
        return () => {
            if (playerRef.current) {
                try {
                    playerRef.current.dispose();
                    playerRef.current = null;
                } catch (error) {
                    console.error('Error disposing player:', error);
                }
            }
        };
    }, [aspectRatio, videoJsLoaded]); // Remove aspectRatio from dependency array

    const getAuthHeaders = () => {
        const token = localStorage.getItem('token');
        return token ? { 'Authorization': `Bearer ${token}` } : {};
    };

    const loadVideoDetails = async (videoId) => {
        try {
            const response = await fetch(`http://localhost:8765/stream/${videoId}/details`, {
                headers: {
                    ...getAuthHeaders()
                }
            });
            if (response.ok) {
                const details = await response.json();
                setVideoDetails(details);
                return details;
            } else {
                console.warn('Could not load video details');
                return null;
            }
        } catch (error) {
            console.error('Error loading video details:', error);
            return null;
        }
    };

    const formatViewCount = (count) => {
        const num = parseInt(count);
        if (num >= 1000000) {
            return (num / 1000000).toFixed(1) + 'M';
        } else if (num >= 1000) {
            return (num / 1000).toFixed(1) + 'K';
        }
        return num.toString();
    };

    const formatDate = (dateString) => {
        const date = new Date(dateString);
        const now = new Date();
        const diffTime = Math.abs(now - date);
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

        if (diffDays === 1) {
            return '1 day ago';
        } else if (diffDays < 30) {
            return `${diffDays} days ago`;
        } else if (diffDays < 365) {
            const months = Math.floor(diffDays / 30);
            return `${months} month${months > 1 ? 's' : ''} ago`;
        } else {
            const years = Math.floor(diffDays / 365);
            return `${years} year${years > 1 ? 's' : ''} ago`;
        }
    };

    const formatDuration = (seconds) => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}:${secs.toString().padStart(2, '0')}`;
    };

    const loadVideo = async () => {
        const trimmedVideoId = videoId.trim();
        if (!trimmedVideoId) {
            return;
        }

        setIsLoading(true);
        setVideoDetails(null);
        setIsVideoLoaded(false);

        try {
            // Check if video is ready
            const response = await fetch(`http://localhost:8765/stream/${trimmedVideoId}/stream-info`, {
                headers: {
                    ...getAuthHeaders()
                }
            });

            if (!response.ok) {
                throw new Error('Video not found or not ready');
            }

            const info = await response.json();

            if (info.status === 'ready') {
                // Load video details
                await loadVideoDetails(trimmedVideoId);

                // Load the master playlist
                const masterPlaylistUrl = `http://localhost:8765/stream/${trimmedVideoId}/master.m3u8`;

                if (playerRef.current) {
                    playerRef.current.src({
                        src: masterPlaylistUrl,
                        type: 'application/x-mpegURL'
                    });
                    console.log('Loading video:', masterPlaylistUrl);
                    setIsVideoLoaded(true);
                }
            } else {
                alert('Video is still processing. Please try again later.');
            }
        } catch (error) {
            console.error('Error loading video:', error);
            alert('Error loading video: ' + error.message);
        } finally {
            setIsLoading(false);
        }
    };

    // Auto-load video when videoId changes
    useEffect(() => {
        if (videoId && videoJsLoaded) {
            loadVideo();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [videoId, videoJsLoaded]);

    return (
        <div className="video-player-container">
            {isLoading && (
                <div className="loading-indicator">
                    <div className="spinner"></div>
                    <span>Loading video...</span>
                </div>
            )}

            <div className="video-wrapper">
                <div
                    ref={containerRef}
                    className={`video-container ${aspectRatio === '9:16' ? 'portrait-video' : 'landscape-video'}`}
                >
                    <video
                        ref={videoRef}
                        className="video-js vjs-default-skin"
                        controls
                        preload="auto"
                        data-setup="{}"
                        playsInline
                        webkit-playsinline="true"
                    >
                        <p className="vjs-no-js">
                            To view this video please enable JavaScript, and consider upgrading to a web browser that{' '}
                            <a href="https://videojs.com/html5-video-support/" target="_blank" rel="noopener noreferrer">
                                supports HTML5 video
                            </a>.
                        </p>
                    </video>
                </div>

                {/* Video Details Section */}
                {isVideoLoaded && videoDetails && (
                    <div className="video-details">
                        <div className="video-info">
                            <h2 className="video-title">{videoDetails.title}</h2>
                            <div className="video-meta">
                                <span className="video-views">
                                    <svg className="icon" viewBox="0 0 24 24" fill="currentColor">
                                        <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z" />
                                    </svg>
                                    {formatViewCount(videoDetails.viewCount)} views
                                </span>
                                <span className="video-date">
                                    <svg className="icon" viewBox="0 0 24 24" fill="currentColor">
                                        <path d="M9 11H7v2h2v-2zm4 0h-2v2h2v-2zm4 0h-2v2h2v-2zm2-7h-1V2h-2v2H8V2H6v2H5c-1.1 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V9h14v11z" />
                                    </svg>
                                    {formatDate(videoDetails.publishedAt)}
                                </span>
                                <span className="video-duration">
                                    <svg className="icon" viewBox="0 0 24 24" fill="currentColor">
                                        <path d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z" />
                                    </svg>
                                    {formatDuration(parseInt(videoDetails.duration))}
                                </span>
                            </div>
                        </div>

                        <div className="video-engagement">
                            <div className="engagement-buttons">
                                <button className="like-btn">
                                    <svg className="icon" viewBox="0 0 24 24" fill="currentColor">
                                        <path d="M1 21h4V9H1v12zm22-11c0-1.1-.9-2-2-2h-6.31l.95-4.57.03-.32c0-.41-.17-.79-.44-1.06L14.17 1 7.59 7.59C7.22 7.95 7 8.45 7 9v10c0 1.1.9 2 2 2h9c.83 0 1.54-.5 1.84-1.22l3.02-7.05c.09-.23.14-.47.14-.73v-1.91l-.01-.01L23 10z" />
                                    </svg>
                                    {formatViewCount(videoDetails.likeCount)}
                                </button>
                                <button className="dislike-btn">
                                    <svg className="icon" viewBox="0 0 24 24" fill="currentColor">
                                        <path d="M15 3H6c-.83 0-1.54.5-1.84 1.22l-3.02 7.05c-.09.23-.14.47-.14.73v1.91l.01.01L1 14c0 1.1.9 2 2 2h6.31l-.95 4.57-.03.32c0 .41.17.79.44 1.06L9.83 23l6.59-6.59c.36-.36.58-.86.58-1.41V5c0-1.1-.9-2-2-2zm4 0v12h4V3h-4z" />
                                    </svg>
                                    {formatViewCount(videoDetails.dislikeCount)}
                                </button>
                                <button className="share-btn">
                                    <svg className="icon" viewBox="0 0 24 24" fill="currentColor">
                                        <path d="M18 16.08c-.76 0-1.44.3-1.96.77L8.91 12.7c.05-.23.09-.46.09-.7s-.04-.47-.09-.7l7.05-4.11c.54.5 1.25.81 2.04.81 1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3c0 .24.04.47.09.7L8.04 9.81C7.5 9.31 6.79 9 6 9c-1.66 0-3 1.34-3 3s1.34 3 3 3c.79 0 1.5-.31 2.04-.81l7.12 4.16c-.05.21-.08.43-.08.65 0 1.61 1.31 2.92 2.92 2.92 1.61 0 2.92-1.31 2.92-2.92s-1.31-2.92-2.92-2.92z" />
                                    </svg>
                                    Share
                                </button>
                            </div>
                        </div>

                        <div className="video-description">
                            <h4>Description</h4>
                            <p>{videoDetails.description}</p>
                            <div className="video-stats">
                                <div className="stat-item">
                                    <strong>Video ID:</strong> {videoDetails.id}
                                </div>
                                <div className="stat-item">
                                    <strong>Comments:</strong> {videoDetails.commentCount}
                                </div>
                                <div className="stat-item">
                                    <strong>Visibility:</strong>
                                    <span className={`visibility-badge ${videoDetails.isPublic ? 'public' : 'private'}`}>
                                        {videoDetails.isPublic ? 'Public' : 'Private'}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                )}
            </div>

            <div className="quality-info">
                <h3>How it works:</h3>
                <div className="info-grid">
                    <div className="info-item">
                        <div className="info-icon">⚡</div>
                        <div className="info-text">
                            <strong>Smart Segmentation</strong>
                            <p>Videos are automatically segmented into 6-second chunks for optimal streaming</p>
                        </div>
                    </div>
                    <div className="info-item">
                        <div className="info-icon">🎯</div>
                        <div className="info-text">
                            <strong>Adaptive Quality</strong>
                            <p>Multiple quality levels (240p, 360p, 720p, 1080p) with automatic switching</p>
                        </div>
                    </div>
                    <div className="info-item">
                        <div className="info-icon">📱</div>
                        <div className="info-text">
                            <strong>Responsive Design</strong>
                            <p>Optimized for both portrait and landscape videos on all devices</p>
                        </div>
                    </div>
                    <div className="info-item">
                        <div className="info-icon">💾</div>
                        <div className="info-text">
                            <strong>Efficient Loading</strong>
                            <p>Only segments being watched are downloaded, saving bandwidth</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default VideoPlayer;