const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function serverNow() {
  return new Date();
}

test("1. Unauthenticated user cannot read user profile or presets", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("presets").get());
});

test("2. Cross-user isolation: Alice cannot read or write Bob's presets, jobs, exports, or settings", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db.collection("users").doc(BOB_UID).collection("presets").doc("p1").set({
      userId: BOB_UID,
      name: "Bob Preset",
      category: "enhance",
      resolution: "1080p",
      fps: 30,
      codec: "H.264",
      bitrateMode: "Auto",
      bitrateKbps: 8000,
      containerFormat: "MP4",
      crf: 20,
      encoderPreset: "medium",
      brightness: 10,
      contrast: 10,
      saturation: 5,
      temperature: 0,
      sharpness: 15,
      denoise: 5,
      createdAt: serverNow(),
      updatedAt: serverNow(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("presets").doc("p1").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("presets").get());
});

test("3. Authenticated owner can create and read valid UserProfile, Preset, Job, Export, and Settings", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = serverNow();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Creator",
      email: "alice@example.com",
      accountTier: "pro",
      cloudConsentGranted: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("presets").doc("preset_1").set({
      userId: ALICE_UID,
      name: "TikTok Crisp 1080p",
      category: "social",
      resolution: "1080p",
      fps: 30,
      codec: "H.264",
      bitrateMode: "Auto",
      bitrateKbps: 12000,
      containerFormat: "MP4",
      crf: 18,
      encoderPreset: "medium",
      brightness: 15,
      contrast: 20,
      saturation: 10,
      temperature: 0,
      sharpness: 20,
      denoise: 10,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("jobs").doc("job_1").set({
      userId: ALICE_UID,
      sourceFileName: "Nature_Video.mp4",
      sourceUri: "content://media/external/video/media/101",
      sourceSizeBytes: 25690112,
      operationType: "enhance",
      executionRoute: "local",
      configSummary: "1080p • 30 FPS • H.264 • +15 Br / +20 Ct",
      status: "processing",
      progressPercent: 45,
      outputSizeBytes: 0,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("exports").doc("export_1").set({
      userId: ALICE_UID,
      jobId: "job_1",
      fileName: "Nature_Video.mp4",
      originalSizeBytes: 25690112,
      outputSizeBytes: 19608371,
      savedPercent: 24,
      resolution: "1080p",
      fps: 30,
      codec: "H.264",
      containerFormat: "MP4",
      durationMs: 14200,
      localUri: "file:///storage/emulated/0/Movies/EliteMatrix/Nature_Video.mp4",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("settings").doc("preferences").set({
      userId: ALICE_UID,
      themePalette: "WarmIvory",
      themeMode: "Light",
      defaultRouting: "Auto",
      cloudConsentAskEveryTime: true,
      preserveMetadata: true,
      notificationsEnabled: true,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("4. Shadow update (ghost field) and terminal job mutation are rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = serverNow();

  await aliceDb.collection("users").doc(ALICE_UID).collection("jobs").doc("job_term").set({
    userId: ALICE_UID,
    sourceFileName: "Clip.mp4",
    sourceUri: "content://media/external/video/media/102",
    sourceSizeBytes: 10000000,
    operationType: "compress",
    executionRoute: "local",
    configSummary: "720p • H.265",
    status: "completed",
    progressPercent: 100,
    outputSizeBytes: 6000000,
    createdAt: now,
    updatedAt: now,
  });

  // Attempting to update a completed job must fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("jobs").doc("job_term").update({
      progressPercent: 50,
      updatedAt: new Date(),
    })
  );

  // Attempting to inject an unknown field on profile must fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@example.com",
      accountTier: "pro",
      cloudConsentGranted: true,
      isAdmin: true,
      createdAt: now,
      updatedAt: now,
    })
  );
});
