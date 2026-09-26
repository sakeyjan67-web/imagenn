# CamTok Data Contract

The browser app talks to `CamTokRepository`, defined in `data-store.js`. UI code reads a hydrated snapshot and writes domain changes through `repository.update`, `repository.saveClip`, and `repository.listClips`. The current adapters are local-only; they are not a server database or an account sync service.

## State schema v3

The persisted state is one versioned document with these fields:

- `schemaVersion`: integer used to select an explicit migration.
- `account.id`: local account identifier; replace with the authenticated server user ID after sign-in exists.
- `following`: creator IDs.
- `likedClipIds` and `savedClipIds`: clip IDs, not creator names.
- `notInterestedClipIds`: clip IDs suppressed from the local For You feed.
- `watchHistory`: newest-first `{ clipId, viewedAt }` records, capped at 50 entries.
- `moderationReports`: local report flags with clip ID, reason code, and creation time; production reports must be stored and reviewed server-side.
- `commentsByClipId`: map of clip ID to ordered comment records.
- `settings`: user preferences, including `personalizeRecommendations`.
- `messages`: local preview messages only.

Clip IDs and comment IDs must be stable, globally unique IDs in a shared database. Enforce unique likes and saves on `(account_id, clip_id)`, and index comments by `(clip_id, created_at)`. Validate ownership and permissions on the server; never trust client-supplied counts, coin balances, payouts, moderation decisions, or match results.

## Media boundary

Video metadata and video bytes are separate. The local adapter stores metadata plus a Blob in IndexedDB. A production adapter should upload bytes to object storage/CDN and store only the immutable media key, processing status, dimensions, duration, and metadata in the database. Keep upload retries idempotent by using a stable upload ID.

## Migration and adapter rules

Increment `STATE_VERSION` for persisted shape changes and add a forward-only migration before changing the normalized state. V1-to-v2 adds empty watch-history and not-interested collections; v2-to-v3 adds a local moderation-report collection. Existing likes, comments, settings, and messages are preserved. Legacy `camtok-*` browser keys are imported once. An app that encounters a newer schema refuses to overwrite it. Adapter writes are serialized so rapid reactions/comments cannot reorder snapshots.

When connecting a server database, keep the repository methods and entity IDs stable, hydrate its cached snapshot before rendering, and make writes asynchronous/idempotent. Replace the state/media adapters rather than putting SQL, SDK calls, or network requests inside feed components. Add server authorization, input validation, rate limits, and conflict handling before enabling shared accounts.
