import React, { useEffect, useRef, useState } from 'react';
import { useLocation } from 'react-router-dom';
import './VideoPlayer.css';

const VideoPlayer = () => {
    const videoRef = useRef(null);
    const playerRef = useRef(null);
    const location = useLocation();
    const [videoId, setVideoId] = useState('');
    const [, setIsLoading] = useState(false);
    const [videoDetails, setVideoDetails] = useState(null);
    const [isVideoLoaded, setIsVideoLoaded] = useState(false);

    // Extract videoId from URL query param
    useEffect(() => {
        const params = new URLSearchParams(location.search);
        const id = params.get('id') || '';
        setVideoId(id);
    }, [location.search]);

    useEffect(() => {
        // Load Video.js script and CSS
        const loadVideoJS = () => {
            // Load CSS
            const link = document.createElement('link');
            link.rel = 'stylesheet';
            link.href = 'https://vjs.zencdn.net/8.2.0/video-js.css';
            document.head.appendChild(link);

            // Load JS
            const script = document.createElement('script');
            script.src = 'https://vjs.zencdn.net/8.2.0/video.js';
            script.onload = () => {
                initializePlayer();
            };
            document.head.appendChild(script);
        };

        const initializePlayer = () => {
            if (window.videojs && videoRef.current) {
                playerRef.current = window.videojs(videoRef.current, {
                    fluid: true,
                    responsive: true,
                    html5: {
                        hls: {
                            enableLowInitialPlaylist: true,
                            smoothQualityChange: true
                        }
                    }
                });

                playerRef.current.ready(() => {
                    console.log('Player is ready');
                });
            }
        };

        // Check if Video.js is already loaded
        if (window.videojs) {
            initializePlayer();
        } else {
            loadVideoJS();
        }

        // Cleanup
        return () => {
            if (playerRef.current) {
                playerRef.current.dispose();
            }
        };
    }, []);

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
        if (videoId) {
            loadVideo();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [videoId]);

    return (
        <div className="video-player-container">
            <h1>Video Player</h1>
            {/* Video ID is now taken from URL, no input box */}

            <div className="video-container">
                <video
                    ref={videoRef}
                    className="video-js vjs-default-skin"
                    controls
                    preload="auto"
                    width="800"
                    height="450"
                    data-setup="{}"
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
                            <span className="video-views">{formatViewCount(videoDetails.viewCount)} views</span>
                            <span className="video-date">
                                {formatDate(videoDetails.publishedAt)}
                            </span>
                            <span className="video-duration">Duration: {formatDuration(parseInt(videoDetails.duration))}</span>
                        </div>
                    </div>

                    <div className="video-engagement">
                        <div className="engagement-buttons">
                            <button className="like-btn">
                                👍 {formatViewCount(videoDetails.likeCount)}
                            </button>
                            <button className="dislike-btn">
                                👎 {formatViewCount(videoDetails.dislikeCount)}
                            </button>
                            <button className="share-btn">
                                📤 Share
                            </button>
                        </div>
                    </div>

                    <div className="video-description">
                        <h4>Description</h4>
                        <p>{videoDetails.description}</p>
                        <div className="video-stats">
                            <p><strong>Video ID:</strong> {videoDetails.id}</p>
                            <p><strong>Comments:</strong> {videoDetails.commentCount}</p>
                            <p><strong>Visibility:</strong> {videoDetails.isPublic ? 'Public' : 'Private'}</p>
                        </div>
                    </div>
                </div>
            )}

            <div className="quality-info">
                <h3>How it works:</h3>
                <ul>
                    <li>Videos are automatically segmented into 6-second chunks</li>
                    <li>Multiple quality levels are available (240p, 360p, 720p, 1080p)</li>
                    <li>Player automatically switches quality based on network conditions</li>
                    <li>Only segments being watched are downloaded</li>
                </ul>
            </div>
        </div>
    );
};

export default VideoPlayer;
