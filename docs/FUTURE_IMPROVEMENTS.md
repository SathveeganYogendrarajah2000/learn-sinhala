# Future Improvements Design Document

This document outlines the technical design for four major feature enhancements. These are proposals only - not yet implemented.

---

## Table of Contents

1. [Speech Recognition](#1-speech-recognition)
2. [Mobile App](#2-mobile-app)
3. [Offline Mode](#3-offline-mode)
4. [Sinhala Slang Mode](#4-sinhala-slang-mode)

---

## 1. Speech Recognition

### Overview

Enable users to practice pronunciation by speaking Sinhala phrases and receiving feedback on accuracy.

### User Stories

- As a learner, I want to speak a phrase and see how accurate my pronunciation is
- As a learner, I want to hear the correct pronunciation compared to mine
- As a learner, I want specific feedback on which syllables I mispronounced

### Technical Architecture

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   Frontend      │────▶│   Backend API    │────▶│  Speech API     │
│   (Angular)     │     │   (Spring Boot)  │     │  (Google/Azure) │
└─────────────────┘     └──────────────────┘     └─────────────────┘
        │                        │                        │
        ▼                        ▼                        ▼
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Web Audio API  │     │  Audio Storage   │     │  Transcription  │
│  MediaRecorder  │     │  (S3/Local)      │     │  + Scoring      │
└─────────────────┘     └──────────────────┘     └─────────────────┘
```

### Frontend Components

```typescript
// speech-practice.component.ts
interface SpeechPracticeState {
  status: 'idle' | 'recording' | 'processing' | 'result';
  targetPhrase: string;
  recordedAudio: Blob | null;
  result: PronunciationResult | null;
}

interface PronunciationResult {
  overallScore: number;          // 0-100
  transcribedText: string;       // What was heard
  wordScores: WordScore[];       // Per-word breakdown
  feedback: string;              // Human-readable feedback
  audioComparison: {
    userAudioUrl: string;
    referenceAudioUrl: string;
  };
}

interface WordScore {
  word: string;
  expected: string;
  score: number;
  phonemeErrors: PhonemeError[];
}
```

### Backend API Design

```
POST /api/speech/analyze
  Request: multipart/form-data
    - audio: Blob (webm/wav)
    - vocabularyId: string
    - expectedText: string

  Response: PronunciationResult

GET /api/speech/history
  Response: PronunciationAttempt[]

POST /api/speech/reference/{vocabularyId}
  Upload native speaker reference audio (admin only)
```

### Speech Service Design

```java
@Service
public class SpeechAnalysisService {

    private final SpeechToTextProvider speechProvider;
    private final PronunciationScorer scorer;

    public PronunciationResult analyze(
        byte[] audioData,
        String expectedText,
        String vocabularyId
    ) {
        // 1. Transcribe audio to text
        TranscriptionResult transcription = speechProvider.transcribe(
            audioData,
            "si-LK"  // Sinhala-Sri Lanka locale
        );

        // 2. Compare with expected text
        TextComparison comparison = scorer.compare(
            transcription.getText(),
            expectedText
        );

        // 3. Analyze phoneme-level accuracy (if supported)
        PhonemeAnalysis phonemes = speechProvider.getPhonemeDetails(
            transcription
        );

        // 4. Generate score and feedback
        return buildResult(comparison, phonemes);
    }
}
```

### Speech Provider Options

| Provider | Pros | Cons | Cost |
|----------|------|------|------|
| **Google Cloud Speech** | Best Sinhala support, phoneme timing | Complex pricing | $0.006/15s |
| **Azure Speech** | Pronunciation assessment API | Limited Sinhala | $1/audio hour |
| **AWS Transcribe** | Good accuracy | No Sinhala support yet | $0.024/min |
| **Whisper (OpenAI)** | Self-hostable, multilingual | No real-time, no phonemes | Free (self-host) |

**Recommendation**: Start with Google Cloud Speech for best Sinhala language support.

### Pronunciation Scoring Algorithm

```typescript
function calculatePronunciationScore(
  expected: string,
  transcribed: string,
  phonemeData?: PhonemeData
): number {
  // 1. Word-level Levenshtein distance (40% weight)
  const wordScore = 1 - (levenshtein(expected, transcribed) / expected.length);

  // 2. Phoneme accuracy if available (40% weight)
  const phonemeScore = phonemeData
    ? phonemeData.correctPhonemes / phonemeData.totalPhonemes
    : wordScore;

  // 3. Fluency - speaking pace and pauses (20% weight)
  const fluencyScore = phonemeData
    ? calculateFluency(phonemeData.timing)
    : 0.8; // Default if no timing data

  return Math.round(
    (wordScore * 0.4 + phonemeScore * 0.4 + fluencyScore * 0.2) * 100
  );
}
```

### UI/UX Design

```
┌────────────────────────────────────────┐
│  Practice: "Ayubowan"                  │
│                                        │
│  ┌────────────────────────────────┐   │
│  │  🔊 Listen to correct          │   │
│  │     pronunciation              │   │
│  └────────────────────────────────┘   │
│                                        │
│  ┌────────────────────────────────┐   │
│  │                                │   │
│  │      🎤 Hold to Record         │   │
│  │                                │   │
│  └────────────────────────────────┘   │
│                                        │
│  ─────────────────────────────────    │
│                                        │
│  Your Score: 78/100                    │
│                                        │
│  ┌────────────────────────────────┐   │
│  │ A-yu-bo-wan                    │   │
│  │ ✓   ✓  ⚠   ✓                  │   │
│  │       └─ Try "bo" not "ba"     │   │
│  └────────────────────────────────┘   │
│                                        │
│  [Try Again]  [Next Word]             │
└────────────────────────────────────────┘
```

### Data Model

```java
@Document(collection = "pronunciation_attempts")
public class PronunciationAttempt {
    @Id
    private String id;
    private String userId;
    private String vocabularyId;
    private String expectedText;
    private String transcribedText;
    private int score;
    private String audioStoragePath;
    private List<WordScore> wordScores;
    private Instant attemptedAt;
}
```

### Privacy Considerations

- Audio recordings stored temporarily (24h) then deleted
- Option to disable audio storage entirely
- No audio sent to third parties without consent
- GDPR compliance: user can request all audio deletion

---

## 2. Mobile App

### Overview

Native mobile experience using a hybrid approach (shared codebase) with platform-specific optimizations.

### Technology Options

| Approach | Pros | Cons | Recommendation |
|----------|------|------|----------------|
| **Capacitor + Angular** | Reuse existing code | Limited native feel | Best for MVP |
| **React Native** | Native performance | Rewrite frontend | Good for scale |
| **Flutter** | Single codebase, fast | New language (Dart) | Consider later |
| **Native (Swift/Kotlin)** | Best performance | Two codebases | Not recommended |

**Recommendation**: Capacitor with existing Angular app for fastest time-to-market.

### Capacitor Integration

```
┌─────────────────────────────────────────────────┐
│                  Angular App                     │
│              (Existing Frontend)                 │
└─────────────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────┐
│               Capacitor Bridge                   │
│  ┌──────────┐ ┌──────────┐ ┌──────────────┐    │
│  │ Storage  │ │  Audio   │ │ Notifications│    │
│  │ Plugin   │ │ Plugin   │ │    Plugin    │    │
│  └──────────┘ └──────────┘ └──────────────┘    │
└─────────────────────────────────────────────────┘
          │                    │
          ▼                    ▼
┌──────────────────┐  ┌──────────────────┐
│    iOS App       │  │   Android App    │
│   (Swift/ObjC)   │  │   (Kotlin/Java)  │
└──────────────────┘  └──────────────────┘
```

### Project Structure

```
learn-sinhala/
├── frontend/                 # Existing Angular app
├── mobile/
│   ├── capacitor.config.ts   # Capacitor configuration
│   ├── ios/                  # iOS native project
│   │   └── App/
│   ├── android/              # Android native project
│   │   └── app/
│   └── resources/            # App icons, splash screens
│       ├── icon.png
│       └── splash.png
```

### Capacitor Configuration

```typescript
// capacitor.config.ts
import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.learnsinhala.app',
  appName: 'Learn Sinhala',
  webDir: '../frontend/dist/learn-sinhala',
  server: {
    androidScheme: 'https',
    // For development:
    // url: 'http://192.168.1.x:4200',
    // cleartext: true
  },
  plugins: {
    SplashScreen: {
      launchShowDuration: 2000,
      backgroundColor: '#4F46E5',
      showSpinner: false
    },
    LocalNotifications: {
      smallIcon: 'ic_stat_notification',
      iconColor: '#4F46E5'
    },
    Keyboard: {
      resize: 'body',
      resizeOnFullScreen: true
    }
  },
  ios: {
    contentInset: 'automatic'
  },
  android: {
    allowMixedContent: true
  }
};

export default config;
```

### Native Features to Implement

#### 1. Push Notifications (Practice Reminders)

```typescript
// notification.service.ts
import { LocalNotifications } from '@capacitor/local-notifications';

@Injectable({ providedIn: 'root' })
export class MobileNotificationService {

  async scheduleDailyReminder(hour: number, minute: number): Promise<void> {
    await LocalNotifications.schedule({
      notifications: [{
        id: 1,
        title: 'Time to Practice! 📚',
        body: 'Your daily Sinhala practice is waiting',
        schedule: {
          on: { hour, minute },
          repeats: true,
          allowWhileIdle: true
        },
        sound: 'reminder.wav',
        actionTypeId: 'PRACTICE_REMINDER'
      }]
    });
  }

  async cancelReminders(): Promise<void> {
    await LocalNotifications.cancel({ notifications: [{ id: 1 }] });
  }
}
```

#### 2. Native Audio Player

```typescript
// native-audio.service.ts
import { NativeAudio } from '@capacitor-community/native-audio';

@Injectable({ providedIn: 'root' })
export class NativeAudioService {

  async preloadAudio(id: string, path: string): Promise<void> {
    await NativeAudio.preload({
      assetId: id,
      assetPath: path,
      audioChannelNum: 1,
      isUrl: true
    });
  }

  async play(id: string): Promise<void> {
    await NativeAudio.play({ assetId: id });
  }
}
```

#### 3. Haptic Feedback

```typescript
// haptics.service.ts
import { Haptics, ImpactStyle } from '@capacitor/haptics';

@Injectable({ providedIn: 'root' })
export class HapticsService {

  async correctAnswer(): Promise<void> {
    await Haptics.impact({ style: ImpactStyle.Light });
  }

  async wrongAnswer(): Promise<void> {
    await Haptics.impact({ style: ImpactStyle.Heavy });
  }

  async buttonTap(): Promise<void> {
    await Haptics.impact({ style: ImpactStyle.Light });
  }
}
```

### Mobile-Specific UI Adaptations

```scss
// mobile-overrides.scss

// Larger touch targets
.mobile {
  .btn {
    min-height: 48px;
    min-width: 48px;
  }

  .nav-link {
    padding: 12px 16px;
  }

  // Safe area insets
  .header {
    padding-top: env(safe-area-inset-top);
  }

  .footer {
    padding-bottom: env(safe-area-inset-bottom);
  }

  // Bottom navigation instead of top
  .nav {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    flex-direction: row;
    justify-content: space-around;
    padding-bottom: env(safe-area-inset-bottom);
  }
}
```

### App Store Requirements

#### iOS (App Store)
- Privacy policy URL
- App icons (1024x1024 + variants)
- Screenshots (6.5", 5.5" iPhones, iPad)
- Age rating questionnaire
- App Review guidelines compliance

#### Android (Play Store)
- Feature graphic (1024x500)
- Screenshots (phone + tablet)
- Privacy policy URL
- Content rating questionnaire
- Target API level compliance

### Build & Release Pipeline

```yaml
# .github/workflows/mobile-release.yml
name: Mobile Release

on:
  push:
    tags: ['v*']

jobs:
  build-ios:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v4
      - name: Setup Node
        uses: actions/setup-node@v4
      - name: Build Angular
        run: cd frontend && npm ci && npm run build
      - name: Sync Capacitor
        run: cd mobile && npx cap sync ios
      - name: Build iOS
        run: |
          cd mobile/ios
          xcodebuild -workspace App.xcworkspace \
            -scheme App \
            -configuration Release \
            -archivePath App.xcarchive archive
      - name: Upload to TestFlight
        uses: apple-actions/upload-testflight-build@v1

  build-android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          java-version: '17'
      - name: Build Angular
        run: cd frontend && npm ci && npm run build
      - name: Sync Capacitor
        run: cd mobile && npx cap sync android
      - name: Build APK
        run: |
          cd mobile/android
          ./gradlew assembleRelease
      - name: Upload to Play Store
        uses: r0adkll/upload-google-play@v1
```

---

## 3. Offline Mode

### Overview

Enable users to practice without internet connection by caching vocabulary, audio, and progress locally, then syncing when online.

### Architecture

```
┌─────────────────────────────────────────────────────────┐
│                      Frontend                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐ │
│  │   Angular   │  │   Service   │  │    IndexedDB    │ │
│  │   App       │──│   Worker    │──│    (Dexie.js)   │ │
│  └─────────────┘  └─────────────┘  └─────────────────┘ │
│         │                │                  │           │
│         ▼                ▼                  ▼           │
│  ┌─────────────────────────────────────────────────┐   │
│  │              Sync Manager                        │   │
│  │  - Conflict resolution                          │   │
│  │  - Queue management                             │   │
│  │  - Background sync                              │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼ (when online)
┌─────────────────────────────────────────────────────────┐
│                    Backend API                           │
│              /api/sync/push                              │
│              /api/sync/pull                              │
└─────────────────────────────────────────────────────────┘
```

### Local Database Schema (IndexedDB via Dexie.js)

```typescript
// offline-db.ts
import Dexie, { Table } from 'dexie';

export interface CachedVocabulary {
  id: string;
  sinhala: string;
  pronunciation: string;
  tamil: string;
  english: string;
  category: string;
  difficulty: string;
  audioBlob?: Blob;        // Cached audio file
  cachedAt: number;
}

export interface LocalProgress {
  id: string;              // visudo
  visudo
  vocabularyId: string;
  visudo
  status: string;
  correctCount: number;
  incorrectCount: number;
  streak: number;
  lastReviewedAt: number;
  nextReviewAt: number;
  syncStatus: 'synced' | 'pending' | 'conflict';
  localUpdatedAt: number;
  serverUpdatedAt?: number;
}

export interface PendingAction {
  id: string;
  type: 'answer' | 'progress_update';
  payload: any;
  createdAt: number;
  retryCount: number;
}

export class OfflineDatabase extends Dexie {
  vocabulary!: Table<CachedVocabulary>;
  progress!: Table<LocalProgress>;
  pendingActions!: Table<PendingAction>;

  constructor() {
    super('LearnSinhalaOffline');

    this.version(1).stores({
      vocabulary: 'id, category, difficulty, cachedAt',
      progress: 'id visudo, visudo
vocabularyId, syncStatus, nextReviewAt',
      pendingActions: 'id, type, createdAt'
    });
  }
}

export const offlineDb = new OfflineDatabase();
```

### Offline Service

```typescript
// offline.service.ts
@Injectable({ providedIn: 'root' })
export class OfflineService {

  private db = offlineDb;
  private online$ = new BehaviorSubject<boolean>(navigator.onLine);

  constructor(private api: ApiService) {
    // Monitor connection status
    window.addEventListener('online', () => this.online$.next(true));
    window.addEventListener('offline', () => this.online$.next(false));

    // Sync when coming online
    this.online$.pipe(
      filter(online => online),
      debounceTime(1000)
    ).subscribe(() => this.syncWithServer());
  }

  get isOnline(): boolean {
    return this.online$.value;
  }

  // ==================
  // Vocabulary Caching
  // ==================

  async cacheVocabulary(items: Vocabulary[]): Promise<void> {
    const cached = items.map(item => ({
      ...item,
      cachedAt: Date.now()
    }));
    await this.db.vocabulary.bulkPut(cached);
  }

  async cacheAudio(vocabularyId: string, audioUrl: string): Promise<void> {
    const response = await fetch(audioUrl);
    const blob = await response.blob();
    await this.db.vocabulary.update(vocabularyId, { audioBlob: blob });
  }

  async getVocabulary(id: string): Promise<CachedVocabulary | undefined> {
    return this.db.vocabulary.get(id);
  }

  async getVocabularyByCategory(category: string): Promise<CachedVocabulary[]> {
    return this.db.vocabulary.where('category').equals(category).toArray();
  }

  // ==================
  // Progress Tracking
  // ==================

  async updateProgressLocally(
    vocabularyId: string,
    result: 'CORRECT' | 'WRONG'
  ): Promise<void> {
    const existing = await this.db.progress.get(vocabularyId);

    const updated: LocalProgress = {
      id: vocabularyId,
      vocabularyId,
      status: this.calculateNewStatus(existing, result),
      correctCount: (existing?.correctCount || 0) + (result === 'CORRECT' ? 1 : 0),
      incorrectCount: (existing?.incorrectCount || 0) + (result === 'WRONG' ? 1 : 0),
      streak: result === 'CORRECT' ? (existing?.streak || 0) + 1 : 0,
      lastReviewedAt: Date.now(),
      nextReviewAt: this.calculateNextReview(existing, result),
      syncStatus: 'pending',
      localUpdatedAt: Date.now()
    };

    await this.db.progress.put(updated);

    // Queue for sync
    await this.queueAction({
      id: crypto.randomUUID(),
      type: 'answer',
      payload: { vocabularyId, result },
      createdAt: Date.now(),
      retryCount: 0
    });
  }

  async getTodaysPractice(): Promise<CachedVocabulary[]> {
    const now = Date.now();

    // Get due reviews
    const dueProgress = await this.db.progress
      .where('nextReviewAt')
      .belowOrEqual(now)
      .toArray();

    const dueIds = dueProgress.map(p => p.vocabularyId);

    // Get vocabulary for due items
    const dueVocab = await this.db.vocabulary
      .where('id')
      .anyOf(dueIds)
      .toArray();

    // Add some new words if needed
    const reviewedIds = (await this.db.progress.toArray()).map(p => p.vocabularyId);
    const newVocab = await this.db.vocabulary
      .filter(v => !reviewedIds.includes(v.id))
      .limit(5)
      .toArray();

    return [...dueVocab, ...newVocab].slice(0, 20);
  }

  // ==================
  // Sync Management
  // ==================

  async syncWithServer(): Promise<SyncResult> {
    if (!this.isOnline) {
      return { status: 'offline', synced: 0, conflicts: 0 };
    }

    try {
      // 1. Push pending actions
      const pending = await this.db.pendingActions.toArray();
      let synced = 0;

      for (const action of pending) {
        try {
          await this.pushAction(action);
          await this.db.pendingActions.delete(action.id);
          synced++;
        } catch (error) {
          if (action.retryCount >= 3) {
            console.error('Action failed permanently:', action);
            await this.db.pendingActions.delete(action.id);
          } else {
            await this.db.pendingActions.update(action.id, {
              retryCount: action.retryCount + 1
            });
          }
        }
      }

      // 2. Pull latest from server
      const serverProgress = await firstValueFrom(
        this.api.getProgress()
      );

      // 3. Resolve conflicts
      const conflicts = await this.resolveConflicts(serverProgress);

      return { status: 'success', synced, conflicts };

    } catch (error) {
      return { status: 'error', synced: 0, conflicts: 0, error };
    }
  }

  private async resolveConflicts(serverData: Progress[]): Promise<number> {
    let conflicts = 0;

    for (const server of serverData) {
      const local = await this.db.progress.get(server.vocabularyId);

      if (!local) {
        // No local data, use server
        await this.db.progress.put({
          ...server,
          syncStatus: 'synced',
          localUpdatedAt: Date.now(),
          serverUpdatedAt: server.updatedAt
        });
      } else if (local.syncStatus === 'pending') {
        // Conflict: local changes not yet synced
        // Strategy: Last-write-wins based on timestamp
        if (server.updatedAt > local.localUpdatedAt) {
          await this.db.progress.put({
            ...server,
            syncStatus: 'synced',
            localUpdatedAt: Date.now(),
            serverUpdatedAt: server.updatedAt
          });
          conflicts++;
        }
        // If local is newer, keep local and it will sync on next push
      } else {
        // No conflict, update with server data
        await this.db.progress.put({
          ...server,
          syncStatus: 'synced',
          localUpdatedAt: Date.now(),
          serverUpdatedAt: server.updatedAt
        });
      }
    }

    return conflicts;
  }

  private async queueAction(action: PendingAction): Promise<void> {
    await this.db.pendingActions.add(action);

    // Try immediate sync if online
    if (this.isOnline) {
      this.syncWithServer();
    }
  }
}
```

### Service Worker for Asset Caching

```typescript
// ngsw-config.json additions
{
  "assetGroups": [
    {
      "name": "app",
      "installMode": "prefetch",
      "resources": {
        "files": [
          "/favicon.ico",
          "/index.html",
          "/*.css",
          "/*.js"
        ]
      }
    },
    {
      "name": "audio",
      "installMode": "lazy",
      "updateMode": "prefetch",
      "resources": {
        "urls": [
          "/api/files/audio/**"
        ]
      }
    }
  ],
  "dataGroups": [
    {
      "name": "vocabulary",
      "urls": ["/api/vocabulary/**"],
      "cacheConfig": {
        "strategy": "freshness",
        "maxSize": 100,
        "maxAge": "1d",
        "timeout": "3s"
      }
    }
  ]
}
```

### Offline UI Indicators

```typescript
// offline-indicator.component.ts
@Component({
  selector: 'app-offline-indicator',
  template: `
    @if (!online()) {
      <div class="offline-banner">
        <span class="icon">📴</span>
        <span>You're offline. Changes will sync when connected.</span>
        @if (pendingCount() > 0) {
          <span class="pending">({{ pendingCount() }} pending)</span>
        }
      </div>
    }

    @if (syncing()) {
      <div class="sync-indicator">
        <span class="spinner"></span>
        <span>Syncing...</span>
      </div>
    }
  `
})
export class OfflineIndicatorComponent {
  private offlineService = inject(OfflineService);

  online = signal(navigator.onLine);
  syncing = signal(false);
  pendingCount = signal(0);
}
```

### Storage Estimates

| Data Type | Items | Estimated Size |
|-----------|-------|----------------|
| Vocabulary (text) | 500 | ~250 KB |
| Audio files | 500 | ~25 MB (50KB each) |
| Progress data | 500 | ~50 KB |
| App shell | - | ~2 MB |
| **Total** | - | **~28 MB** |

---

## 4. Sinhala Slang Mode

### Overview

A special learning mode focusing on colloquial, informal, and slang expressions used by young Sri Lankans - the kind of language you won't find in textbooks.

### Content Categories

```typescript
enum SlangCategory {
  STREET_TALK = 'street_talk',      // Everyday informal
  SOCIAL_MEDIA = 'social_media',    // Online/texting slang
  YOUTH_SLANG = 'youth_slang',      // Trendy expressions
  HUMOR = 'humor',                  // Jokes, sarcasm
  EXPRESSIONS = 'expressions',      // Idioms, exclamations
  WARNINGS = 'warnings'             // Rude words to recognize (not use)
}

enum SlangTone {
  FRIENDLY = 'friendly',            // Use with friends
  CASUAL = 'casual',                // Informal settings
  CAUTION = 'caution',              // Know but use carefully
  RECOGNIZE_ONLY = 'recognize_only' // Understand but don't use
}
```

### Data Model Extension

```java
@Document(collection = "slang_vocabulary")
public class SlangVocabulary {
    @Id
    private String id;

    private String sinhala;           // Romanized slang term
    private String pronunciation;
    private String english;           // Meaning
    private String literal;           // Literal translation (often funny)

    private SlangCategory category;
    private SlangTone tone;
    private int popularity;           // 1-5, how commonly used
    private String region;            // Colombo, rural, universal

    private String usage;             // When/how to use
    private String warning;           // What to avoid
    private List<String> alternatives; // Polite alternatives

    private String exampleSinhala;
    private String exampleEnglish;
    private String exampleContext;    // Situation description

    private List<String> tags;
    private boolean adultContent;     // Age-gate flag
}
```

### Sample Slang Data

```java
List<SlangVocabulary> slangPhrases = List.of(

    // ========== FRIENDLY CASUAL ==========

    SlangVocabulary.builder()
        .sinhala("machan")
        .pronunciation("ma-chan")
        .english("Bro / Dude / Mate")
        .literal("From Tamil 'machaan' (brother-in-law)")
        .category(SlangCategory.STREET_TALK)
        .tone(SlangTone.FRIENDLY)
        .popularity(5)
        .region("universal")
        .usage("Used constantly between male friends. Very casual.")
        .exampleSinhala("Machan, koheda yanney?")
        .exampleEnglish("Bro, where are you going?")
        .exampleContext("Calling out to a friend on the street")
        .tags(List.of("greeting", "male", "common"))
        .adultContent(false)
        .build(),

    SlangVocabulary.builder()
        .sinhala("bung")
        .pronunciation("bung")
        .english("Buddy / Pal (informal)")
        .literal("Unknown origin, possibly from 'bungalow' era")
        .category(SlangCategory.YOUTH_SLANG)
        .tone(SlangTone.FRIENDLY)
        .popularity(4)
        .region("Colombo")
        .usage("Casual way to address a friend, slightly playful")
        .exampleSinhala("Bung, meka try karanna")
        .exampleEnglish("Buddy, try this")
        .tags(List.of("address", "urban"))
        .adultContent(false)
        .build(),

    SlangVocabulary.builder()
        .sinhala("yakko")
        .pronunciation("yak-ko")
        .english("Dude (rough) / Devil")
        .literal("From 'yakka' (demon)")
        .category(SlangCategory.STREET_TALK)
        .tone(SlangTone.CASUAL)
        .popularity(4)
        .region("universal")
        .usage("Informal address, can be friendly or confrontational depending on tone")
        .warning("Don't use with strangers or elders")
        .exampleSinhala("Yakko, mokada karanne?")
        .exampleEnglish("Dude, what are you doing?")
        .tags(List.of("address", "male"))
        .adultContent(false)
        .build(),

    // ========== EXPRESSIONS ==========

    SlangVocabulary.builder()
        .sinhala("kella set")
        .pronunciation("kel-la set")
        .english("It's all good / Everything's sorted")
        .literal("'The girl is set' - origin unclear")
        .category(SlangCategory.EXPRESSIONS)
        .tone(SlangTone.FRIENDLY)
        .popularity(3)
        .region("Colombo")
        .usage("Confirming plans are finalized or everything is okay")
        .exampleSinhala("Don't worry machan, kella set")
        .exampleEnglish("Don't worry bro, it's all sorted")
        .exampleContext("Reassuring a friend about party plans")
        .tags(List.of("confirmation", "urban", "youth"))
        .adultContent(false)
        .build(),

    SlangVocabulary.builder()
        .sinhala("pol")
        .pronunciation("pol")
        .english("Fail / Mess up / Embarrass oneself")
        .literal("Coconut (implying empty head)")
        .category(SlangCategory.YOUTH_SLANG)
        .tone(SlangTone.CASUAL)
        .popularity(4)
        .region("universal")
        .usage("When someone does something stupid or fails")
        .exampleSinhala("Exam eke godak pol una")
        .exampleEnglish("Totally failed the exam")
        .exampleContext("Discussing bad exam results with friends")
        .tags(List.of("failure", "humor"))
        .adultContent(false)
        .build(),

    SlangVocabulary.builder()
        .sinhala("patta")
        .pronunciation("pat-ta")
        .english("Awesome / Cool / Amazing")
        .literal("From 'patta' (belt/stripe)")
        .category(SlangCategory.YOUTH_SLANG)
        .tone(SlangTone.FRIENDLY)
        .popularity(5)
        .region("universal")
        .usage("Expressing approval or excitement")
        .exampleSinhala("Meka patta machan!")
        .exampleEnglish("This is awesome bro!")
        .exampleContext("Reacting to something cool")
        .tags(List.of("approval", "excitement", "common"))
        .adultContent(false)
        .build(),

    // ========== SOCIAL MEDIA ==========

    SlangVocabulary.builder()
        .sinhala("sepu")
        .pronunciation("se-pu")
        .english("Super / Excellent")
        .literal("Shortened 'super'")
        .category(SlangCategory.SOCIAL_MEDIA)
        .tone(SlangTone.FRIENDLY)
        .popularity(3)
        .region("universal")
        .usage("Online/text compliment, often in comments")
        .exampleSinhala("Photo eka sepu!")
        .exampleEnglish("The photo is super!")
        .tags(List.of("compliment", "online"))
        .adultContent(false)
        .build(),

    // ========== RECOGNIZE ONLY ==========

    SlangVocabulary.builder()
        .sinhala("modaya")
        .pronunciation("mo-da-ya")
        .english("Fool / Idiot")
        .literal("Fool")
        .category(SlangCategory.WARNINGS)
        .tone(SlangTone.RECOGNIZE_ONLY)
        .popularity(5)
        .region("universal")
        .usage("Insult - you'll hear it, but don't use it")
        .warning("Offensive if directed at someone. Know it to understand context.")
        .alternatives(List.of("Don't call people this"))
        .exampleSinhala("Eya gedara honda modaya")
        .exampleEnglish("(Rude way to call someone foolish)")
        .tags(List.of("insult", "rude", "recognize"))
        .adultContent(false)
        .build()
);
```

### UI/UX Design

```
┌────────────────────────────────────────┐
│  🔥 Slang Mode                    [i]  │
│                                        │
│  ┌────────────────────────────────┐   │
│  │ Filter by vibe:                │   │
│  │ [😎 Chill] [🔥 Trendy] [⚠️ Edgy]│   │
│  └────────────────────────────────┘   │
│                                        │
│  ┌────────────────────────────────┐   │
│  │       "MACHAN"                 │   │
│  │       /ma-chan/                │   │
│  │                                │   │
│  │   = Bro / Dude / Mate         │   │
│  │                                │   │
│  │   ┌──────────────────────┐    │   │
│  │   │ 😎 Friendly          │    │   │
│  │   │ 🔥🔥🔥🔥🔥 Very common   │    │   │
│  │   │ 📍 Used everywhere    │    │   │
│  │   └──────────────────────┘    │   │
│  │                                │   │
│  │   💡 TIP: Use with male       │   │
│  │   friends your age or younger │   │
│  │                                │   │
│  │   Example:                    │   │
│  │   "Machan, koheda yanney?"    │   │
│  │   (Bro, where you going?)     │   │
│  │                                │   │
│  └────────────────────────────────┘   │
│                                        │
│  [🔊 Hear it]     [Next →]            │
└────────────────────────────────────────┘
```

### Tone Indicators

```typescript
const toneConfig = {
  friendly: {
    emoji: '😎',
    color: '#10B981',
    label: 'Friendly',
    description: 'Safe to use with friends'
  },
  casual: {
    emoji: '👋',
    color: '#3B82F6',
    label: 'Casual',
    description: 'Informal settings only'
  },
  caution: {
    emoji: '⚠️',
    color: '#F59E0B',
    label: 'Use Carefully',
    description: 'Can offend if used wrong'
  },
  recognize_only: {
    emoji: '👀',
    color: '#EF4444',
    label: 'Recognize Only',
    description: "Understand it, don't use it"
  }
};
```

### Age Gate

```typescript
// slang-settings.component.ts
@Component({
  template: `
    <div class="slang-settings">
      <h3>Slang Mode Settings</h3>

      <div class="setting">
        <label>
          <input type="checkbox" [(ngModel)]="showAdultContent">
          Include mature content
        </label>
        <p class="hint">
          Some slang may be crude or inappropriate.
          Enable only if you're 18+.
        </p>
      </div>

      <div class="setting">
        <label>Show warnings for:</label>
        <select [(ngModel)]="warningLevel">
          <option value="all">All potentially offensive terms</option>
          <option value="rude">Only rude/insulting terms</option>
          <option value="none">None (I can handle it)</option>
        </select>
      </div>
    </div>
  `
})
export class SlangSettingsComponent {
  showAdultContent = false;
  warningLevel: 'all' | 'rude' | 'none' = 'all';
}
```

### Slang Quiz Mode

```typescript
interface SlangQuiz {
  type: 'meaning' | 'usage' | 'tone';
  question: string;
  options: string[];
  correctIndex: number;
  explanation: string;
}

// Example questions:
const slangQuizzes: SlangQuiz[] = [
  {
    type: 'meaning',
    question: 'What does "patta" mean?',
    options: ['Terrible', 'Awesome', 'Hungry', 'Tired'],
    correctIndex: 1,
    explanation: '"Patta" is used to express that something is cool or amazing!'
  },
  {
    type: 'tone',
    question: 'When is it OK to say "yakko"?',
    options: [
      'To your boss',
      'To your grandmother',
      'To a close friend',
      'To a stranger'
    ],
    correctIndex: 2,
    explanation: '"Yakko" is rough slang - only use with close friends who won\'t mind!'
  },
  {
    type: 'usage',
    question: 'Your friend messed up badly. You say:',
    options: [
      'Eya patta!',
      'Eya pol una!',
      'Eya machan!',
      'Eya sepu!'
    ],
    correctIndex: 1,
    explanation: '"Pol una" means they failed/messed up. "Patta" would mean they did great!'
  }
];
```

### Content Moderation

```java
@Service
public class SlangModerationService {

    // All slang must be reviewed before publishing
    public enum ReviewStatus {
        PENDING,
        APPROVED,
        REJECTED,
        NEEDS_WARNING
    }

    public ReviewResult reviewSlang(SlangVocabulary slang) {
        ReviewResult result = new ReviewResult();

        // Check against blocklist
        if (containsBlockedContent(slang.getSinhala())) {
            result.setStatus(ReviewStatus.REJECTED);
            result.setReason("Contains prohibited content");
            return result;
        }

        // Flag potential issues
        if (isPotentiallyOffensive(slang)) {
            result.setStatus(ReviewStatus.NEEDS_WARNING);
            result.setSuggestedTone(SlangTone.RECOGNIZE_ONLY);
        }

        // Auto-approve safe content
        if (slang.getTone() == SlangTone.FRIENDLY && !slang.isAdultContent()) {
            result.setStatus(ReviewStatus.APPROVED);
        } else {
            result.setStatus(ReviewStatus.PENDING);
            result.setReason("Requires manual review");
        }

        return result;
    }
}
```

---

## Implementation Priority

| Feature | Effort | Impact | Priority |
|---------|--------|--------|----------|
| **Offline Mode** | Medium | High | 1st |
| **Mobile App** | Medium | High | 2nd |
| **Slang Mode** | Low | Medium | 3rd |
| **Speech Recognition** | High | Medium | 4th |

### Recommended Implementation Order

1. **Offline Mode** - Immediately improves UX for users with poor connectivity (common in Sri Lanka)
2. **Mobile App** - Most users will prefer mobile; Capacitor makes this relatively quick
3. **Slang Mode** - Fun feature that differentiates from competitors; mostly content work
4. **Speech Recognition** - Most complex; save for when core features are stable

---

## Technical Dependencies

```json
{
  "offline-mode": {
    "dexie": "^4.0.0",
    "@angular/service-worker": "^18.0.0"
  },
  "mobile-app": {
    "@capacitor/core": "^6.0.0",
    "@capacitor/ios": "^6.0.0",
    "@capacitor/android": "^6.0.0",
    "@capacitor/local-notifications": "^6.0.0",
    "@capacitor/haptics": "^6.0.0"
  },
  "speech-recognition": {
    "@google-cloud/speech": "^6.0.0"
  },
  "slang-mode": {
    "no-additional-dependencies": true
  }
}
```

---

*Document Version: 1.0*
*Last Updated: January 2026*
*Status: Proposal - Not Implemented*
