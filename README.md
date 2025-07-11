# Video Streaming Platform 🎬

A comprehensive microservices-based video streaming platform built with Spring Boot and Spring Cloud ecosystem, demonstrating modern distributed system architecture patterns for video processing and adaptive streaming.

## 🏗️ Architecture Overview

This project implements a scalable video streaming platform using microservices architecture with the following components:

- **Service Registry (Eureka Server)** - Service discovery and registration
- **API Gateway** - Single entry point for all client requests with load balancing
- **Video Service** - Handles video upload, metadata management, and user interactions
- **Video Processing Service** - Processes videos into multiple qualities and formats
- **Video Streaming Service** - Delivers adaptive video streaming with HLS
- **React Frontend** - Modern responsive web interface for video upload and playback

## 🎯 Key Features

### Video Platform Capabilities

- **Video Upload & Management** - Support for multiple video formats (MP4, AVI, MOV, WMV, FLV, MKV, WebM)
- **Adaptive Streaming** - Automatic quality adjustment based on network conditions
- **Multi-Quality Processing** - Videos processed into 240p, 360p, 480p, 720p, and 1080p
- **HLS Streaming** - HTTP Live Streaming with 6-second segments for optimal buffering
- **Real-time Processing** - Asynchronous video processing with status updates
- **Metadata Management** - Video details, descriptions, visibility settings, and statistics

### Microservices Patterns Implemented

- **Service Discovery** - Automatic service registration and discovery using Netflix Eureka
- **API Gateway** - Centralized routing, load balancing, and cross-cutting concerns
- **Inter-service Communication** - RESTful APIs with OpenFeign declarative clients
- **Asynchronous Processing** - Background video processing with status callbacks
- **Database Per Service** - PostgreSQL databases for service independence
- **Configuration Management** - Externalized configuration per service

## 🚀 Technologies Used

### Core Technologies

- **Java 21** - Programming language
- **Spring Boot 3.5.3** - Application framework
- **Spring Cloud 2025.0.0** - Microservices framework
- **Maven** - Build and dependency management

### Microservices Stack

- **Netflix Eureka** - Service discovery and registration
- **Spring Cloud Gateway** - API Gateway for routing and load balancing
- **OpenFeign** - Declarative REST client for inter-service communication
- **Spring Cloud Load Balancer** - Client-side load balancing

### Database & Persistence

- **PostgreSQL** - Primary database for all services
- **Spring Data JPA** - Object-relational mapping
- **Hibernate** - ORM framework with automatic DDL updates

### Video Processing & Streaming

- **FFmpeg** - Video processing and transcoding
- **HLS (HTTP Live Streaming)** - Adaptive bitrate streaming protocol
- **Video.js** - HTML5 video player with HLS support

### Frontend Technologies

- **React 19.1.0** - Frontend framework
- **Vite 7.0.4** - Build tool and development server
- **React Router DOM** - Client-side routing
- **Video.js 8.23.3** - Advanced video player

### Development Tools

- **Lombok** - Reduces boilerplate code
- **Spring Boot Starter Test** - Testing framework
- **ESLint** - JavaScript/React code linting

## 🎬 How It Works

### Video Upload Flow

```
1. User uploads video via React frontend
2. API Gateway routes request to Video Service
3. Video Service saves metadata to PostgreSQL
4. Video Service calls Video Processing Service via Feign
5. Video Processing Service transcodes video to multiple qualities
6. Processing creates HLS segments and master playlist
7. Status updates sent back to Video Service
8. Video ready for streaming
```

### Video Streaming Flow

```
1. User requests video via React player
2. API Gateway routes to Video Streaming Service
3. Service serves master playlist (master.m3u8)
4. Player automatically selects quality based on bandwidth
5. Service delivers video segments (.ts files) on demand
6. Player adapts quality in real-time for optimal experience
```

### Quality Levels & Adaptive Streaming

- **240p** - 400k bitrate (mobile/slow connections)
- **360p** - 800k bitrate (standard mobile)
- **480p** - 1200k bitrate (standard definition)
- **720p** - 2500k bitrate (high definition)
- **1080p** - 5000k bitrate (full HD)

Videos are automatically segmented into 6-second chunks, allowing for smooth quality transitions and reduced buffering.

## 📋 Prerequisites

Before running this application, ensure you have:

- **Java 21** or later installed
- **Maven 3.6+** installed
- **PostgreSQL 12+** database server running
- **FFmpeg** installed for video processing
- **Node.js 18+** and npm for frontend development
- **Git** for version control

## 🛠️ Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd "Media Platform"
```

### 2. Database Setup

Create PostgreSQL database:

```sql
-- Create database
CREATE DATABASE "streaming_service";

-- Create user (if not exists)
CREATE USER admin WITH PASSWORD 'admin';
GRANT ALL PRIVILEGES ON DATABASE "streaming_service" TO admin;
```

### 3. Install FFmpeg

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install ffmpeg

# macOS
brew install ffmpeg

# Windows
# Download from https://ffmpeg.org/download.html
```

### 4. Configure Video Storage Directory

Update the path in application.properties files:

```properties
video.upload.directory=/path/to/your/video/storage
video.processing.directory=/path/to/your/video/storage
```

### 5. Build All Services

```bash
# Build all Spring Boot services
mvn clean install
```

### 6. Start Services (in order)

#### Start Service Registry (Port: 8761)

```bash
cd eureka-server
mvn spring-boot:run
```

**Dashboard**: http://localhost:8761

#### Start API Gateway (Port: 8765)

```bash
cd api-gateway
mvn spring-boot:run
```

#### Start Video Service (Port: 8080)

```bash
cd Video-service
mvn spring-boot:run
```

#### Start Video Processing Service (Port: 8090)

```bash
cd Video-Processing-Service
mvn spring-boot:run
```

#### Start Video Streaming Service (Port: 7000)

```bash
cd Video-Streaming
mvn spring-boot:run
```

#### Start React Frontend (Port: 5173)

```bash
cd Frontend/streaming-platform
npm install
npm run dev
```

## 🔗 Service Endpoints & APIs

### Frontend Application

- **URL**: http://localhost:5173
- **Features**: Video upload, player, responsive design

### Service Registry (Eureka Dashboard)

- **URL**: http://localhost:8761
- **Purpose**: Monitor all registered services and their health status

### API Gateway

- **Base URL**: http://localhost:8765
- **Routes**:
  - `/video/**` → Video Service
  - `/stream/**` → Video Streaming Service

### Video Service APIs

- **Base URL**: http://localhost:8080 (direct) or http://localhost:8765/video (via gateway)
- **Key Endpoints**:
  - `POST /video/upload` - Upload video with metadata
  - `PUT /api/videos/{id}/status` - Update processing status
  - `PUT /api/videos/{id}/duration` - Update video duration

### Video Processing Service APIs

- **Base URL**: http://localhost:8090
- **Key Endpoints**:
  - `POST /video/process/init` - Start video processing

### Video Streaming Service APIs

- **Base URL**: http://localhost:7000 (direct) or http://localhost:8765/stream (via gateway)
- **Key Endpoints**:
  - `GET /stream/{videoId}/master.m3u8` - Master playlist for adaptive streaming
  - `GET /stream/{videoId}/{quality}/playlist.m3u8` - Quality-specific playlist
  - `GET /stream/{videoId}/{quality}/{segment}` - Video segments
  - `GET /stream/{videoId}/details` - Video metadata
  - `GET /stream/{videoId}/stream-info` - Streaming information

## 🏗️ Project Structure

```
Media Platform/
├── eureka-server/                 # Service Registry
│   ├── src/main/java/com/moddynerd/eurekaserver/
│   └── pom.xml
├── api-gateway/                   # API Gateway
│   ├── src/main/java/com/moddynerd/apigateway/
│   └── pom.xml
├── Video-service/                 # Video Management Service
│   ├── src/main/java/com/moddynerd/videoservice/
│   │   ├── controller/            # REST Controllers
│   │   ├── service/               # Business Logic
│   │   ├── dao/                   # Data Access Layer
│   │   ├── model/                 # Entity Models
│   │   ├── client/                # Feign Clients
│   │   └── dto/                   # Data Transfer Objects
│   └── pom.xml
├── Video-Processing-Service/      # Video Processing Service
│   ├── src/main/java/com/moddynerd/videoprocessingservice/
│   │   ├── controller/            # REST Controllers
│   │   ├── service/               # Processing Logic
│   │   ├── client/                # Feign Clients
│   │   ├── model/                 # Request/Response Models
│   │   └── utils/                 # Processing Utilities
│   └── pom.xml
├── Video-Streaming/               # Video Streaming Service
│   ├── src/main/java/com/moddynerd/videostreaming/
│   │   ├── controller/            # Streaming Controllers
│   │   ├── service/               # Streaming Logic
│   │   ├── dao/                   # Data Access Layer
│   │   └── model/                 # Entity Models
│   └── pom.xml
├── Frontend/streaming-platform/   # React Frontend
│   ├── src/
│   │   ├── components/            # React Components
│   │   ├── VideoPlayer.jsx        # Video Player Component
│   │   ├── VideoUpload.jsx        # Upload Component
│   │   └── Navbar.jsx             # Navigation Component
│   ├── package.json
│   └── vite.config.js
└── README.md
```

## 🧪 Testing the Platform

### 1. Upload a Video

1. Open http://localhost:5173
2. Click "Upload Video"
3. Fill in title, description, and select video file
4. Click upload and wait for processing

### 2. Watch Video

1. Note the video ID from upload response
2. Go to video player section
3. Enter video ID and click "Load Video"
4. Player will automatically adapt quality based on your connection

### 3. Monitor Services

- Check Eureka Dashboard: http://localhost:8761
- All services should be registered and UP

## 📊 Service Communication Flow

```
React Frontend ←→ API Gateway ←→ Video Service
                      ↓
                Video Processing Service
                      ↓
                Video Streaming Service
                      ↓
                Service Registry (Eureka)
```

### Inter-Service Communication

- **Synchronous**: REST APIs with OpenFeign for immediate responses
- **Asynchronous**: Background video processing with status callbacks
- **Load Balancing**: Client-side load balancing for multiple service instances

## 🔧 Configuration Details

### Database Configuration

All services connect to the same PostgreSQL database but use separate tables:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/streaming_service
spring.datasource.username=admin
spring.datasource.password=admin
```

### File Storage Configuration

```properties
video.upload.directory=${user.home}/Downloads/Java-Practice/Videos-Testing
video.processing.directory=/home/moddynerd/Downloads/Java-Practice/Videos-Testing
```

### Processing Configuration

```properties
video.processing.thread-pool-size=4
spring.servlet.multipart.max-file-size=20000MB
spring.servlet.multipart.max-request-size=20000MB
```

## 📈 Monitoring and Health Checks

- **Eureka Dashboard**: http://localhost:8761 - Service health and registration
- **Gateway Actuator**: Spring Cloud Gateway provides metrics endpoints
- **Service Health**: Each service exposes health endpoints via Spring Boot Actuator
- **Database Monitoring**: JPA show-sql enabled for SQL query monitoring
- **Processing Logs**: Detailed logging for video processing pipeline

## 🔄 Development Workflow

1. **Service Development**: Each service can be developed and tested independently
2. **Service Registration**: Services automatically register with Eureka on startup
3. **API Gateway Routing**: Gateway discovers services and routes requests automatically
4. **Inter-service Communication**: Services communicate via OpenFeign clients with load balancing
5. **Asynchronous Processing**: Video processing happens in background with status updates
6. **Frontend Integration**: React frontend communicates with backend via API Gateway

## 🚧 Future Enhancements

### Phase 2 Features

- **User Authentication & Authorization** - JWT-based security with Spring Security
- **Live Streaming** - Real-time video streaming with WebRTC
- **Video Recommendations** - AI-powered recommendation engine
- **Comments & Interactions** - User engagement features
- **Video Analytics** - View tracking and analytics dashboard

### Technical Improvements

- **Distributed Tracing** - Zipkin/Jaeger for request tracing across services
- **Centralized Configuration** - Spring Cloud Config Server
- **Circuit Breaker** - Resilience4j for fault tolerance
- **API Documentation** - OpenAPI/Swagger documentation
- **Containerization** - Docker and Kubernetes deployment
- **CDN Integration** - Content delivery network for global distribution
- **Caching** - Redis for metadata and session caching
- **Message Queues** - RabbitMQ/Kafka for asynchronous communication
- **Monitoring** - Prometheus and Grafana integration
- **Security** - OAuth2/JWT authentication and HTTPS

### Scalability Features

- **Horizontal Scaling** - Multiple instances of each service
- **Database Sharding** - Scale database horizontally
- **Video CDN** - Distribute video content globally
- **Auto-scaling** - Kubernetes horizontal pod autoscaler
- **Performance Optimization** - Video compression and optimization

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow Spring Boot best practices
- Write unit tests for new features
- Use consistent code formatting
- Update documentation for new APIs
- Test all services integration

## 🎯 Performance Characteristics

### Video Processing

- **Concurrent Processing**: 4 parallel threads for video transcoding
- **Supported Formats**: MP4, AVI, MOV, WMV, FLV, MKV, WebM
- **Processing Speed**: Depends on video length and system resources
- **Storage Efficiency**: Multiple quality levels with optimized bitrates

### Streaming Performance

- **Segment Size**: 6-second segments for optimal buffering
- **Adaptive Quality**: Automatic adjustment based on network conditions
- **Cache Headers**: Optimized caching for video segments
- **Concurrent Streams**: Limited by system resources and database connections

## 👨‍💻 Author

<div align="center">
  <img src="https://avatars.githubusercontent.com/AnimeshNilawar?s=120" alt="Animesh Nilawar" style="border-radius: 50%; border: 3px solid #0366d6;">
  
  **Animesh Nilawar**
  
  *Backend Developer & Microservices Enthusiast*
  
  [![GitHub](https://img.shields.io/badge/GitHub-AnimeshNilawar-black?style=for-the-badge&logo=github)](https://github.com/AnimeshNilawar)
  [![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-blue?style=for-the-badge&logo=linkedin)](https://in.linkedin.com/in/animesh-nilawar)
  [![Email](https://img.shields.io/badge/Email-Contact-red?style=for-the-badge&logo=gmail)](mailto:nilawaranimesh@gmail.com)

  
  ---
  
  💡 *Passionate about building scalable distributed systems and modern web applications*
</div>

## 📄 License

This project is licensed under the **MIT License** – see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Spring Cloud Team** - For excellent microservices framework
- **Netflix OSS** - For pioneering microservices patterns
- **FFmpeg Community** - For powerful video processing capabilities
- **Video.js Team** - For robust HTML5 video player
- **React Community** - For modern frontend development tools
- **PostgreSQL Community** - For reliable database solution

## 📚 Additional Resources

- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Netflix Eureka](https://github.com/Netflix/eureka)
- [FFmpeg Documentation](https://ffmpeg.org/documentation.html)
- [HLS Specification](https://tools.ietf.org/html/rfc8216)
- [Video.js Documentation](https://docs.videojs.com/)
- [React Documentation](https://react.dev/)

---

<div align="center">
  <h3>🎬 "Netflix at Home" - Built with passion for learning microservices architecture! 🎬</h3>
  <p><em>This project serves as a comprehensive learning exercise in microservices architecture and demonstrates practical implementation of modern video streaming platforms.</em></p>
</div>
