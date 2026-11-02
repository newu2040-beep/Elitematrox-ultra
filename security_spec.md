# Security Specification — ELITE MATRIX

## 1. Data Invariants
1. **Strict Multi-Tenant Ownership**: Every document under `/users/{userId}` and its subcollections (`presets`, `jobs`, `exports`, `settings`) belongs exclusively to `userId`. Only an authenticated user with `request.auth.uid == userId` may read, create, update, or delete documents in their namespace.
2. **Identity Integrity**: Every document written must carry `userId == request.auth.uid`, and `userId` is immutable across all updates.
3. **Temporal Integrity**: Every document must carry a server-generated `createdAt` (`is timestamp && <= request.time`) and `updatedAt` (`is timestamp && <= request.time`). `createdAt` is immutable on update.
4. **Terminal Job Locking**: Once a job in `/users/{userId}/jobs/{jobId}` reaches a terminal status (`completed`, `failed`, `cancelled`), it cannot be mutated except via explicit retry action which resets status to `queued` or `processing`.
5. **Bounded Strings & Numeric Ranges**: All strings have strict `.size()` upper bounds, path IDs match `^[a-zA-Z0-9_\-]+$`, and numeric parameters (`progressPercent`, `fps`, `savedPercent`, sizes) are bounded integers/numbers.

## 2. The "Dirty Dozen" Payloads
1. **Unauthenticated Profile Read**: `GET /users/alice_123` with `auth == null` -> REJECT.
2. **Cross-User Preset Read**: `GET /users/bob_456/presets/p1` as `alice_123` -> REJECT.
3. **Identity Spoofing on Create**: `CREATE /users/alice_123/jobs/j1` with `userId: "bob_456"` as `alice_123` -> REJECT.
4. **Ghost Field Injection (Shadow Update)**: `UPDATE /users/alice_123` adding `"isAdmin": true` -> REJECT (`hasOnly` check).
5. **Immutable `createdAt` Tampering**: `UPDATE /users/alice_123/presets/p1` modifying `createdAt` -> REJECT.
6. **Immutable `userId` Tampering**: `UPDATE /users/alice_123/exports/e1` modifying `userId` -> REJECT.
7. **Invalid Enum Value**: `CREATE /users/alice_123/jobs/j1` with `status: "hacked_state"` -> REJECT.
8. **Out-of-Bounds Progress**: `UPDATE /users/alice_123/jobs/j1` with `progressPercent: 500` -> REJECT.
9. **Oversized String DoS**: `CREATE /users/alice_123/presets/p1` with a 10,000-char `name` -> REJECT.
10. **Future Timestamp Injection**: `CREATE /users/alice_123` with `createdAt` in the future (`> request.time`) -> REJECT.
11. **Missing Required Keys**: `CREATE /users/alice_123/exports/e1` omitting `fileName` or `outputSizeBytes` -> REJECT.
12. **Value Type Poisoning**: `UPDATE /users/alice_123/settings/preferences` setting `themeMode` to an integer `123` instead of a valid enum string -> REJECT.
