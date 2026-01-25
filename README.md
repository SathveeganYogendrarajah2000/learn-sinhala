# Learn Sinhala

A web application designed to help Tamil speakers learn spoken Sinhala through interactive vocabulary practice, spaced repetition, and sentence building.

## Purpose

This project addresses the need for Tamil speakers in Sri Lanka (and elsewhere) to learn conversational Sinhala. Unlike traditional language learning apps that focus on formal written language, this app emphasizes:

- **Spoken Sinhala** - Real phrases used in daily Sri Lankan life
- **Romanized text** - No Sinhala script required; learn to speak, not read
- **Tamil translations** - Leverage your Tamil knowledge to learn faster
- **Practical vocabulary** - Shopping, travel, food, greetings, emergencies

## Tech Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Runtime |
| Spring Boot | 3.2.x | Web framework |
| Spring Security | 6.x | Authentication |
| Spring Data MongoDB | - | Database access |
| JWT (jjwt) | 0.12.x | Token authentication |
| Lombok | - | Boilerplate reduction |
| Maven | 3.9+ | Build tool |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| Angular | 18.x | UI framework |
| TypeScript | 5.x | Language |
| RxJS | 7.x | Reactive programming |
| Angular Signals | - | State management |

### Database
| Technology | Purpose |
|------------|---------|
| MongoDB | Document storage |

## Project Structure

```
learn-sinhala/
├── backend/                 # Spring Boot API
│   ├── src/main/java/
│   │   └── com/learnsinhala/
│   │       ├── config/      # Security, CORS, data seeding
│   │       ├── controller/  # REST endpoints
│   │       ├── model/       # MongoDB documents
│   │       ├── repository/  # Data access
│   │       ├── security/    # JWT authentication
│   │       └── service/     # Business logic
│   └── src/main/resources/
│       └── application.yml  # Configuration
│
├── frontend/                # Angular SPA
│   └── src/
│       ├── app/
│       │   ├── core/        # Services, guards, interceptors
│       │   ├── features/    # Page components
│       │   └── shared/      # Reusable components
│       └── environments/    # Environment configs
│
├── storage/                 # Local file storage (audio)
│   └── audio/
│
└── README.md
```

## Prerequisites

- **Java 21** - [Download](https://adoptium.net/)
- **Node.js 20+** - [Download](https://nodejs.org/)
- **MongoDB 7+** - [Download](https://www.mongodb.com/try/download/community) or use [MongoDB Atlas](https://www.mongodb.com/atlas)
- **Maven 3.9+** - [Download](https://maven.apache.org/download.cgi)

## Running the Backend

### 1. Start MongoDB

```bash
# Local MongoDB
mongod --dbpath /path/to/data

# Or use Docker
docker run -d -p 27017:27017 --name mongodb mongo:7
```

### 2. Configure Environment

Create or edit `backend/src/main/resources/application.yml`:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/learnsinhala
  profiles:
    active: dev  # Enables data seeding

jwt:
  secret: your-256-bit-secret-key-here-make-it-long-and-random
  expiration: 86400000  # 24 hours

storage:
  local:
    path: ../storage
```

### 3. Run the Application

```bash
cd backend

# Using Maven
./mvnw spring-boot:run

# Or build and run JAR
./mvnw clean package
java -jar target/learn-sinhala-0.0.1-SNAPSHOT.jar
```

The API will be available at `http://localhost:8080/api`

### 4. Verify Setup

```bash
# Health check
curl http://localhost:8080/api/health

# Register a user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123","displayName":"Test User"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

## Running the Frontend

### 1. Install Dependencies

```bash
cd frontend
npm install
```

### 2. Configure Environment

Edit `frontend/src/environments/environment.ts` if needed:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

### 3. Start Development Server

```bash
npm start
# or
ng serve
```

The app will be available at `http://localhost:4200`

### 4. Build for Production

```bash
npm run build
# Output in frontend/dist/
```

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login and get JWT |

### Vocabulary
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/vocabulary` | List vocabulary (with filters) |
| GET | `/api/vocabulary/:id` | Get single item |
| GET | `/api/vocabulary/categories` | List categories |
| POST | `/api/vocabulary/:id/progress` | Update learning progress |

### Practice
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/practice/today` | Get today's practice session |
| POST | `/api/practice/answer` | Submit practice answer |

### User
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/me` | Get current user profile |

## Features

### Current Features

- **User Authentication** - Register, login, JWT-based sessions
- **Vocabulary Browser** - Filter by category and difficulty
- **Flashcard Practice** - Spaced repetition learning
- **Progress Tracking** - Track mastered vs learning words
- **Sentence Builder** - Build sentences from patterns
- **Audio Support** - Pronunciation audio playback
- **Keyboard Navigation** - Space to reveal, 1/2 to rate

### Learning Flow

1. **Browse Vocabulary** - Explore words by category
2. **Daily Practice** - Review due words + learn new ones
3. **Rate Knowledge** - "Got it" or "Didn't know"
4. **Spaced Repetition** - Algorithm schedules optimal review times
5. **Build Sentences** - Practice with sentence patterns

## Roadmap

### Phase 1: Core Features (Current)
- [x] User authentication
- [x] Vocabulary management
- [x] Flashcard practice
- [x] Spaced repetition algorithm
- [x] Sentence builder
- [x] 50 common phrases seed data

### Phase 2: Enhanced Learning
- [ ] Audio recording for pronunciation comparison
- [ ] Quiz mode with multiple choice
- [ ] Daily streaks and achievements
- [ ] Lesson-based curriculum
- [ ] Grammar tips and explanations

### Phase 3: Social Features
- [ ] Leaderboards
- [ ] Share progress on social media
- [ ] Community-contributed phrases
- [ ] Native speaker audio contributions

### Phase 4: Mobile & Offline
- [ ] Progressive Web App (PWA)
- [ ] Offline mode with sync
- [ ] Native mobile app (Capacitor/React Native)
- [ ] Push notifications for practice reminders

### Phase 5: Advanced Features
- [ ] AI-powered pronunciation feedback
- [ ] Conversation practice with AI
- [ ] Regional dialect variations
- [ ] Integration with Tamil-Sinhala dictionary API

## Development

### Backend Development

```bash
cd backend

# Run tests
./mvnw test

# Run with hot reload
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=true"

# Generate API docs (if Swagger added)
./mvnw springdoc-openapi:generate
```

### Frontend Development

```bash
cd frontend

# Run tests
npm test

# Run e2e tests
npm run e2e

# Lint
npm run lint

# Format
npm run format
```

### Code Style

- **Backend**: Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- **Frontend**: ESLint + Prettier (configured)

## Environment Variables

### Backend (application.yml or env vars)

| Variable | Description | Default |
|----------|-------------|---------|
| `MONGODB_URI` | MongoDB connection string | `mongodb://localhost:27017/learnsinhala` |
| `JWT_SECRET` | Secret key for JWT signing | (required) |
| `JWT_EXPIRATION` | Token expiration in ms | `86400000` |
| `STORAGE_PATH` | Local file storage path | `../storage` |

### Frontend (environment.ts)

| Variable | Description | Default |
|----------|-------------|---------|
| `apiUrl` | Backend API URL | `http://localhost:8080/api` |
| `production` | Production mode flag | `false` |

## Troubleshooting

### MongoDB Connection Issues

```bash
# Check if MongoDB is running
mongosh --eval "db.adminCommand('ping')"

# Check connection string in application.yml
```

### CORS Errors

Ensure backend CORS config includes your frontend URL:
```yaml
# application.yml
cors:
  allowed-origins: http://localhost:4200
```

### JWT Token Issues

- Check token expiration time
- Ensure secret key is consistent
- Clear browser localStorage and re-login

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is for personal/educational use. See [LICENSE](LICENSE) for details.

## Acknowledgments

- Sri Lankan Sinhala speakers who provided authentic phrases
- Tamil-speaking community for feedback on translations
- Open source community for the amazing tools

---

**Learn to speak Sinhala, one phrase at a time!**
