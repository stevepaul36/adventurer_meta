import { createHash } from "node:crypto";

export interface RevenueCatEntitlement {
  expires_date?: string | null;
  grace_period_expires_date?: string | null;
}

/** Fail closed when subscription status is absent, malformed or expired. */
export function isEntitlementActive(
  entitlement: RevenueCatEntitlement | undefined,
  nowMs = Date.now(),
): boolean {
  if (!entitlement) return false;
  if (entitlement.expires_date === null) return true;

  const expiryMs =
    typeof entitlement.expires_date === "string"
      ? Date.parse(entitlement.expires_date)
      : Number.NaN;
  if (Number.isFinite(expiryMs) && expiryMs > nowMs) return true;

  const graceMs =
    typeof entitlement.grace_period_expires_date === "string"
      ? Date.parse(entitlement.grace_period_expires_date)
      : Number.NaN;
  return Number.isFinite(graceMs) && graceMs > nowMs;
}

/** Stable per-subscriber quota owner, even if RevenueCat aliases an identity after restore. */
export function stableQuotaOwnerKey(firebaseUid: string, originalAppUserId: unknown): string {
  const canonicalId = typeof originalAppUserId === "string" ? originalAppUserId.trim() : "";
  return createHash("sha256").update(canonicalId || firebaseUid, "utf8").digest("hex");
}

export function utcMonthKey(date: Date): string {
  const month = String(date.getUTCMonth() + 1).padStart(2, "0");
  return `${date.getUTCFullYear()}-${month}`;
}

export function isSafeStoragePath(path: unknown, uid: string): path is string {
  if (typeof path !== "string" || path.length > 512) return false;
  if (!path.startsWith(`ride-videos/${uid}/`)) return false;
  if (path.includes("..") || path.startsWith("/") || path.includes("\\")) return false;
  return path.split("/").length === 3;
}