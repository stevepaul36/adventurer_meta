import assert from "node:assert/strict";
import test from "node:test";
import { isEntitlementActive, isSafeStoragePath, utcMonthKey } from "./entitlements";

const NOW = Date.parse("2026-10-26T12:00:00.000Z");

test("accepts an unexpired subscription", () => {
  assert.equal(isEntitlementActive({ expires_date: "2026-11-01T00:00:00.000Z" }, NOW), true);
});

test("accepts a subscription during its explicit grace period", () => {
  assert.equal(
    isEntitlementActive({
      expires_date: "2026-10-25T00:00:00.000Z",
      grace_period_expires_date: "2026-10-28T00:00:00.000Z",
    }, NOW),
    true,
  );
});

test("rejects expired, missing and invalid subscription data", () => {
  assert.equal(isEntitlementActive({ expires_date: "2026-10-25T00:00:00.000Z" }, NOW), false);
  assert.equal(isEntitlementActive(undefined, NOW), false);
  assert.equal(isEntitlementActive({ expires_date: "not-a-date" }, NOW), false);
});

test("treats an explicit null expiration as non-expiring", () => {
  assert.equal(isEntitlementActive({ expires_date: null }, NOW), true);
});

test("uses a stable UTC month key", () => {
  assert.equal(utcMonthKey(new Date("2026-10-31T23:59:00.000Z")), "2026-10");
  assert.equal(utcMonthKey(new Date("2026-11-01T00:00:00.000Z")), "2026-11");
});

test("validates the exact authenticated user's temporary video path", () => {
  assert.equal(isSafeStoragePath("ride-videos/user-1/clip.mp4", "user-1"), true);
  assert.equal(isSafeStoragePath("ride-videos/user-2/clip.mp4", "user-1"), false);
  assert.equal(isSafeStoragePath("ride-videos/user-1/../other.mp4", "user-1"), false);
  assert.equal(isSafeStoragePath("ride-videos/user-1/nested/clip.mp4", "user-1"), false);
});