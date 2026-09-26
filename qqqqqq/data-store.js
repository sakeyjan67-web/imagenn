(function attachCamTokStore(global) {
  "use strict";

  const STATE_VERSION = 3;
  const STATE_KEY = "camtok:state";
  const DATABASE_NAME = "camtok-local-media";
  const DATABASE_VERSION = 1;

  function emptyState() {
    return {
      schemaVersion: STATE_VERSION,
      account: { id: "local-user" },
      following: [],
      likedClipIds: [],
      savedClipIds: [],
      notInterestedClipIds: [],
      watchHistory: [],
      moderationReports: [],
      commentsByClipId: {},
      settings: {},
      messages: []
    };
  }

  function readLegacyJson(storage, key, fallback) {
    try {
      const value = JSON.parse(storage.getItem(key) || "null");
      return value ?? fallback;
    } catch {
      return fallback;
    }
  }

  function normalizeState(value) {
    const state = value && typeof value === "object" ? value : {};
    const normalized = emptyState();
    normalized.account = {
      id: typeof state.account?.id === "string" ? state.account.id : "local-user"
    };
    for (const key of ["following", "likedClipIds", "savedClipIds", "notInterestedClipIds", "watchHistory", "moderationReports", "messages"]) {
      if (Array.isArray(state[key])) normalized[key] = state[key];
    }
    if (state.commentsByClipId && typeof state.commentsByClipId === "object" && !Array.isArray(state.commentsByClipId)) {
      normalized.commentsByClipId = state.commentsByClipId;
    }
    if (state.settings && typeof state.settings === "object" && !Array.isArray(state.settings)) {
      normalized.settings = state.settings;
    }
    return normalized;
  }

  class LocalStateAdapter {
    constructor(storage) {
      this.storage = storage;
      this.memoryState = emptyState();
    }

    async load() {
      let state = null;
      try {
        state = JSON.parse(this.storage.getItem(STATE_KEY) || "null");
      } catch {
        state = null;
      }
      if (state && Number(state.schemaVersion) > STATE_VERSION) {
        throw new Error("CamTok data was created by a newer app version");
      }
      if (state && Number(state.schemaVersion) <= STATE_VERSION) {
        const migrated = normalizeState(state);
        if (Number(state.schemaVersion) < STATE_VERSION) await this.save(migrated);
        return migrated;
      }

      const legacyLikes = readLegacyJson(this.storage, "camtok-liked", []);
      const legacySaves = readLegacyJson(this.storage, "camtok-saved", []);
      state = normalizeState({
        schemaVersion: STATE_VERSION,
        following: readLegacyJson(this.storage, "camtok-followed", []),
        likedClipIds: Array.isArray(legacyLikes) ? legacyLikes : [],
        savedClipIds: Array.isArray(legacySaves) ? legacySaves : [],
        commentsByClipId: readLegacyJson(this.storage, "camtok-comments", {}),
        settings: Object.fromEntries(["notifications", "privateAccount", "dataSaver"].flatMap(key => {
          const value = readLegacyJson(this.storage, `camtok-setting-${key}`, null);
          return value === null ? [] : [[key, value === true || value === "true"]];
        }))
      });
      await this.save(state);
      return state;
    }

    async save(state) {
      this.memoryState = normalizeState(state);
      try {
        this.storage.setItem(STATE_KEY, JSON.stringify(this.memoryState));
      } catch {
        // Keep the in-memory snapshot usable when browser storage is disabled or full.
      }
    }
  }

  class LocalMediaAdapter {
    constructor() {
      this.databasePromise = null;
    }

    open() {
      if (this.databasePromise) return this.databasePromise;
      this.databasePromise = new Promise((resolve, reject) => {
        if (!global.indexedDB) {
          reject(new Error("IndexedDB is unavailable"));
          return;
        }
        const request = global.indexedDB.open(DATABASE_NAME, DATABASE_VERSION);
        request.onupgradeneeded = () => {
          const database = request.result;
          if (!database.objectStoreNames.contains("clips")) database.createObjectStore("clips", { keyPath: "id" });
        };
        request.onsuccess = () => resolve(request.result);
        request.onerror = () => reject(request.error);
      }).catch(error => {
        this.databasePromise = null;
        throw error;
      });
      return this.databasePromise;
    }

    async saveClip(clip, file) {
      const database = await this.open();
      const { videoUrl, ...metadata } = clip;
      return new Promise((resolve, reject) => {
        const transaction = database.transaction("clips", "readwrite");
        transaction.objectStore("clips").put({ ...metadata, videoBlob: file, createdAt: Date.now() });
        transaction.oncomplete = resolve;
        transaction.onerror = () => reject(transaction.error);
        transaction.onabort = () => reject(transaction.error);
      });
    }

    async listClips() {
      const database = await this.open();
      return new Promise((resolve, reject) => {
        const transaction = database.transaction("clips", "readonly");
        const request = transaction.objectStore("clips").getAll();
        request.onsuccess = () => resolve(request.result);
        request.onerror = () => reject(request.error);
      });
    }
  }

  class CamTokRepository {
    constructor({ stateAdapter, mediaAdapter }) {
      this.stateAdapter = stateAdapter;
      this.mediaAdapter = mediaAdapter;
      this.state = emptyState();
      this.writeQueue = Promise.resolve();
    }

    async initialize() {
      this.state = normalizeState(await this.stateAdapter.load());
      return this.snapshot();
    }

    snapshot() {
      return structuredClone(this.state);
    }

    update(mutator) {
      mutator(this.state);
      this.state = normalizeState(this.state);
      const snapshot = this.snapshot();
      this.writeQueue = this.writeQueue.catch(() => undefined).then(() => this.stateAdapter.save(snapshot));
      return this.writeQueue;
    }

    saveClip(clip, file) {
      return this.mediaAdapter.saveClip(clip, file);
    }

    async listClips() {
      return this.mediaAdapter.listClips();
    }
  }

  function create(options = {}) {
    let storage = options.storage;
    if (!storage) {
      try {
        storage = global.localStorage;
      } catch {
        storage = null;
      }
    }
    return new CamTokRepository({
      stateAdapter: options.stateAdapter || new LocalStateAdapter(storage),
      mediaAdapter: options.mediaAdapter || new LocalMediaAdapter()
    });
  }

  global.CamTokStore = Object.freeze({ create, schemaVersion: STATE_VERSION });
})(window);
