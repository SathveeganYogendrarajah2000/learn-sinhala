/**
 * Local sentence patterns data.
 * Frontend-driven for now, can be moved to backend API later.
 */

export interface SentencePattern {
  id: string;
  name: string;
  category: string;
  sinhalaPattern: string;
  englishPattern: string;
  slots: PatternSlot[];
  examples: PatternExample[];
}

export interface PatternSlot {
  id: string;
  placeholder: string;  // e.g., "{subject}"
  label: string;        // e.g., "Subject"
  type: SlotType;
  options: SlotOption[];
}

export interface SlotOption {
  sinhala: string;
  english: string;
  tamil?: string;
}

export interface PatternExample {
  sinhala: string;
  english: string;
}

export type SlotType = 'subject' | 'object' | 'place' | 'food' | 'time' | 'action';

// =====================
// Vocabulary for slots
// =====================

export const SUBJECTS: SlotOption[] = [
  { sinhala: 'mama', english: 'I', tamil: 'naan' },
  { sinhala: 'oya', english: 'you (informal)', tamil: 'nee' },
  { sinhala: 'oba', english: 'you (formal)', tamil: 'neengal' },
  { sinhala: 'eya', english: 'he/she', tamil: 'avan/aval' },
  { sinhala: 'api', english: 'we', tamil: 'naangal' },
];

export const PLACES: SlotOption[] = [
  { sinhala: 'geta', english: 'home', tamil: 'veedu' },
  { sinhala: 'kadeta', english: 'shop', tamil: 'kadai' },
  { sinhala: 'iskooleta', english: 'school', tamil: 'palli' },
  { sinhala: 'hospital-eka', english: 'hospital', tamil: 'maruthuvamanai' },
  { sinhala: 'bus-eka', english: 'bus', tamil: 'bus' },
  { sinhala: 'Colombo', english: 'Colombo', tamil: 'Colombo' },
];

export const FOODS: SlotOption[] = [
  { sinhala: 'bath', english: 'rice', tamil: 'saadam' },
  { sinhala: 'te', english: 'tea', tamil: 'tea' },
  { sinhala: 'koopi', english: 'coffee', tamil: 'kaapi' },
  { sinhala: 'paang', english: 'bread', tamil: 'rotti' },
  { sinhala: 'mas', english: 'fish/meat', tamil: 'meen/iraichi' },
  { sinhala: 'elawalukaeng', english: 'vegetables', tamil: 'kaaigari' },
];

export const TIMES: SlotOption[] = [
  { sinhala: 'ada', english: 'today', tamil: 'indru' },
  { sinhala: 'heta', english: 'tomorrow', tamil: 'naalai' },
  { sinhala: 'iye', english: 'yesterday', tamil: 'netru' },
  { sinhala: 'daan', english: 'now', tamil: 'ippo' },
  { sinhala: 'passe', english: 'later', tamil: 'appuram' },
];

export const OBJECTS: SlotOption[] = [
  { sinhala: 'pota', english: 'book', tamil: 'puthagam' },
  { sinhala: 'phone-eka', english: 'phone', tamil: 'phone' },
  { sinhala: 'watura', english: 'water', tamil: 'thanneer' },
  { sinhala: 'salli', english: 'money', tamil: 'panam' },
  { sinhala: 'meka', english: 'this', tamil: 'ithu' },
];

// =====================
// Sentence Patterns
// =====================

export const SENTENCE_PATTERNS: SentencePattern[] = [
  {
    id: 'want-to-go',
    name: 'I want to go to...',
    category: 'Travel',
    sinhalaPattern: '{subject} {place} yanna ona',
    englishPattern: '{subject} want(s) to go to {place}',
    slots: [
      {
        id: 'subject',
        placeholder: '{subject}',
        label: 'Who',
        type: 'subject',
        options: SUBJECTS
      },
      {
        id: 'place',
        placeholder: '{place}',
        label: 'Where',
        type: 'place',
        options: PLACES
      }
    ],
    examples: [
      { sinhala: 'Mama geta yanna ona', english: 'I want to go home' },
      { sinhala: 'Oya Colombo yanna ona', english: 'You want to go to Colombo' }
    ]
  },
  {
    id: 'want-to-eat',
    name: 'I want to eat...',
    category: 'Food',
    sinhalaPattern: '{subject} {food} kanna ona',
    englishPattern: '{subject} want(s) to eat {food}',
    slots: [
      {
        id: 'subject',
        placeholder: '{subject}',
        label: 'Who',
        type: 'subject',
        options: SUBJECTS
      },
      {
        id: 'food',
        placeholder: '{food}',
        label: 'What food',
        type: 'food',
        options: FOODS
      }
    ],
    examples: [
      { sinhala: 'Mama bath kanna ona', english: 'I want to eat rice' },
      { sinhala: 'Eya paang kanna ona', english: 'He/She wants to eat bread' }
    ]
  },
  {
    id: 'want-to-drink',
    name: 'I want to drink...',
    category: 'Food',
    sinhalaPattern: '{subject} {drink} bonna ona',
    englishPattern: '{subject} want(s) to drink {drink}',
    slots: [
      {
        id: 'subject',
        placeholder: '{subject}',
        label: 'Who',
        type: 'subject',
        options: SUBJECTS
      },
      {
        id: 'drink',
        placeholder: '{drink}',
        label: 'What drink',
        type: 'food',
        options: [
          { sinhala: 'te', english: 'tea', tamil: 'tea' },
          { sinhala: 'koopi', english: 'coffee', tamil: 'kaapi' },
          { sinhala: 'watura', english: 'water', tamil: 'thanneer' },
          { sinhala: 'kiri', english: 'milk', tamil: 'paal' },
        ]
      }
    ],
    examples: [
      { sinhala: 'Mama te bonna ona', english: 'I want to drink tea' },
      { sinhala: 'Oba koopi bonna ona', english: 'You want to drink coffee' }
    ]
  },
  {
    id: 'give-me',
    name: 'Please give me...',
    category: 'Request',
    sinhalaPattern: 'Karunakara mata {object} denna',
    englishPattern: 'Please give me {object}',
    slots: [
      {
        id: 'object',
        placeholder: '{object}',
        label: 'What',
        type: 'object',
        options: [
          ...OBJECTS,
          { sinhala: 'bath', english: 'rice', tamil: 'saadam' },
          { sinhala: 'te ekak', english: 'a tea', tamil: 'oru tea' },
        ]
      }
    ],
    examples: [
      { sinhala: 'Karunakara mata watura denna', english: 'Please give me water' },
      { sinhala: 'Karunakara mata te ekak denna', english: 'Please give me a tea' }
    ]
  },
  {
    id: 'where-is',
    name: 'Where is...?',
    category: 'Questions',
    sinhalaPattern: '{place} koheda?',
    englishPattern: 'Where is {place}?',
    slots: [
      {
        id: 'place',
        placeholder: '{place}',
        label: 'What place',
        type: 'place',
        options: [
          { sinhala: 'bus-eka', english: 'the bus', tamil: 'bus' },
          { sinhala: 'bathroom-eka', english: 'the bathroom', tamil: 'bathroom' },
          { sinhala: 'hospital-eka', english: 'the hospital', tamil: 'maruthuvamanai' },
          { sinhala: 'iskooleta', english: 'the school', tamil: 'palli' },
          { sinhala: 'kadeta', english: 'the shop', tamil: 'kadai' },
        ]
      }
    ],
    examples: [
      { sinhala: 'Bus-eka koheda?', english: 'Where is the bus?' },
      { sinhala: 'Bathroom-eka koheda?', english: 'Where is the bathroom?' }
    ]
  },
  {
    id: 'how-much',
    name: 'How much is this?',
    category: 'Shopping',
    sinhalaPattern: 'Meka kiyada?',
    englishPattern: 'How much is this?',
    slots: [],
    examples: [
      { sinhala: 'Meka kiyada?', english: 'How much is this?' }
    ]
  },
  {
    id: 'i-am-going',
    name: 'I am going to...',
    category: 'Travel',
    sinhalaPattern: '{subject} {time} {place} yanawa',
    englishPattern: '{subject} {time} going to {place}',
    slots: [
      {
        id: 'subject',
        placeholder: '{subject}',
        label: 'Who',
        type: 'subject',
        options: SUBJECTS
      },
      {
        id: 'time',
        placeholder: '{time}',
        label: 'When',
        type: 'time',
        options: TIMES
      },
      {
        id: 'place',
        placeholder: '{place}',
        label: 'Where',
        type: 'place',
        options: PLACES
      }
    ],
    examples: [
      { sinhala: 'Mama heta Colombo yanawa', english: 'I am going to Colombo tomorrow' },
      { sinhala: 'Api ada kadeta yanawa', english: 'We are going to the shop today' }
    ]
  },
  {
    id: 'i-like',
    name: 'I like...',
    category: 'Preferences',
    sinhalaPattern: '{subject} {object} kamathi',
    englishPattern: '{subject} like(s) {object}',
    slots: [
      {
        id: 'subject',
        placeholder: '{subject}',
        label: 'Who',
        type: 'subject',
        options: [
          { sinhala: 'mata', english: 'I', tamil: 'enakku' },
          { sinhala: 'oyata', english: 'you', tamil: 'unakku' },
          { sinhala: 'eyata', english: 'he/she', tamil: 'avanukku' },
        ]
      },
      {
        id: 'object',
        placeholder: '{object}',
        label: 'What',
        type: 'object',
        options: [
          ...FOODS,
          { sinhala: 'meka', english: 'this', tamil: 'ithu' },
        ]
      }
    ],
    examples: [
      { sinhala: 'Mata bath kamathi', english: 'I like rice' },
      { sinhala: 'Oyata te kamathi', english: 'You like tea' }
    ]
  }
];

/**
 * Get patterns by category
 */
export function getPatternsByCategory(): Map<string, SentencePattern[]> {
  const map = new Map<string, SentencePattern[]>();

  for (const pattern of SENTENCE_PATTERNS) {
    const existing = map.get(pattern.category) || [];
    existing.push(pattern);
    map.set(pattern.category, existing);
  }

  return map;
}

/**
 * Get all unique categories
 */
export function getCategories(): string[] {
  return [...new Set(SENTENCE_PATTERNS.map(p => p.category))];
}
