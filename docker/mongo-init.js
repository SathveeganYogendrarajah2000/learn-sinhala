// ===========================================
// MongoDB Initialization Script
// Creates application database and user
// ===========================================

// Switch to application database
db = db.getSiblingDB('learnsinhala');

// Create application user with readWrite access
db.createUser({
    user: 'learnsinhala',
    pwd: 'learnsinhala123',  // Change in production via env vars
    roles: [
        {
            role: 'readWrite',
            db: 'learnsinhala'
        }
    ]
});

print('Created learnsinhala user');

// Create collections with validation
db.createCollection('users', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['firstName', 'lastName', 'email', 'passwordHash'],
            properties: {
                firstName: {
                    bsonType: 'string',
                    description: 'First name is required'
                },
                lastName: {
                    bsonType: 'string',
                    description: 'Last name is required'
                },
                email: {
                    bsonType: 'string',
                    pattern: '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$',
                    description: 'Valid email is required'
                },
                passwordHash: {
                    bsonType: 'string',
                    description: 'Password hash is required'
                }
            }
        }
    }
});

db.createCollection('vocabulary');
db.createCollection('user_progress');
db.createCollection('sentence_patterns');

print('Created collections');

// Create indexes for better query performance
db.users.createIndex({ 'email': 1 }, { unique: true });

db.vocabulary.createIndex({ 'category': 1, 'difficulty': 1 });
db.vocabulary.createIndex({ 'tags': 1 });
db.vocabulary.createIndex({ 'sinhala': 'text', 'english': 'text', 'tamil': 'text' });

db.user_progress.createIndex({ 'userId': 1, 'vocabularyId': 1 }, { unique: true });
db.user_progress.createIndex({ 'userId': 1, 'nextReviewAt': 1 });
db.user_progress.createIndex({ 'userId': 1, 'status': 1 });

db.sentence_patterns.createIndex({ 'category': 1 });

print('Created indexes');
print('MongoDB initialization complete!');
